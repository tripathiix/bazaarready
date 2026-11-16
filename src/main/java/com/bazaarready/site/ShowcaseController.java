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

    record RecentSite(String name, String tagline, String path, Photo photo) {
    }

    record Showcase(List<Photo> examples, List<RecentSite> recent) {
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
                select b.id, b.name, b.slug, b.tagline, b.hero_query,
                       (select p.name from product p where p.business_id = b.id order by p.sort_order limit 1) as first_name,
                       (select p.image_query from product p where p.business_id = b.id order by p.sort_order limit 1) as first_query
                from business b where b.status = 'PUBLISHED' order by b.published_at desc limit 24
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
                        photo(picture.get())));
            }
            if (recent.size() == 6) {
                break;
            }
        }
        images.ensureLater(missing);
        return new Showcase(examples, recent);
    }

    private static Photo photo(ImageService.Picture p) {
        return new Photo(p.url(), p.photographer(), p.pageUrl(), p.licence(), p.source());
    }
}
