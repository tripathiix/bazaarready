package com.bazaarready.guard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Public facts about an internet domain, used to judge whether a sender is who they claim to be:
 * when it was registered (RDAP), and whether it is set up to send real email (MX, SPF and DMARC
 * records, looked up over DNS-over-HTTPS). These are plain lookups; the judgement is the agent's.
 */
@Component
class DomainFacts {

    private static final Pattern DOMAIN = Pattern.compile("^(?=.{4,253}$)([a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,24}$");

    static final Set<String> FREE_MAIL = Set.of("gmail.com", "googlemail.com", "yahoo.com", "yahoo.co.in", "outlook.com",
            "hotmail.com", "live.com", "icloud.com", "me.com", "proton.me", "protonmail.com", "rediffmail.com", "aol.com",
            "gmx.com", "yandex.com", "mail.com");

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(6))
            .build();
    private final ObjectMapper json;

    DomainFacts(ObjectMapper json) {
        this.json = json;
    }

    static String normalise(String domain) {
        if (domain == null) {
            return null;
        }
        String d = domain.trim().toLowerCase(Locale.ROOT);
        if (d.startsWith("www.")) {
            d = d.substring(4);
        }
        return DOMAIN.matcher(d).matches() ? d : null;
    }

    /** A short report for the agent. Lookups that fail say so instead of guessing. */
    String report(String rawDomain) {
        String domain = normalise(rawDomain);
        if (domain == null) {
            return "Not a valid domain name: " + rawDomain;
        }
        StringBuilder out = new StringBuilder("domain: ").append(domain).append('\n');
        if (FREE_MAIL.contains(domain)) {
            out.append("This is a free email provider anyone can sign up to, so it says nothing about who the sender is.\n");
            return out.toString();
        }
        out.append(registration(domain));
        out.append("mail server (MX record): ").append(hasRecord(domain, "MX", null)).append('\n');
        out.append("SPF record (says which servers may send its email): ").append(hasRecord(domain, "TXT", "v=spf1")).append('\n');
        out.append("DMARC record (anti-spoofing policy): ").append(hasRecord("_dmarc." + domain, "TXT", "v=DMARC1")).append('\n');
        return out.toString();
    }

    private String registration(String domain) {
        try {
            HttpResponse<String> response = http.send(HttpRequest.newBuilder(URI.create("https://rdap.org/domain/" + domain))
                    .timeout(Duration.ofSeconds(12)).header("Accept", "application/rdap+json").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                return "registered: NO. The domain does not exist, so mail claiming to come from it cannot be genuine.\n";
            }
            if (response.statusCode() != 200) {
                return "registration date: could not be looked up (HTTP " + response.statusCode() + ")\n";
            }
            JsonNode body = json.readTree(response.body());
            String registered = null;
            for (JsonNode event : body.path("events")) {
                if ("registration".equals(event.path("eventAction").asText())) {
                    registered = event.path("eventDate").asText();
                }
            }
            String registrar = null;
            for (JsonNode entity : body.path("entities")) {
                for (JsonNode role : entity.path("roles")) {
                    if ("registrar".equals(role.asText())) {
                        JsonNode vcard = entity.path("vcardArray").path(1);
                        for (JsonNode field : vcard) {
                            if ("fn".equals(field.path(0).asText())) {
                                registrar = field.path(3).asText();
                            }
                        }
                    }
                }
            }
            StringBuilder out = new StringBuilder("registered: yes\n");
            if (registered != null) {
                long days = ChronoUnit.DAYS.between(Instant.parse(registered), Instant.now());
                out.append("registered on: ").append(registered, 0, 10).append(" (").append(days).append(" days ago)\n");
            } else {
                out.append("registered on: not published\n");
            }
            if (registrar != null) {
                out.append("registrar: ").append(registrar).append('\n');
            }
            return out.toString();
        } catch (Exception e) {
            return "registration date: could not be looked up (" + e.getClass().getSimpleName() + ")\n";
        }
    }

    private String hasRecord(String name, String type, String mustStartWith) {
        try {
            String url = "https://dns.google/resolve?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8) + "&type=" + type;
            HttpResponse<String> response = http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            JsonNode answers = json.readTree(response.body()).path("Answer");
            for (JsonNode answer : answers) {
                String data = answer.path("data").asText().replace("\"", "");
                if (mustStartWith == null || data.toLowerCase(Locale.ROOT).startsWith(mustStartWith.toLowerCase(Locale.ROOT))) {
                    return "yes";
                }
            }
            return "no";
        } catch (Exception e) {
            return "could not be looked up";
        }
    }
}
