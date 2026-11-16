package com.bazaarready.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Per-address limits on the requests that cost money or invite abuse: AI calls, sign-in attempts,
 * customer forms and uploads. Simple fixed windows kept in memory, which is enough for one server.
 *
 * <p>The app itself (the AI test customers, which browse generated sites from inside the server) is
 * not limited. Behind the proxy, the client address comes from Tomcat's RemoteIpValve, which only
 * trusts forwarding headers from private-network proxies, so it cannot be faked from outside.
 */
class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private record Rule(String name, Predicate<HttpServletRequest> applies, int limit, Duration window, String message) {
    }

    private static final Pattern OWNER_AI = Pattern.compile("^/owner/[0-9a-f-]{36}/(test|check)$");
    private static final Pattern OWNER_EDIT = Pattern.compile("^/owner/[0-9a-f-]{36}/(photo|products.*)$");

    private static boolean post(HttpServletRequest r) {
        return "POST".equalsIgnoreCase(r.getMethod());
    }

    private static boolean ai(HttpServletRequest r) {
        String uri = r.getRequestURI();
        return post(r) && (uri.startsWith("/api/agent/") || uri.startsWith("/api/coach") || OWNER_AI.matcher(uri).matches());
    }

    private static final String AI_MESSAGE = "You're going a little fast. Please wait a minute and try again.";

    private static final List<Rule> RULES = List.of(
            new Rule("ai-minute", RateLimitFilter::ai, 12, Duration.ofMinutes(1), AI_MESSAGE),
            new Rule("ai-day", RateLimitFilter::ai, 250, Duration.ofDays(1), "You've reached today's limit for AI requests. Please come back tomorrow."),
            new Rule("sign-in", r -> post(r) && (r.getRequestURI().equals("/login") || r.getRequestURI().equals("/signup")),
                    10, Duration.ofMinutes(15), "Too many sign-in attempts. Please wait 15 minutes and try again."),
            new Rule("customer-forms", r -> post(r) && r.getRequestURI().startsWith("/s/"),
                    20, Duration.ofMinutes(10), "Too many orders or messages from your connection. Please wait a few minutes."),
            new Rule("owner-edits", r -> post(r) && OWNER_EDIT.matcher(r.getRequestURI()).matches(),
                    40, Duration.ofMinutes(10), "Too many changes in a short time. Please wait a few minutes."));

    /** rule|address -> {window number, count} */
    private final Map<String, long[]> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String address = request.getRemoteAddr();
        if (!isLocal(address)) {
            long now = System.currentTimeMillis();
            for (Rule rule : RULES) {
                if (!rule.applies().test(request)) {
                    continue;
                }
                long window = now / rule.window().toMillis();
                long[] counter = counters.compute(rule.name() + "|" + address, (k, old) ->
                        old == null || old[0] != window ? new long[] {window, 1} : new long[] {window, old[1] + 1});
                if (counter[1] > rule.limit()) {
                    log.info("Rate limit '{}' reached for {}", rule.name(), address);
                    refuse(request, response, rule, (window + 1) * rule.window().toMillis() - now);
                    return;
                }
            }
            if (counters.size() > 50_000) {
                counters.clear(); // crude, but keeps memory bounded if someone sprays addresses
            }
        }
        chain.doFilter(request, response);
    }

    private static boolean isLocal(String address) {
        return "127.0.0.1".equals(address) || "0:0:0:0:0:0:0:1".equals(address) || "::1".equals(address);
    }

    private static void refuse(HttpServletRequest request, HttpServletResponse response, Rule rule, long retryMillis)
            throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(Math.max(1, retryMillis / 1000)));
        response.setCharacterEncoding("UTF-8");
        if (request.getRequestURI().startsWith("/api/")) {
            response.setContentType("application/problem+json");
            response.getWriter().write("{\"status\":429,\"title\":\"Too many requests\",\"detail\":\"" + rule.message() + "\"}");
        } else {
            response.setContentType("text/html");
            response.getWriter().write("<!doctype html><meta charset=utf-8><meta name=viewport content=\"width=device-width\">"
                    + "<title>Please wait</title><body style=\"font-family:system-ui;max-width:32rem;margin:4rem auto;padding:0 1rem\">"
                    + "<h1>Please wait a moment</h1><p>" + rule.message() + "</p><p><a href=\"/\">Back to BazaarReady</a></p>");
        }
    }
}
