package com.bazaarready.site;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Pictures for generated sites. An owner's own upload always wins; otherwise a free stock photo is
 * used, found with search words the agent chose; otherwise the page shows a coloured letter tile.
 * Stock photos come from Pexels when a key is set, and from Wikimedia Commons (no key needed)
 * otherwise. Each search is made once and stored.
 */
@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    /** Largest upload accepted, in bytes. */
    public static final int MAX_UPLOAD = 3 * 1024 * 1024;

    /** Wikimedia asks automated clients to identify themselves. */
    private static final String USER_AGENT = "BazaarReady/0.1 (hackathon prototype; https://github.com/tripathiix/bazaarready)";

    /**
     * Licences a Commons photo may have to be used here: CC0, public domain, or plain CC BY (any
     * version). CC BY-NC is excluded because sites are commercial; BY-SA and BY-ND are excluded too.
     */
    private static final java.util.regex.Pattern ALLOWED_LICENCE = java.util.regex.Pattern.compile(
            "cc0( 1\\.0)?|public domain|pd(-[a-z0-9.-]+)?|cc[ -]by( [0-9.]+)?", java.util.regex.Pattern.CASE_INSENSITIVE);

    /** A picture ready for a page. Credit fields are empty for an owner's own photo. */
    public record Picture(String url, String photographer, String photographerUrl, String pageUrl,
                          String source, String licence, String licenceUrl) {
        public boolean isStock() {
            return source != null;
        }
    }

    public record Upload(String contentType, byte[] data) {
    }

    private record Found(String imageUrl, String thumbUrl, String photographer, String photographerUrl, String pageUrl,
                         String source, String licence, String licenceUrl) {
    }

    private final JdbcTemplate jdbc;
    private final RestClient pexels;
    private final RestClient commons;
    private final String pexelsKey;

    ImageService(JdbcTemplate jdbc, RestClient.Builder builder, @Value("${bazaarready.pexels.api-key:}") String pexelsKey) {
        this.jdbc = jdbc;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.pexels = builder.clone().baseUrl("https://api.pexels.com").requestFactory(factory).build();
        this.commons = builder.clone().baseUrl("https://commons.wikimedia.org").requestFactory(factory)
                .defaultHeader("User-Agent", USER_AGENT).build();
        this.pexelsKey = pexelsKey == null ? "" : pexelsKey.trim();
    }

    /** Looks up any searches not seen before. Called when the agent saves a profile. */
    public void ensure(Collection<String> queries) {
        Set<String> wanted = new LinkedHashSet<>();
        for (String query : queries) {
            String q = normalise(query);
            if (q != null) {
                wanted.add(q);
            }
        }
        for (String query : wanted) {
            Integer known = jdbc.queryForObject("select count(*) from stock_photo where query = ?", Integer.class, query);
            if (known != null && known > 0) {
                continue;
            }
            try {
                Optional<Found> found = pexelsKey.isEmpty() ? searchCommons(query) : searchPexels(query);
                store(query, found.orElse(null));
            } catch (Exception e) {
                // Not stored, so the next save tries again.
                log.warn("Stock photo search for '{}' failed: {}", query, e.getMessage());
            }
        }
    }

    private Optional<Found> searchPexels(String query) {
        JsonNode body = pexels.get()
                .uri(uri -> uri.path("/v1/search").queryParam("query", query).queryParam("per_page", 1)
                        .queryParam("orientation", "landscape").build())
                .header("Authorization", pexelsKey)
                .retrieve()
                .body(JsonNode.class);
        JsonNode photo = body == null ? null : body.path("photos").path(0);
        if (photo == null || photo.isMissingNode()) {
            return Optional.empty();
        }
        return Optional.of(new Found(photo.path("src").path("large").asText(), photo.path("src").path("medium").asText(),
                photo.path("photographer").asText(), photo.path("photographer_url").asText(), photo.path("url").asText(),
                "Pexels", null, null));
    }

    /** Searches Wikimedia Commons for a landscape photo under a free licence that allows commercial use. */
    private Optional<Found> searchCommons(String query) {
        JsonNode body = commons.get()
                .uri(uri -> uri.path("/w/api.php")
                        .queryParam("action", "query").queryParam("format", "json")
                        .queryParam("generator", "search").queryParam("gsrnamespace", 6).queryParam("gsrlimit", 12)
                        .queryParam("gsrsearch", query + " filetype:bitmap")
                        .queryParam("prop", "imageinfo").queryParam("iiprop", "url|size|mime|extmetadata")
                        .queryParam("iiurlwidth", 1200)
                        .build())
                .retrieve()
                .body(JsonNode.class);
        JsonNode pages = body == null ? null : body.path("query").path("pages");
        if (pages == null || !pages.isObject()) {
            return Optional.empty();
        }
        List<JsonNode> ranked = new ArrayList<>();
        pages.forEach(ranked::add);
        ranked.sort(Comparator.comparingInt(page -> page.path("index").asInt(Integer.MAX_VALUE)));

        for (JsonNode page : ranked) {
            JsonNode info = page.path("imageinfo").path(0);
            JsonNode meta = info.path("extmetadata");
            String mime = info.path("mime").asText();
            int width = info.path("width").asInt();
            int height = info.path("height").asInt();
            String licence = meta.path("LicenseShortName").path("value").asText("").trim();
            boolean nonFree = "true".equalsIgnoreCase(meta.path("NonFree").path("value").asText(""));
            boolean free = ALLOWED_LICENCE.matcher(licence).matches();
            boolean landscape = width >= 900 && width >= height * 1.15;
            if (nonFree || !free || !landscape || !(mime.equals("image/jpeg") || mime.equals("image/png"))) {
                continue;
            }
            String url = info.path("thumburl").asText(info.path("url").asText());
            String artist = plainText(meta.path("Artist").path("value").asText(""));
            return Optional.of(new Found(url, url, artist.isEmpty() ? "Unknown author" : artist, null,
                    info.path("descriptionurl").asText(), "Wikimedia Commons", licence,
                    meta.path("LicenseUrl").path("value").asText(null)));
        }
        return Optional.empty();
    }

    private void store(String query, Found found) {
        if (found == null) {
            jdbc.update("insert into stock_photo (query, found) values (?, false) on conflict (query) do nothing", query);
            return;
        }
        jdbc.update("""
                insert into stock_photo (query, found, image_url, thumb_url, photographer, photographer_url, page_url,
                                         source, licence, licence_url)
                values (?, true, ?, ?, ?, ?, ?, ?, ?, ?) on conflict (query) do nothing
                """,
                query, found.imageUrl(), found.thumbUrl(), found.photographer(), found.photographerUrl(), found.pageUrl(),
                found.source(), found.licence(), found.licenceUrl());
    }

    /** The picture for one offering (or, with an empty name, the banner). */
    public Optional<Picture> resolve(UUID businessId, String itemName, String query, boolean thumbnail) {
        List<UUID> uploads = jdbc.queryForList("select id from item_photo where business_id = ? and item_name = ?",
                UUID.class, businessId, itemName == null ? "" : itemName);
        if (!uploads.isEmpty()) {
            return Optional.of(new Picture("/media/" + uploads.get(0), null, null, null, null, null, null));
        }
        String q = normalise(query);
        if (q == null) {
            return Optional.empty();
        }
        List<Map<String, Object>> rows = jdbc.queryForList("""
                select image_url, thumb_url, photographer, photographer_url, page_url, source, licence, licence_url
                from stock_photo where query = ? and found
                """, q);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> row = rows.get(0);
        String url = (String) row.get(thumbnail ? "thumb_url" : "image_url");
        return Optional.of(new Picture(url, (String) row.get("photographer"), (String) row.get("photographer_url"),
                (String) row.get("page_url"), (String) row.get("source"), (String) row.get("licence"),
                (String) row.get("licence_url")));
    }

    /** Stores an owner's photo for an offering, replacing any earlier one. */
    public void saveUpload(UUID businessId, String itemName, String contentType, byte[] data) {
        jdbc.update("""
                insert into item_photo (id, business_id, item_name, content_type, data) values (?, ?, ?, ?, ?)
                on conflict (business_id, item_name)
                do update set id = excluded.id, content_type = excluded.content_type, data = excluded.data, created_at = now()
                """, UUID.randomUUID(), businessId, itemName == null ? "" : itemName, contentType, data);
    }

    public void removeUpload(UUID businessId, String itemName) {
        jdbc.update("delete from item_photo where business_id = ? and item_name = ?", businessId, itemName == null ? "" : itemName);
    }

    public Optional<Upload> upload(UUID id) {
        return jdbc.query("select content_type, data from item_photo where id = ?",
                rs -> rs.next() ? Optional.of(new Upload(rs.getString(1), rs.getBytes(2))) : Optional.empty(), id);
    }

    /**
     * The image type, read from the file's first bytes rather than trusting the browser, or
     * null if the file is not a JPEG, PNG or WebP picture.
     */
    public static String detectType(byte[] data) {
        if (data.length > 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (data.length > 8 && (data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "image/png";
        }
        if (data.length > 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    /** Commons stores the author as HTML; keep only the readable text. */
    private static String plainText(String html) {
        String text = html.replaceAll("<[^>]+>", " ").replace("&amp;", "&").replace("&nbsp;", " ")
                .replaceAll("\\s+", " ").trim();
        return text.length() > 60 ? text.substring(0, 57) + "..." : text;
    }

    private static String normalise(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String q = query.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return q.length() > 80 ? q.substring(0, 80) : q;
    }
}
