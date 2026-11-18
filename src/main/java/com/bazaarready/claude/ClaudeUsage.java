package com.bazaarready.claude;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Records the token counts from every Claude reply and works out what the call cost.
 *
 * <p>Claude reports input in three parts: tokens read at full price ({@code input_tokens}), tokens
 * written to the prompt cache ({@code cache_creation_input_tokens}, 1.25x price) and tokens read
 * from it ({@code cache_read_input_tokens}, a fraction of the price). The uncached cost is what the
 * same request would have cost with no caching, so the difference is what caching saved.
 */
@Component
public class ClaudeUsage {

    private static final Logger log = LoggerFactory.getLogger(ClaudeUsage.class);
    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

    /** US dollars per million tokens: input, output, cache read. A 5-minute cache write is 1.25x input. */
    record Price(double input, double output, double cacheRead) {
    }

    static final Map<String, Price> PRICES = Map.of(
            "claude-sonnet-5-5", new Price(2.00, 10.00, 0.10),
            "claude-haiku-5-5", new Price(0.10, 0.50, 0.01));

    private final JdbcTemplate jdbc;

    ClaudeUsage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    void record(ClaudeClient.Feature feature, String model, JsonNode response, long durationMs) {
        try {
            JsonNode u = response == null ? null : response.path("usage");
            if (u == null || u.isMissingNode()) {
                return;
            }
            long input = u.path("input_tokens").asLong();
            long write = u.path("cache_creation_input_tokens").asLong();
            long read = u.path("cache_read_input_tokens").asLong();
            long output = u.path("output_tokens").asLong();
            Price p = PRICES.getOrDefault(model, PRICES.get(ClaudeClient.AGENT_MODEL));
            double cost = input * p.input() + write * p.input() * 1.25 + read * p.cacheRead() + output * p.output();
            double uncached = (input + write + read) * p.input() + output * p.output();
            jdbc.update("""
                    insert into claude_call (feature, model, input_tokens, cache_write, cache_read, output_tokens, cost_usd, uncached_usd, duration_ms)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, feature.name().toLowerCase(), model, input, write, read, output,
                    usd(cost), usd(uncached), (int) Math.min(durationMs, Integer.MAX_VALUE));
        } catch (RuntimeException e) {
            // Tracking must never break the feature that called Claude.
            log.warn("Could not record Claude usage: {}", e.getMessage());
        }
    }

    private static BigDecimal usd(double perMillionTotal) {
        return BigDecimal.valueOf(perMillionTotal).divide(MILLION, 6, RoundingMode.HALF_UP);
    }
}
