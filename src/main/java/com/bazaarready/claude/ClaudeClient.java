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
 */
@Component
public class ClaudeClient {

    public static final String AGENT_MODEL = "claude-sonnet-5-5";

    /** For simple, high-volume jobs such as translating labels. */
    public static final String FAST_MODEL = "claude-haiku-5-5";

    private final RestClient http;
    private final ObjectMapper json;
    private final String apiKey;

    ClaudeClient(@Value("${bazaarready.anthropic.api-key:}") String apiKey, RestClient.Builder builder, ObjectMapper json) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(120));
        this.http = builder.baseUrl("https://api.anthropic.com").requestFactory(factory).build();
        this.json = json;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    /** One plain question and answer, with no tools. Returns the text of the reply. */
    public String createText(String model, int maxTokens, String system, String userText) {
        ArrayNode messages = json.createArrayNode();
        messages.addObject().put("role", "user").put("content", userText);
        JsonNode response = createMessage(model, maxTokens, system, null, messages);
        StringBuilder text = new StringBuilder();
        for (JsonNode block : response.path("content")) {
            if ("text".equals(block.path("type").asText())) {
                text.append(block.path("text").asText());
            }
        }
        return text.toString();
    }

    /** One call to Claude with tools. The model may answer with text or ask to use exactly one tool. */
    public JsonNode createMessage(String model, int maxTokens, String system, ArrayNode tools, ArrayNode messages) {
        if (!isConfigured()) {
            throw new ClaudeException("The Claude API key is not set. Add ANTHROPIC_API_KEY to the .env file and restart.");
        }
        ObjectNode body = json.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("system", system);
        if (tools != null) {
            body.set("tools", tools);
            ObjectNode toolChoice = body.putObject("tool_choice");
            toolChoice.put("type", "auto");
            toolChoice.put("disable_parallel_tool_use", true);
        }
        body.set("messages", messages);

        try {
            return http.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            String detail = e.getResponseBodyAsString();
            if (detail.length() > 300) {
                detail = detail.substring(0, 300);
            }
            throw new ClaudeException("Claude returned an error (HTTP " + e.getStatusCode().value() + "): " + detail);
        }
    }
}
