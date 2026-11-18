package com.bazaarready.extras;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The owner's settings for the living shutter, haggle mode, media kit and coming-soon mode.
 * A site with no row simply uses the defaults: open, no haggling, no media kit, not coming soon.
 */
@Service
public class ExtrasService {

    /** One platform line of a creator's media kit. */
    public record Platform(String name, String handle, String followers, String views) {
    }

    public record Extras(boolean shopOpen, String closedNote, boolean autoHours, LocalTime openAt, LocalTime closeAt,
                         String timeZone, boolean haggleOn, int haggleMaxOff, int haggleMinQty,
                         List<Platform> platforms, String audience, boolean comingSoon, List<BigDecimal> priceOptions) {

        static final Extras DEFAULTS = new Extras(true, null, false, null, null, "Asia/Kolkata", false, 10, 1,
                List.of(), null, false, List.of());

        /** Open right now: by the owner's switch, or by the opening hours when those are on. */
        public boolean openNow() {
            if (autoHours && openAt != null && closeAt != null) {
                LocalTime now = ZonedDateTime.now(zone()).toLocalTime();
                boolean inside = openAt.isBefore(closeAt)
                        ? !now.isBefore(openAt) && now.isBefore(closeAt)
                        : !now.isBefore(openAt) || now.isBefore(closeAt); // hours that run past midnight
                return inside;
            }
            return shopOpen;
        }

        public ZoneId zone() {
            try {
                return ZoneId.of(timeZone);
            } catch (DateTimeException e) {
                return ZoneId.of("Asia/Kolkata");
            }
        }

        public boolean hasMediaKit() {
            return !platforms.isEmpty();
        }

        /** The lowest price haggling may reach, in whole currency units, for the owner's page. */
        public BigDecimal floorOf(BigDecimal price) {
            return com.bazaarready.profile.ProfileService.toUnits(floorMinor(com.bazaarready.profile.ProfileService.toMinor(price)));
        }

        /** The lowest unit price haggling may reach for a list price, in the smallest currency unit. */
        public long floorMinor(long listMinor) {
            int off = Math.max(0, Math.min(50, haggleMaxOff));
            return Math.round(listMinor * (100 - off) / 100.0);
        }
    }

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    ExtrasService(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public Extras get(UUID businessId) {
        List<Extras> rows = jdbc.query("select * from site_extras where business_id = ?", (rs, i) -> {
            Time open = rs.getTime("open_at");
            Time close = rs.getTime("close_at");
            List<Platform> platforms = new ArrayList<>();
            String audience = null;
            String kit = rs.getString("media_kit");
            if (kit != null) {
                try {
                    JsonNode node = json.readTree(kit);
                    for (JsonNode p : node.path("platforms")) {
                        platforms.add(new Platform(p.path("name").asText(), p.path("handle").asText(),
                                p.path("followers").asText(), p.path("views").asText()));
                    }
                    audience = node.path("audience").asText(null);
                } catch (Exception ignored) {
                    // an unreadable kit is treated as none
                }
            }
            return new Extras(rs.getBoolean("shop_open"), rs.getString("closed_note"), rs.getBoolean("auto_hours"),
                    open == null ? null : open.toLocalTime(), close == null ? null : close.toLocalTime(),
                    rs.getString("time_zone"), rs.getBoolean("haggle_on"), rs.getInt("haggle_max_off"),
                    rs.getInt("haggle_min_qty"), platforms, audience, rs.getBoolean("coming_soon"),
                    parsePrices(rs.getString("price_options")));
        }, businessId);
        return rows.isEmpty() ? Extras.DEFAULTS : rows.get(0);
    }

    private void ensureRow(UUID businessId) {
        jdbc.update("insert into site_extras (business_id) values (?) on conflict do nothing", businessId);
    }

    public void setShutter(UUID businessId, boolean open, String note, boolean autoHours, LocalTime openAt,
                           LocalTime closeAt, String timeZone) {
        ensureRow(businessId);
        jdbc.update("""
                update site_extras set shop_open = ?, closed_note = ?, auto_hours = ?, open_at = ?, close_at = ?,
                       time_zone = ?, updated_at = now() where business_id = ?
                """, open, note, autoHours && openAt != null && closeAt != null,
                openAt == null ? null : Time.valueOf(openAt), closeAt == null ? null : Time.valueOf(closeAt),
                validZone(timeZone), businessId);
    }

    /** Flips the switch only, keeping the hours; used by the one-tap button and voice notes. */
    public void setOpen(UUID businessId, boolean open, String note) {
        ensureRow(businessId);
        jdbc.update("update site_extras set shop_open = ?, closed_note = ?, auto_hours = false, updated_at = now() where business_id = ?",
                open, note, businessId);
    }

    public void setHaggle(UUID businessId, boolean on, int maxOff, int minQty) {
        ensureRow(businessId);
        jdbc.update("update site_extras set haggle_on = ?, haggle_max_off = ?, haggle_min_qty = ?, updated_at = now() where business_id = ?",
                on, Math.max(1, Math.min(50, maxOff)), Math.max(1, Math.min(99, minQty)), businessId);
    }

    public void setMediaKit(UUID businessId, List<Platform> platforms, String audience) {
        ensureRow(businessId);
        String kit = null;
        if (!platforms.isEmpty()) {
            var node = json.createObjectNode();
            var list = node.putArray("platforms");
            for (Platform p : platforms) {
                list.addObject().put("name", p.name()).put("handle", p.handle()).put("followers", p.followers()).put("views", p.views());
            }
            if (audience != null && !audience.isBlank()) {
                node.put("audience", audience);
            }
            kit = node.toString();
        }
        jdbc.update("update site_extras set media_kit = ?, updated_at = now() where business_id = ?", kit, businessId);
    }

    public void setComingSoon(UUID businessId, boolean on, List<BigDecimal> prices) {
        ensureRow(businessId);
        String joined = String.join(",", prices.stream().map(BigDecimal::toPlainString).toList());
        jdbc.update("update site_extras set coming_soon = ?, price_options = ?, updated_at = now() where business_id = ?",
                on, joined.isEmpty() ? null : joined, businessId);
    }

    // ---- Coming-soon waitlist ----

    public record Signup(String name, String contact, String priceSeen, java.time.Instant at) {
    }

    /** How many visitors saw a price, and how many of them joined. */
    public record PriceResult(String price, int signups) {
    }

    public void addSignup(UUID businessId, String name, String contact, String priceSeen) {
        jdbc.update("insert into waitlist_signup (business_id, name, contact, price_seen) values (?, ?, ?, ?)",
                businessId, name, contact, priceSeen);
    }

    public List<Signup> signups(UUID businessId) {
        return jdbc.query("select * from waitlist_signup where business_id = ? order by created_at desc limit 200",
                (rs, i) -> new Signup(rs.getString("name"), rs.getString("contact"), rs.getString("price_seen"),
                        rs.getTimestamp("created_at").toInstant()), businessId);
    }

    /** Sign-ups per price, in the order the owner wrote the prices. */
    public List<PriceResult> priceResults(UUID businessId, List<BigDecimal> prices) {
        List<PriceResult> out = new ArrayList<>();
        for (BigDecimal price : prices) {
            Integer n = jdbc.queryForObject("select count(*) from waitlist_signup where business_id = ? and price_seen = ?",
                    Integer.class, businessId, price.toPlainString());
            out.add(new PriceResult(price.toPlainString(), n == null ? 0 : n));
        }
        return out;
    }

    /** Reads up to three prices written like "199, 249, 299"; anything that is not a number is skipped. */
    public static List<BigDecimal> parsePrices(String text) {
        List<BigDecimal> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        for (String part : text.split("[,;/\\s]+")) {
            String p = part.replaceAll("[^0-9.]", "");
            if (p.isEmpty() || out.size() >= 3) {
                continue;
            }
            try {
                BigDecimal value = new BigDecimal(p);
                BigDecimal clean = value.stripTrailingZeros();
                if (clean.scale() < 0) {
                    clean = clean.setScale(0);
                }
                if (value.signum() > 0 && value.compareTo(BigDecimal.valueOf(10_000_000)) < 0 && !out.contains(clean)) {
                    out.add(clean);
                }
            } catch (NumberFormatException ignored) {
                // skip
            }
        }
        return out;
    }

    private static String validZone(String zone) {
        try {
            return zone == null || zone.isBlank() ? "Asia/Kolkata" : ZoneId.of(zone.trim()).getId();
        } catch (DateTimeException e) {
            return "Asia/Kolkata";
        }
    }
}
