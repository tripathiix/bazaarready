package com.bazaarready.claude;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The public "How BazaarReady uses AI" page: totals only, from {@link ClaudeUsage}'s records.
 * Nothing here identifies a person or shows a prompt.
 */
@Controller
class UsageController {

    /** One column of headline numbers (today, last 7 days, all time). */
    record Period(String label, long calls, String tokens, String cost, String saved, int hitRate) {
    }

    record FeatureRow(String label, String model, long calls, String tokens, String cost, String saved, int hitRate, String avgSeconds) {
    }

    /** One bar of the daily chart. Heights are percentages of the tallest day. */
    record Day(String label, String title, int cachedPct, int otherPct) {
    }

    private final JdbcTemplate jdbc;
    private final int dailyCap;

    UsageController(JdbcTemplate jdbc, @Value("${bazaarready.claude.daily-cap:3000}") int dailyCap) {
        this.jdbc = jdbc;
        this.dailyCap = dailyCap;
    }

    @GetMapping("/usage")
    String usage(Model model) {
        model.addAttribute("periods", List.of(
                period("Today", "at >= current_date"),
                period("Last 7 days", "at >= current_date - 6"),
                period("All time", "true")));
        model.addAttribute("features", features());
        model.addAttribute("days", days());
        Integer callsToday = jdbc.queryForObject("select coalesce(sum(calls), 0) from claude_usage where day = current_date", Integer.class);
        int used = callsToday == null ? 0 : callsToday;
        model.addAttribute("capUsed", used);
        model.addAttribute("cap", dailyCap);
        model.addAttribute("capPct", Math.min(100, dailyCap == 0 ? 0 : used * 100 / dailyCap));
        Date since = jdbc.queryForObject("select min(at)::date from claude_call", Date.class);
        model.addAttribute("since", since == null ? null : since.toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)));
        return "usage";
    }

    private static final String SUMS = """
            count(*) as calls,
            coalesce(sum(input_tokens), 0) as input, coalesce(sum(cache_write), 0) as write,
            coalesce(sum(cache_read), 0) as read, coalesce(sum(output_tokens), 0) as output,
            coalesce(sum(cost_usd), 0) as cost, coalesce(sum(uncached_usd), 0) as uncached
            """;

    private Period period(String label, String where) {
        Map<String, Object> r = jdbc.queryForMap("select " + SUMS + " from claude_call where " + where);
        long in = n(r, "input") + n(r, "write") + n(r, "read");
        return new Period(label, n(r, "calls"), tokens(in + n(r, "output")), money(d(r, "cost")),
                money(d(r, "uncached").subtract(d(r, "cost"))), pct(n(r, "read"), in));
    }

    private List<FeatureRow> features() {
        List<FeatureRow> rows = new ArrayList<>();
        Map<String, ClaudeClient.Feature> byKey = new HashMap<>();
        for (ClaudeClient.Feature f : ClaudeClient.Feature.values()) {
            byKey.put(f.name().toLowerCase(), f);
        }
        for (Map<String, Object> r : jdbc.queryForList("select feature, string_agg(distinct model, ', ') as models, avg(duration_ms) as ms, "
                + SUMS + " from claude_call group by feature order by sum(cost_usd) desc")) {
            ClaudeClient.Feature f = byKey.get((String) r.get("feature"));
            long in = n(r, "input") + n(r, "write") + n(r, "read");
            String models = ((String) r.get("models")).replace("claude-", "").replace("-5-5", " 5.5").replace("sonnet", "Sonnet").replace("haiku", "Haiku");
            double ms = ((Number) r.get("ms")).doubleValue();
            rows.add(new FeatureRow(f == null ? (String) r.get("feature") : f.label(), models, n(r, "calls"),
                    tokens(in + n(r, "output")), money(d(r, "cost")), money(d(r, "uncached").subtract(d(r, "cost"))),
                    pct(n(r, "read"), in), String.format(Locale.ENGLISH, "%.1f s", ms / 1000)));
        }
        return rows;
    }

    private List<Day> days() {
        Map<LocalDate, long[]> byDay = new HashMap<>();
        for (Map<String, Object> r : jdbc.queryForList("""
                select at::date as day, coalesce(sum(cache_read), 0) as read,
                       coalesce(sum(input_tokens + cache_write + output_tokens), 0) as other
                from claude_call where at >= current_date - 13 group by 1
                """)) {
            byDay.put(((Date) r.get("day")).toLocalDate(), new long[] {n(r, "read"), n(r, "other")});
        }
        LocalDate today = jdbc.queryForObject("select current_date", Date.class).toLocalDate();
        long max = byDay.values().stream().mapToLong(v -> v[0] + v[1]).max().orElse(0);
        List<Day> days = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long[] v = byDay.getOrDefault(day, new long[] {0, 0});
            int cached = max == 0 ? 0 : (int) Math.round(v[0] * 100.0 / max);
            int other = max == 0 ? 0 : (int) Math.round(v[1] * 100.0 / max);
            String title = day.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)) + ": " + tokens(v[0] + v[1])
                    + " tokens, " + tokens(v[0]) + " read from cache";
            days.add(new Day(day.format(DateTimeFormatter.ofPattern("d", Locale.ENGLISH)), title, cached, other));
        }
        return days;
    }

    private static long n(Map<String, Object> r, String key) {
        Object v = r.get(key);
        return v == null ? 0 : ((Number) v).longValue();
    }

    private static BigDecimal d(Map<String, Object> r, String key) {
        Object v = r.get(key);
        return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString());
    }

    private static int pct(long part, long whole) {
        return whole == 0 ? 0 : (int) Math.round(part * 100.0 / whole);
    }

    static String tokens(long t) {
        if (t >= 1_000_000) {
            return String.format(Locale.ENGLISH, "%.1fM", t / 1_000_000.0);
        }
        if (t >= 10_000) {
            return String.format(Locale.ENGLISH, "%.0fk", t / 1000.0);
        }
        return String.format(Locale.ENGLISH, "%,d", t);
    }

    static String money(BigDecimal usd) {
        double v = usd.doubleValue();
        if (v > 0 && v < 0.001) {
            return "<$0.001";
        }
        return String.format(Locale.ENGLISH, v != 0 && Math.abs(v) < 1 ? "$%.3f" : "$%,.2f", v);
    }
}
