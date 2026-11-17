package com.bazaarready.site;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** BazaarReady's own pages linked from the home page header: Templates, How it works and Features. */
@Controller
class PagesController {

    /** One of the seven site designs, with a real published site that uses it when there is one. */
    record Design(String key, String name, String blurb, String goodFor, String siteName, String sitePath, String photo) {
    }

    private static final List<String[]> DESIGNS = List.of(
            new String[] {"classic", "Classic", "Clean and familiar, with your products front and centre.", "farms and general shops"},
            new String[] {"boutique", "Boutique", "Soft, photo-led and personal.", "food, handmade goods and fashion"},
            new String[] {"mart", "Mart", "Search, categories and a big catalogue.", "shops with many products"},
            new String[] {"split", "Split", "Clear services, prices and booking side by side.", "classes, services and health"},
            new String[] {"bento", "Bento", "Modern tiles that show off what you do.", "freelancers, coaches and agencies"},
            new String[] {"magazine", "Magazine", "Editorial and bold, built around your story.", "creators, artists and events"},
            new String[] {"noir", "Noir", "Dark, premium and calm.", "salons, fine dining and jewellery"});

    private final JdbcTemplate jdbc;
    private final ImageService images;

    PagesController(JdbcTemplate jdbc, ImageService images) {
        this.jdbc = jdbc;
        this.images = images;
    }

    @GetMapping("/guide")
    String guide() {
        return "guide";
    }

    @GetMapping("/features")
    String features() {
        return "features";
    }

    @GetMapping("/templates")
    String templates(Model model) {
        List<Design> designs = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String[] d : DESIGNS) {
            String siteName = null;
            String sitePath = null;
            String photo = null;
            // The most recently published sites in this design; the first one with a photo is the preview.
            for (Map<String, Object> row : jdbc.queryForList("""
                    select b.id, b.name, b.slug, b.hero_query,
                           (select p.name from product p where p.business_id = b.id order by p.sort_order limit 1) as first_name,
                           (select p.image_query from product p where p.business_id = b.id order by p.sort_order limit 1) as first_query
                    from business b
                    where b.status = 'PUBLISHED' and b.layout = ? order by b.published_at desc limit 5
                    """, d[0])) {
                if (sitePath == null) {
                    siteName = (String) row.get("name");
                    sitePath = "/s/" + row.get("slug");
                }
                UUID id = (UUID) row.get("id");
                // The site's main photo, or else the photo of its first product (as the home page gallery does).
                var picture = images.resolve(id, "", (String) row.get("hero_query"), true)
                        .or(() -> images.resolve(id, (String) row.get("first_name"), (String) row.get("first_query"), true));
                if (picture.isPresent()) {
                    siteName = (String) row.get("name");
                    sitePath = "/s/" + row.get("slug");
                    photo = picture.get().url();
                    break;
                }
                if (row.get("hero_query") != null) {
                    missing.add((String) row.get("hero_query"));
                }
                if (row.get("first_query") != null) {
                    missing.add((String) row.get("first_query"));
                }
            }
            designs.add(new Design(d[0], d[1], d[2], d[3], siteName, sitePath, photo));
        }
        images.ensureLater(missing);
        model.addAttribute("designs", designs);
        return "templates";
    }
}
