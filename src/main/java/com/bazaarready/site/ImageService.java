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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Pictures for generated sites. An owner's own upload always wins; otherwise a free stock photo
 * from Pexels is used, found with search words the agent chose; otherwise the page shows a
 * coloured letter tile. Searches are stored, so each one is made once.
 */
@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    /** Largest upload accepted, in bytes. */
    public static final int MAX_UPLOAD = 3 * 1024 * 1024;

    /** A picture ready for a page. Credit fields are empty for an owner's own photo. */
    public record Picture(String url, String photographer, String photographerUrl, String pageUrl) {
        public boolean isStock() {
            return photographer != null;
        }
    }

    public record Upload(String contentType, byte[] data) {
    }

    private final JdbcTemplate jdbc;
    private final RestClient pexels;
    private final String apiKey;

    ImageService(JdbcTemplate jdbc, RestClient.Builder builder, @Value("${bazaarready.pexels.api-key:}") String apiKey) {
        this.jdbc = jdbc;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(8));
        this.pexels = builder.baseUrl("https://api.pexels.com").requestFactory(factory).build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public boolean stockEnabled() {
        return !apiKey.isEmpty();
    }

    /** Looks up any searches not seen before. Called when the agent saves a profile. */
    public void ensure(Collection<String> queries) {
        if (!stockEnabled()) {
            return;
        }
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
                fetch(query);
            } catch (Exception e) {
                // Not stored, so the next save tries again.
                log.warn("Stock photo search for '{}' failed: {}", query, e.getMessage());
            }
        }
    }

    private void fetch(String query) {
        JsonNode body = pexels.get()
                .uri(uri -> uri.path("/v1/search").queryParam("query", query).queryParam("per_page", 1)
                        .queryParam("orientation", "landscape").build())
                .header("Authorization", apiKey)
                .retrieve()
                .body(JsonNode.class);
        JsonNode photo = body == null ? null : body.path("photos").path(0);
        if (photo == null || photo.isMissingNode()) {
            jdbc.update("insert into stock_photo (query, found) values (?, false) on conflict (query) do nothing", query);
            return;
        }
        jdbc.update("""
                insert into stock_photo (query, found, image_url, thumb_url, photographer, photographer_url, page_url)
                values (?, true, ?, ?, ?, ?, ?) on conflict (query) do nothing
                """,
                query, photo.path("src").path("large").asText(), photo.path("src").path("medium").asText(),
                photo.path("photographer").asText(), photo.path("photographer_url").asText(), photo.path("url").asText());
    }

    /** The picture for one offering (or, with an empty name, the banner). */
    public Optional<Picture> resolve(UUID businessId, String itemName, String query, boolean thumbnail) {
        List<UUID> uploads = jdbc.queryForList("select id from item_photo where business_id = ? and item_name = ?",
                UUID.class, businessId, itemName == null ? "" : itemName);
        if (!uploads.isEmpty()) {
            return Optional.of(new Picture("/media/" + uploads.get(0), null, null, null));
        }
        String q = normalise(query);
        if (q == null) {
            return Optional.empty();
        }
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select image_url, thumb_url, photographer, photographer_url, page_url from stock_photo where query = ? and found",
                q);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> row = rows.get(0);
        String url = (String) row.get(thumbnail ? "thumb_url" : "image_url");
        return Optional.of(new Picture(url, (String) row.get("photographer"), (String) row.get("photographer_url"),
                (String) row.get("page_url")));
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

    private static String normalise(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String q = query.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return q.length() > 80 ? q.substring(0, 80) : q;
    }
}
