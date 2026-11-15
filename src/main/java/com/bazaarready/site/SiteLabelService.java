package com.bazaarready.site;

import com.bazaarready.claude.ClaudeClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Supplies a site's fixed labels in its language, translating and storing them the first time. */
@Service
public class SiteLabelService {

    private static final Logger log = LoggerFactory.getLogger(SiteLabelService.class);
    private static final SiteLabels ENGLISH = new SiteLabels(Map.of());

    private final JdbcTemplate jdbc;
    private final ClaudeClient claude;
    private final ObjectMapper json;
    private final Map<String, SiteLabels> cache = new ConcurrentHashMap<>();
    private final Set<String> inProgress = ConcurrentHashMap.newKeySet();

    SiteLabelService(JdbcTemplate jdbc, ClaudeClient claude, ObjectMapper json) {
        this.jdbc = jdbc;
        this.claude = claude;
        this.json = json;
    }

    /** Labels for a page view. Never waits on a translation: shows English until one is stored. */
    public SiteLabels labelsFor(String language) {
        String code = normalise(language);
        if (code.equals("en")) {
            return ENGLISH;
        }
        SiteLabels cached = cache.get(code);
        if (cached != null) {
            return cached;
        }
        SiteLabels stored = load(code);
        if (stored != null && stored.complete()) {
            cache.put(code, stored);
            return stored;
        }
        if (claude.isConfigured() && inProgress.add(code)) {
            Thread worker = new Thread(() -> {
                try {
                    ensure(code);
                } finally {
                    inProgress.remove(code);
                }
            }, "site-labels-" + code);
            worker.setDaemon(true);
            worker.start();
        }
        return stored != null ? stored : ENGLISH;
    }

    /** Makes sure labels exist for a language, translating them now if needed. Called when a profile is saved. */
    public void ensure(String language) {
        String code = normalise(language);
        SiteLabels existing = cache.containsKey(code) ? cache.get(code) : load(code);
        if (code.equals("en") || (existing != null && existing.complete()) || !claude.isConfigured()) {
            return;
        }
        try {
            Map<String, String> translated = translate(code);
            jdbc.update("insert into site_labels (language, labels) values (?, ?) on conflict (language) do update set labels = excluded.labels, created_at = now()",
                    code, json.writeValueAsString(translated));
            cache.put(code, new SiteLabels(translated));
        } catch (Exception e) {
            log.warn("Could not translate site labels into {}: {}", code, e.getMessage());
        }
    }

    private Map<String, String> translate(String code) throws Exception {
        String languageName = "hinglish".equals(code)
                ? "Hinglish (Hindi written in English letters)"
                : Locale.forLanguageTag(code).getDisplayLanguage(Locale.ENGLISH);
        String system = """
                You translate the interface labels of a small-business website. Reply with one JSON object \
                and nothing else. Use exactly the same keys. Translate each value into %s (language code %s), \
                in its usual script, short and natural, the way shopping and booking websites in that \
                language word their buttons. Keep placeholders such as {name}, {amount} and {days} unchanged. \
                Keep the word WhatsApp as it is.
                """.formatted(languageName, code);
        String reply = claude.createText(ClaudeClient.FAST_MODEL, 4000, system, json.writeValueAsString(SiteLabels.ENGLISH));
        int start = reply.indexOf('{');
        int end = reply.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("no JSON object in the reply");
        }
        JsonNode node = json.readTree(reply.substring(start, end + 1));
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : SiteLabels.ENGLISH.keySet()) {
            JsonNode value = node.get(key);
            if (value != null && value.isTextual() && !value.asText().isBlank()) {
                result.put(key, value.asText().trim());
            }
        }
        if (result.size() < SiteLabels.ENGLISH.size() / 2) {
            throw new IllegalStateException("too few labels came back");
        }
        return result;
    }

    private SiteLabels load(String code) {
        List<String> rows = jdbc.queryForList("select labels from site_labels where language = ?", String.class, code);
        if (rows.isEmpty()) {
            return null;
        }
        try {
            return new SiteLabels(json.readValue(rows.get(0), new TypeReference<Map<String, String>>() { }));
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalise(String language) {
        return language == null || language.isBlank() ? "en" : language.trim().toLowerCase(Locale.ROOT);
    }
}
