package com.bazaarready.site;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Photos for BazaarReady's own home page: the three example shopfronts and recently published sites. */
@RestController
class ShowcaseController {

    /** Search words for the example shopfronts, in the order the home page shows them. */
    private static final List<String> EXAMPLES = List.of("cupcakes bakery display", "students classroom teacher",
            "camera lens photographer");

    record Photo(String url, String credit, String creditUrl, String licence, String source) {
    }

    record RecentSite(String name, String tagline, String path, Photo photo, String location, java.time.Instant publishedAt) {
    }

    record Item(String name, String price, Photo photo) {
    }

    /** One real published site for the home page preview, with the result of its latest AI customer test. */
    record Featured(String name, String tagline, String path, Photo photo, List<Item> items, Integer passed, Integer total) {
    }

    record Showcase(List<Photo> examples, List<RecentSite> recent, Featured featured) {
    }

    private final JdbcTemplate jdbc;
    private final ImageService images;

    ShowcaseController(JdbcTemplate jdbc, ImageService images) {
        this.jdbc = jdbc;
        this.images = images;
    }

    @GetMapping("/api/showcase")
    Showcase showcase() {
        images.ensure(EXAMPLES);
        UUID none = new UUID(0, 0);
        List<Photo> examples = EXAMPLES.stream()
                .map(q -> images.resolve(none, null, q, true).map(ShowcaseController::photo).orElse(null))
                .toList();

        List<RecentSite> recent = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                select b.id, b.name, b.slug, b.tagline, b.hero_query, b.location, b.published_at,
                       (select p.name from product p where p.business_id = b.id order by p.sort_order limit 1) as first_name,
                       (select p.image_query from product p where p.business_id = b.id order by p.sort_order limit 1) as first_query
                from business b where b.status = 'PUBLISHED' and not b.sample order by b.published_at desc limit 24
                """);
        for (Map<String, Object> row : rows) {
            UUID id = (UUID) row.get("id");
            var picture = images.resolve(id, "", (String) row.get("hero_query"), true)
                    .or(() -> images.resolve(id, (String) row.get("first_name"), (String) row.get("first_query"), true));
            if (picture.isEmpty()) {
                // Look the photos up in the background so the site joins the gallery shortly.
                if (row.get("hero_query") != null) {
                    missing.add((String) row.get("hero_query"));
                }
                if (row.get("first_query") != null) {
                    missing.add((String) row.get("first_query"));
                }
            }
            boolean sameName = recent.stream().anyMatch(r -> r.name().equals(row.get("name")));
            if (picture.isPresent() && !sameName) {
                recent.add(new RecentSite((String) row.get("name"), (String) row.get("tagline"), "/s/" + row.get("slug"),
                        photo(picture.get()), (String) row.get("location"),
                        row.get("published_at") instanceof java.sql.Timestamp t ? t.toInstant() : null));
            }
            if (recent.size() == 6) {
                break;
            }
        }
        Featured featured = featured(missing);
        images.ensureLater(missing);
        return new Showcase(examples, recent, featured);
    }

    /**
     * The published site whose latest finished AI customer test went best (most customers got what they came for),
     * so the home page shows a real site and a real result. Built-in sample sites are used only when no
     * real site has a photo yet. Null when there is nothing with a photo.
     */
    private Featured featured(List<String> missing) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                select b.id, b.name, b.slug, b.tagline, b.hero_query, t.passed, t.total
                from business b
                left join lateral (
                    select count(*) filter (where c.status = 'PASSED')::int as passed, count(*)::int as total
                    from test_run r join test_case c on c.run_id = r.id
                    where r.id = (select r2.id from test_run r2 where r2.business_id = b.id and r2.status = 'DONE'
                                  order by r2.started_at desc limit 1)
                ) t on true
                where b.status = 'PUBLISHED'
                order by b.sample, coalesce(t.passed, -1) desc, b.published_at desc
                limit 6
                """);
        for (Map<String, Object> row : rows) {
            UUID id = (UUID) row.get("id");
            var hero = images.resolve(id, "", (String) row.get("hero_query"), true);
            if (hero.isEmpty()) {
                continue;
            }
            List<Item> items = new ArrayList<>();
            for (Map<String, Object> p : jdbc.queryForList("""
                    select name, price_paise, unit, image_query from product
                    where business_id = ? order by sort_order limit 3
                    """, id)) {
                var picture = images.resolve(id, (String) p.get("name"), (String) p.get("image_query"), true);
                if (picture.isEmpty() && p.get("image_query") != null) {
                    missing.add((String) p.get("image_query"));
                }
                items.add(new Item((String) p.get("name"), price((Number) p.get("price_paise"), (String) p.get("unit")),
                        picture.map(ShowcaseController::photo).orElse(null)));
            }
            Integer total = (Integer) row.get("total");
            boolean tested = total != null && total > 0;
            return new Featured((String) row.get("name"), (String) row.get("tagline"), "/s/" + row.get("slug"),
                    photo(hero.get()), items, tested ? (Integer) row.get("passed") : null, tested ? total : null);
        }
        return null;
    }

    private static String price(Number paise, String unit) {
        if (paise == null) {
            return null;
        }
        long p = paise.longValue();
        String amount = p % 100 == 0
                ? java.text.NumberFormat.getIntegerInstance(new java.util.Locale("en", "IN")).format(p / 100)
                : String.format("%.2f", p / 100.0);
        if (unit == null || unit.isBlank()) {
            return "₹" + amount;
        }
        return "₹" + amount + (unit.trim().equalsIgnoreCase("each") ? " each" : " / " + unit.trim());
    }

    private static Photo photo(ImageService.Picture p) {
        return new Photo(p.url(), p.photographer(), p.pageUrl(), p.licence(), p.source());
    }
}
