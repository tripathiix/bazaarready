package com.bazaarready.site;

import com.bazaarready.common.NotFoundException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** BazaarReady's own pages linked from the home page header: Templates, How it works and Features. */
@Controller
class PagesController {

    /**
     * One of the seven site designs, shown with its built-in sample site (see V21__sample_sites.sql).
     * shot is a screenshot of the sample's home page when one ships with the app; photo is the fallback.
     */
    record Design(String key, String name, String blurb, String goodFor, String group, String groupLabel,
                  String siteName, String sitePath, String shot, String photo) {
    }

    /** Filter chips on the Templates page: key and label. */
    record Group(String key, String label) {
    }

    static final List<Group> GROUPS = List.of(new Group("shops", "Shops"), new Group("food", "Food"),
            new Group("services", "Services and classes"), new Group("portfolio", "Portfolio"), new Group("premium", "Premium"));

    // key, name, blurb, good for, filter group
    private static final List<String[]> DESIGNS = List.of(
            new String[] {"classic", "Classic", "Clean and familiar, with your products front and centre.", "farms and general shops", "shops"},
            new String[] {"boutique", "Boutique", "Soft, photo-led and personal.", "food, handmade goods and fashion", "food"},
            new String[] {"mart", "Mart", "Search, categories and a big catalogue.", "shops with many products", "shops"},
            new String[] {"split", "Split", "Clear services, prices and booking side by side.", "classes, services and health", "services"},
            new String[] {"bento", "Bento", "Modern tiles that show off what you do.", "freelancers, coaches and agencies", "services"},
            new String[] {"magazine", "Magazine", "Editorial and bold, built around your story.", "creators, artists and events", "portfolio"},
            new String[] {"noir", "Noir", "Dark, premium and calm.", "salons, fine dining and jewellery", "premium"});

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
        model.addAttribute("designs", designs());
        model.addAttribute("groups", GROUPS);
        return "templates";
    }

    /** A full-size preview of one design's sample site, with a desktop and phone view. */
    @GetMapping("/templates/{key}")
    String preview(@PathVariable String key, Model model) {
        Design design = designs().stream().filter(d -> d.key().equals(key)).findFirst()
                .orElseThrow(() -> new NotFoundException("No such template"));
        if (design.sitePath() == null) {
            return "redirect:/templates";
        }
        model.addAttribute("design", design);
        return "template-preview";
    }

    private List<Design> designs() {
        List<Design> designs = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String[] d : DESIGNS) {
            List<Map<String, Object>> rows = jdbc.queryForList("""
                    select b.id, b.name, b.slug, b.hero_query from business b
                    where b.sample and b.status = 'PUBLISHED' and b.layout = ? order by b.slug limit 1
                    """, d[0]);
            String siteName = null;
            String sitePath = null;
            String photo = null;
            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                siteName = (String) row.get("name");
                sitePath = "/s/" + row.get("slug");
                var picture = images.resolve((UUID) row.get("id"), "", (String) row.get("hero_query"), true);
                if (picture.isPresent()) {
                    photo = picture.get().url();
                } else if (row.get("hero_query") != null) {
                    missing.add((String) row.get("hero_query"));
                }
            }
            String shotFile = "public/template-shots/" + d[0] + ".jpg";
            String shot = new ClassPathResource(shotFile).exists() ? "/template-shots/" + d[0] + ".jpg" : null;
            String groupLabel = GROUPS.stream().filter(g -> g.key().equals(d[4])).findFirst().map(Group::label).orElse("");
            designs.add(new Design(d[0], d[1], d[2], d[3], d[4], groupLabel, siteName, sitePath, shot, photo));
        }
        images.ensureLater(missing);
        return designs;
    }
}
