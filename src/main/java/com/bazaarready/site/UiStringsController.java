package com.bazaarready.site;

import com.bazaarready.claude.ClaudeClient;
import com.bazaarready.common.Languages;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder screens in any language. The eight hand-checked languages ship with the page; for any other,
 * the page sends its English words here and gets them back translated by Claude.
 *
 * <p>The result is stored under the language and a hash of the English, so each language is translated
 * once per version of the page, and a request with different English can never change what others see.
 * The translation must keep the exact shape of the English and every {placeholder}; any part that doesn't
 * falls back to English. Requests are size-limited and count against the AI rate limits and daily cap.
 */
@RestController
class UiStringsController {

    private static final Logger log = LoggerFactory.getLogger(UiStringsController.class);
    private static final int MAX_BYTES = 60_000;
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{[a-zA-Z]+\\}");

    private final ClaudeClient claude;
    private final ObjectMapper json;
    private final JdbcTemplate jdbc;

    UiStringsController(ClaudeClient claude, ObjectMapper json, JdbcTemplate jdbc) {
        this.claude = claude;
        this.json = json;
        this.jdbc = jdbc;
    }

    @PostMapping("/api/ui-strings/{code}")
    ResponseEntity<JsonNode> translate(@PathVariable String code, @RequestBody String english) throws Exception {
        Languages.Language language = Languages.of(code);
        if (!code.matches("[a-z]{2,3}") || (language.code().equals("en") && !code.equals("en"))) {
            return ResponseEntity.badRequest().build();
        }
        byte[] bytes = english.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) {
            return ResponseEntity.status(413).build();
        }
        JsonNode source = json.readTree(english);
        if (!source.isObject()) {
            return ResponseEntity.badRequest().build();
        }
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        List<String> stored = jdbc.queryForList("select strings from ui_strings where language = ? and hash = ?", String.class, code, hash);
        if (!stored.isEmpty()) {
            return ResponseEntity.ok(json.readTree(stored.get(0)));
        }

        // Each part of the page is translated in parallel, so a new language is ready in seconds, not a minute.
        ObjectNode result = json.createObjectNode();
        List<String> keys = new ArrayList<>();
        List<CompletableFuture<JsonNode>> parts = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = source.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> part = it.next();
            keys.add(part.getKey());
            parts.add(CompletableFuture.supplyAsync(() -> translatePart(part.getValue(), language)));
        }
        boolean complete = true;
        for (int i = 0; i < keys.size(); i++) {
            JsonNode english0 = source.get(keys.get(i));
            JsonNode translated = parts.get(i).join();
            if (translated == null) {
                complete = false;
            }
            result.set(keys.get(i), merge(english0, translated));
        }
        if (complete) {
            jdbc.update("insert into ui_strings (language, hash, strings) values (?, ?, ?) on conflict do nothing",
                    code, hash, result.toString());
        }
        return ResponseEntity.ok(result);
    }

    private JsonNode translatePart(JsonNode english, Languages.Language language) {
        String system = """
                You translate the words on a website builder for small businesses and creators. Translate every \
                text value in the JSON into %s, in its usual script, short and natural, the way apps in that \
                language word things. Keep the JSON keys and structure exactly. Keep placeholders such as {name} \
                and {items}, brand names (BazaarReady, WhatsApp, Instagram), emoji, prices and phone numbers as \
                they are; you may change currency and place names in examples to ones that suit the language. \
                Reply with the JSON only.
                """.formatted(language.englishName());
        try {
            // Every part goes in wrapped as {"v": ...}, so a single sentence comes back as JSON too.
            ObjectNode wrapped = json.createObjectNode();
            wrapped.set("v", english);
            String reply = claude.createText(ClaudeClient.Feature.UI_TRANSLATE, ClaudeClient.FAST_MODEL, 8000, system, wrapped.toString());
            int start = reply.indexOf('{');
            int end = reply.lastIndexOf('}');
            return json.readTree(reply.substring(start, end + 1)).get("v");
        } catch (Exception e) {
            log.warn("UI translation into {} failed: {}", language.code(), e.getMessage());
            return null;
        }
    }

    private static int index(String s, char c) {
        int i = s.indexOf(c);
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    /** Takes the translation where it matches the English shape and keeps its placeholders; English elsewhere. */
    private JsonNode merge(JsonNode english, JsonNode translated) {
        if (english.isTextual()) {
            if (translated != null && translated.isTextual() && !translated.asText().isBlank()
                    && placeholders(english.asText()).equals(placeholders(translated.asText()))) {
                return translated;
            }
            return english;
        }
        if (english.isArray()) {
            ArrayNode out = json.createArrayNode();
            for (int i = 0; i < english.size(); i++) {
                out.add(merge(english.get(i), translated != null && translated.isArray() && i < translated.size() ? translated.get(i) : null));
            }
            return out;
        }
        if (english.isObject()) {
            ObjectNode out = json.createObjectNode();
            english.fields().forEachRemaining(e -> out.set(e.getKey(),
                    merge(e.getValue(), translated != null && translated.isObject() ? translated.get(e.getKey()) : null)));
            return out;
        }
        return english;
    }

    private static List<String> placeholders(String text) {
        List<String> found = new ArrayList<>();
        Matcher m = PLACEHOLDER.matcher(text);
        while (m.find()) {
            found.add(m.group());
        }
        found.sort(String::compareTo);
        return found;
    }
}
