package com.bazaarready.claude;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

/**
 * Thin client for the Claude Messages API. It sends one request and returns the raw response;
 * the agent loop decides what to do with it.
 *
 * <p>Prompt caching: the fixed instructions (tools, then the system prompt) carry a cache marker, so
 * repeated requests read them from Claude's cache at a fraction of the price. Multi-step features
 * (see {@link Feature#conversation()}) also turn on automatic caching, which moves the marker to the end
 * of the conversation so each step re-reads everything before it. One-off questions don't, because
 * their last part is different every time and would be written to the cache and never read.
 *
 * <p>Every reply's token counts are recorded by {@link ClaudeUsage} for the public /usage page.
 */
@Component
public class ClaudeClient {

    public static final String AGENT_MODEL = "claude-sonnet-5-5";

    /** For simple, high-volume jobs such as translating labels. */
    public static final String FAST_MODEL = "claude-haiku-5-5";

    /** What a call is for, shown on the usage page. Conversation features call Claude several times in a row. */
    public enum Feature {
        BUILD("Building websites", true),
        COACH("Idea coach", true),
        COLLAB_CHECK("Collab and enquiry check", true),
        TEST_CUSTOMERS("AI test customers", true),
        TEST_SUMMARY("Test report summary", false),
        SCOPE_CHECK("Off-topic check", false),
        PHOTO_PICK("Choosing photos", false),
        LABELS("Translating site labels", false),
        HAGGLE("Haggle mode", true),
        CHAT_TRANSLATE("Two-language chat", false),
        SHUTTER_VOICE("Shutter voice notes", false),
        FOCUS_GROUP("AI focus group", false),
        UI_TRANSLATE("Translating builder screens", false);

        private final String label;
        private final boolean conversation;

        Feature(String label, boolean conversation) {
            this.label = label;
            this.conversation = conversation;
        }

        public String label() {
            return label;
        }

        public boolean conversation() {
            return conversation;
        }
    }

    private final RestClient http;
    private final ObjectMapper json;
    private final String apiKey;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    private final int dailyCap;
    private final int perAccountCap;
    private final ClaudeUsage usage;

    ClaudeClient(@Value("${bazaarready.anthropic.api-key:}") String apiKey, RestClient.Builder builder, ObjectMapper json,
                 org.springframework.jdbc.core.JdbcTemplate jdbc, ClaudeUsage usage,
                 @Value("${bazaarready.claude.daily-cap:3000}") int dailyCap,
                 @Value("${bazaarready.claude.per-account-daily-cap:400}") int perAccountCap) {
        this.jdbc = jdbc;
        this.usage = usage;
        this.dailyCap = dailyCap;
        this.perAccountCap = perAccountCap;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(120));
        this.http = builder.baseUrl("https://api.anthropic.com").requestFactory(factory).build();
        this.json = json;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    /**
     * Counts this call against today's caps: one for the whole service and one per signed-in account.
     * Work with no signed-in person (photo checks, AI test customers) counts only towards the total.
     */
    /**
     * Features anyone can use without signing in (bargaining, customer chat, page translation). When nobody is
     * signed in, these count in their own bucket, capped at a share of the daily total, so traffic from the
     * public can never use up the budget that owners need to build and fix their sites.
     */
    private static final java.util.Set<Feature> PUBLIC_FEATURES = java.util.EnumSet.of(
            Feature.HAGGLE, Feature.CHAT_TRANSLATE, Feature.UI_TRANSLATE, Feature.LABELS);
    private static final String PUBLIC_BUCKET = "~public";
    private static final int PUBLIC_SHARE_PERCENT = 40;

    private void countAndCheckCap(Feature feature) {
        String account = com.bazaarready.account.AccountService.currentEmail();
        boolean isPublic = account == null && PUBLIC_FEATURES.contains(feature);
        String key = isPublic ? PUBLIC_BUCKET : account == null ? "" : account;
        Integer mine = jdbc.queryForObject("""
                insert into claude_usage (day, account, calls) values (current_date, ?, 1)
                on conflict (day, account) do update set calls = claude_usage.calls + 1
                returning calls
                """, Integer.class, key);
        Integer total = jdbc.queryForObject("select coalesce(sum(calls), 0) from claude_usage where day = current_date", Integer.class);
        if (total != null && total > dailyCap) {
            throw new ClaudeException("BazaarReady has reached today's limit for AI requests. Please try again tomorrow.");
        }
        if (isPublic && mine != null && mine > dailyCap * PUBLIC_SHARE_PERCENT / 100) {
            throw new ClaudeException("This is busy right now. Please try again tomorrow, or message the shop on WhatsApp.");
        }
        if (account != null && mine != null && mine > perAccountCap) {
            throw new ClaudeException("Your account has reached today's limit for AI requests. It resets tomorrow.");
        }
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    /** One plain question and answer, with no tools. Returns the text of the reply. */
    public String createText(Feature feature, String model, int maxTokens, String system, String userText) {
        ArrayNode messages = json.createArrayNode();
        messages.addObject().put("role", "user").put("content", userText);
        JsonNode response = createMessage(feature, model, maxTokens, system, null, messages);
        StringBuilder text = new StringBuilder();
        for (JsonNode block : response.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                text.append(block.path("text").asText());
            }
        }
        return text.toString();
    }

    /** One call to Claude with tools. The model may answer with text or ask to use exactly one tool. */
    public JsonNode createMessage(Feature feature, String model, int maxTokens, String system, ArrayNode tools, ArrayNode messages) {
        if (!isConfigured()) {
            throw new ClaudeException("The Claude API key is not set. Add ANTHROPIC_API_KEY to the .env file and restart.");
        }
        countAndCheckCap(feature);
        ObjectNode body = json.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        // The system prompt as one text block with a cache marker. Tools come before it in the prompt,
        // so the marker covers both. Prompts shorter than the model's minimum (512 tokens) are simply not cached.
        ObjectNode systemBlock = body.putArray("system").addObject();
        systemBlock.put("type", "text");
        systemBlock.put("text", system);
        systemBlock.putObject("cache_control").put("type", "ephemeral");
        if (feature.conversation()) {
            body.putObject("cache_control").put("type", "ephemeral");
        }
        if (tools != null) {
            body.set("tools", tools);
            ObjectNode toolChoice = body.putObject("tool_choice");
            toolChoice.put("type", "auto");
            toolChoice.put("disable_parallel_tool_use", true);
        }
        body.set("messages", messages);

        long started = System.nanoTime();
        try {
            JsonNode response = http.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            usage.record(feature, model, response, (System.nanoTime() - started) / 1_000_000);
            return response;
        } catch (RestClientResponseException e) {
            String detail = e.getResponseBodyAsString();
            if (detail.length() > 300) {
                detail = detail.substring(0, 300);
            }
            throw new ClaudeException("Claude returned an error (HTTP " + e.getStatusCode().value() + "): " + detail);
        }
    }
}
