package com.bazaarready.testing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A plain, script-free browser for the AI test customers. It can only visit pages of the one site
 * under test, and turns each page into the text, links and forms a visitor would notice.
 */
@Component
class SiteBrowser {

    record Field(String name, String label, boolean required, List<String> options) {
    }

    record Form(String method, String action, List<Field> fields) {
    }

    record Page(int status, String path, String title, String text, List<String> links, List<Form> forms, Document doc) {

        /** The page as the test customer sees it. */
        String render() {
            StringBuilder out = new StringBuilder();
            out.append("PAGE ").append(path).append(" (HTTP ").append(status).append(")\n");
            out.append("TITLE: ").append(title).append("\n\n").append(text).append("\n");
            if (!links.isEmpty()) {
                out.append("\nLINKS:\n");
                links.forEach(link -> out.append("- ").append(link).append("\n"));
            }
            for (Form form : forms) {
                out.append("\nFORM ").append(form.method()).append(" ").append(form.action()).append("\n");
                for (Field field : form.fields()) {
                    out.append("- field \"").append(field.name()).append("\": ").append(field.label())
                            .append(field.required() ? " (required)" : "");
                    if (!field.options().isEmpty()) {
                        out.append(" options: ").append(String.join(" | ", field.options()));
                    }
                    out.append("\n");
                }
            }
            String s = out.toString();
            return s.length() > 6000 ? s.substring(0, 6000) + "\n[page cut short]" : s;
        }
    }

    /** Elements that start a new line when a page is read out. */
    private static final java.util.Set<String> BLOCKS = java.util.Set.of("div", "p", "h1", "h2", "h3", "h4", "li", "tr",
            "section", "header", "footer", "nav", "main", "form", "label", "button", "dt", "dd", "summary",
            "details", "table", "ul", "ol", "br", "a");

    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String origin;

    SiteBrowser(@Value("${server.port:8080}") int port) {
        this.origin = "http://127.0.0.1:" + port;
    }

    /** Whether a path belongs to the site under test. Anything else is refused. */
    static boolean allowed(String base, String path) {
        return path != null && path.length() < 300 && !path.contains("..") && !path.contains("//")
                && (path.equals(base) || path.startsWith(base + "/") || path.startsWith(base + "?"));
    }

    /** Turns a link or redirect target into a site path, or null if it points elsewhere. */
    static String toPath(String target) {
        if (target == null || target.isBlank()) {
            return null;
        }
        if (target.startsWith("/")) {
            return target;
        }
        try {
            URI uri = URI.create(target);
            if (uri.getHost() == null) {
                return null;
            }
            String host = uri.getHost();
            if (!host.equals("localhost") && !host.equals("127.0.0.1")) {
                return null;
            }
            return uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    Page get(String base, String path) throws Exception {
        return request(base, "GET", path, null);
    }

    Page post(String base, String path, Map<String, String> fields) throws Exception {
        String body = fields.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return request(base, "POST", path, body);
    }

    private Page request(String base, String method, String path, String body) throws Exception {
        String current = path;
        String currentMethod = method;
        for (int hop = 0; hop < 5; hop++) {
            if (!allowed(base, current)) {
                throw new IllegalArgumentException("That address is not part of this website: " + current);
            }
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(origin + current))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "BazaarReady-TestCustomer");
            if ("POST".equals(currentMethod)) {
                builder.header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body));
            } else {
                builder.GET();
            }
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            if (status >= 300 && status < 400) {
                current = toPath(response.headers().firstValue("Location").orElse(null));
                currentMethod = "GET";
                body = null;
                continue;
            }
            return parse(base, current, status, response.body());
        }
        throw new IllegalStateException("Too many redirects");
    }

    private static Page parse(String base, String path, int status, String html) {
        Document doc = Jsoup.parse(html);
        Document view = doc.clone();
        view.select("script,style,svg,noscript,[aria-hidden=true]").remove();
        Element body = view.body();

        StringBuilder text = new StringBuilder();
        NodeTraversor.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode textNode) {
                    text.append(textNode.text());
                } else if (node instanceof Element el && BLOCKS.contains(el.tagName())) {
                    text.append(el.tagName().matches("h[1-4]") ? "\n## " : "\n");
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node instanceof Element el && BLOCKS.contains(el.tagName())) {
                    text.append("\n");
                }
            }
        }, body);
        String lines = java.util.Arrays.stream(text.toString().split("\n"))
                .map(line -> line.replaceAll("\\s+", " ").trim())
                .filter(line -> !line.isEmpty() && !line.equals("##"))
                .collect(Collectors.joining("\n"));
        text.setLength(0);
        text.append(lines).append("\n");
        List<String> images = new ArrayList<>();
        for (Element img : body.select("img")) {
            if (!img.hasAttr("alt")) {
                images.add("[image with no description]");
            } else if (!img.attr("alt").isBlank()) {
                images.add("[image: " + img.attr("alt") + "]");
            }
        }
        if (!images.isEmpty()) {
            text.append(String.join(" ", images)).append("\n");
        }

        List<String> links = new ArrayList<>();
        for (Element a : body.select("a[href]")) {
            String target = toPath(a.attr("href"));
            String label = a.text().isBlank() ? a.attr("aria-label") : a.text();
            if (label.isBlank()) {
                label = "(link with no text)";
            }
            String entry = target != null && allowed(base, target) ? label + " -> " + target : label + " -> (external site)";
            if (!links.contains(entry) && links.size() < 40) {
                links.add(entry);
            }
        }

        List<Form> forms = new ArrayList<>();
        for (Element form : body.select("form")) {
            List<Field> fields = new ArrayList<>();
            for (Element input : form.select("input[name],select[name],textarea[name]")) {
                String type = input.attr("type");
                if (type.equals("hidden") || type.equals("submit")) {
                    continue;
                }
                List<String> options = input.tagName().equals("select")
                        ? input.select("option").stream().map(o -> o.text() + " = " + o.attr("value")).limit(12).toList()
                        : List.of();
                fields.add(new Field(input.attr("name"), labelOf(view, input), input.hasAttr("required"), options));
            }
            for (Element button : form.select("button[name]")) {
                fields.add(new Field(button.attr("name"), "button \"" + button.text() + "\" sends value " + button.attr("value"),
                        false, List.of()));
            }
            String action = toPath(form.attr("action"));
            forms.add(new Form(form.attr("method").isBlank() ? "GET" : form.attr("method").toUpperCase(),
                    action == null ? path : action, fields));
        }
        String title = doc.title();
        String content = text.length() > 4500 ? text.substring(0, 4500) + "[...]" : text.toString();
        return new Page(status, path, title, content, links, forms, doc);
    }

    private static String labelOf(Document doc, Element input) {
        String id = input.id();
        if (!id.isEmpty()) {
            Element label = doc.selectFirst("label[for=" + id + "]");
            if (label != null && !label.text().isBlank()) {
                return "labelled \"" + label.text() + "\"";
            }
        }
        if (!input.attr("aria-label").isBlank()) {
            return "labelled \"" + input.attr("aria-label") + "\"";
        }
        Element wrapping = input.closest("label");
        if (wrapping != null && !wrapping.text().isBlank()) {
            return "labelled \"" + wrapping.text() + "\"";
        }
        if (!input.attr("placeholder").isBlank()) {
            return "NO LABEL (only a placeholder: \"" + input.attr("placeholder") + "\")";
        }
        return "NO LABEL";
    }
}
