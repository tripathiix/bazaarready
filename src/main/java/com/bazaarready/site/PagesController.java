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
     * One card on the Templates page: a built-in sample site and the design it uses (see V21, V27 and V31).
     * key is the sample's slug, or the design's key for a design with no sample yet. layout is the design key
     * that "Use this design" passes on. shot is a screenshot of the sample's home page when one ships with
     * the app; photo is the fallback.
     */
    record Design(String key, String layout, String name, String blurb, String goodFor, String group, String groupLabel,
                  String siteName, String sitePath, String shot, String photo) {
    }

    /** Designs whose filter group says more than the business kind does. */
    private static final java.util.Set<String> DESIGN_GROUPS = java.util.Set.of("3d", "premium", "stays", "events", "clinics", "listings");

    /** Filter chips on the Templates page: key and label. */
    record Group(String key, String label) {
    }

    static final List<Group> GROUPS = List.of(new Group("3d", "3D"), new Group("shops", "Shops"), new Group("food", "Food"),
            new Group("services", "Services and classes"), new Group("portfolio", "Portfolio"), new Group("premium", "Premium"),
            new Group("stays", "Stays"), new Group("events", "Events"), new Group("clinics", "Clinics"), new Group("listings", "Listings"));

    // key, name, blurb, good for, filter group
    private static final List<String[]> DESIGNS = List.of(
            new String[] {"globe", "Globe", "A glossy 3D globe for a shop open to the world.", "exporters and businesses that sell abroad", "3d"},
            new String[] {"poppy", "Poppy", "Bold type, bright colours and bouncy 3D bubbles.", "cupcakes, gifts and fun brands", "3d"},
            new String[] {"gradient", "Gradient", "Soft pink-to-purple glass with a 3D spiral.", "skin care, wellness and beauty", "3d"},
            new String[] {"neon", "Neon", "Glowing signs and 3D shapes on a night-market purple.", "music, gaming and nightlife", "3d"},
            new String[] {"classic", "Classic", "Clean and familiar, with your products front and centre.", "farms and general shops", "shops"},
            new String[] {"boutique", "Boutique", "Soft, photo-led and personal.", "food, handmade goods and fashion", "food"},
            new String[] {"mart", "Mart", "Search, categories and a big catalogue.", "shops with many products", "shops"},
            new String[] {"split", "Split", "Clear services, prices and booking side by side.", "classes, services and health", "services"},
            new String[] {"bento", "Bento", "Modern tiles that show off what you do.", "freelancers, coaches and agencies", "services"},
            new String[] {"magazine", "Magazine", "Editorial and bold, built around your story.", "creators, artists and events", "portfolio"},
            new String[] {"noir", "Noir", "Dark, premium and calm.", "salons, fine dining and jewellery", "premium"},
            new String[] {"stay", "Stay", "A wide photo with a booking card resting on it.", "hotels, homestays and guest houses", "stays"},
            new String[] {"menu", "Menu", "A menu board with every dish and price.", "restaurants, cafes, bakeries and tiffin services", "food"},
            new String[] {"celebrate", "Celebrate", "An arched photo, fine lines and a graceful name.", "weddings, events and photographers", "events"},
            new String[] {"listing", "Listing", "Search first, then large property cards.", "real estate, rentals and car hire", "listings"},
            new String[] {"clinic", "Clinic", "Calm and clear, with timings and booking first.", "dentists, physios, doctors and therapists", "clinics"});

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
        List<Design> all = designs();
        // Old links used the design's key: show that design's first sample.
        Design design = all.stream().filter(d -> d.key().equals(key)).findFirst()
                .or(() -> all.stream().filter(d -> d.layout().equals(key) && d.sitePath() != null).findFirst())
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
                    select b.id, b.name, b.slug, b.kind, b.hero_query from business b
                    where b.sample and b.status = 'PUBLISHED' and b.layout = ? order by b.name
                    """, d[0]);
            String designGroup = d[4];
            if (rows.isEmpty()) {
                designs.add(new Design(d[0], d[0], d[1], d[2], d[3], designGroup, label(designGroup), null, null, shot(d[0]), null));
                continue;
            }
            for (Map<String, Object> row : rows) {
                String slug = (String) row.get("slug");
                String photo = null;
                var picture = images.resolve((UUID) row.get("id"), "", (String) row.get("hero_query"), true);
                if (picture.isPresent()) {
                    photo = picture.get().url();
                } else if (row.get("hero_query") != null) {
                    missing.add((String) row.get("hero_query"));
                }
                String group = DESIGN_GROUPS.contains(designGroup) ? designGroup : groupFor((String) row.get("kind"), designGroup);
                String shot = shot(slug);
                designs.add(new Design(slug, d[0], d[1], d[2], d[3], group, label(group), (String) row.get("name"),
                        "/s/" + slug, shot != null ? shot : rows.get(0) == row ? shot(d[0]) : null, photo));
            }
        }
        images.ensureLater(missing);
        return designs;
    }

    /** The filter group for a sample, from what kind of business it is. */
    private static String groupFor(String kind, String fallback) {
        if (kind == null) {
            return fallback;
        }
        return switch (kind) {
            case "shop" -> "shops";
            case "food" -> "food";
            case "services", "classes", "transport" -> "services";
            case "portfolio" -> "portfolio";
            case "events" -> "events";
            default -> fallback;
        };
    }

    private static String label(String group) {
        return GROUPS.stream().filter(g -> g.key().equals(group)).findFirst().map(Group::label).orElse("");
    }

    /** A screenshot that ships with the app, by sample slug or design key. */
    private static String shot(String name) {
        return new ClassPathResource("public/template-shots/" + name + ".jpg").exists() ? "/template-shots/" + name + ".jpg" : null;
    }
}
