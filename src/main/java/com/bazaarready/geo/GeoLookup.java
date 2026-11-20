package com.bazaarready.geo;

import com.maxmind.db.Reader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Finds the country of an IP address in the free DB-IP country database, kept on this server. Nothing
 * is sent to any outside service and addresses are not stored. Without the database file it finds
 * nothing, and no language is suggested.
 */
@Component
class GeoLookup implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(GeoLookup.class);

    private final Reader reader;

    GeoLookup(@Value("${bazaarready.geo.database}") String path) {
        Reader opened = null;
        File file = new File(path);
        if (file.isFile() && file.length() > 0) {
            try {
                opened = new Reader(file);
                log.info("Country database loaded: {}", opened.getMetadata().getDatabaseType());
            } catch (IOException e) {
                log.warn("Country database at {} could not be read: {}", path, e.getMessage());
            }
        } else {
            log.info("No country database at {}; languages won't be suggested by location", path);
        }
        this.reader = opened;
    }

    /** The two-letter country code for an address, if it is a public address the database knows. */
    @SuppressWarnings("unchecked")
    Optional<String> country(String address) {
        if (reader == null || address == null || address.isBlank()) {
            return Optional.empty();
        }
        try {
            // Literal addresses only: never resolve a host name.
            if (!address.matches("[0-9a-fA-F:.]+")) {
                return Optional.empty();
            }
            InetAddress ip = InetAddress.getByName(address);
            if (ip.isLoopbackAddress() || ip.isSiteLocalAddress() || ip.isLinkLocalAddress() || ip.isAnyLocalAddress()) {
                return Optional.empty();
            }
            Map<String, Object> record = reader.get(ip, Map.class);
            if (record != null && record.get("country") instanceof Map<?, ?> country
                    && country.get("iso_code") instanceof String code && code.matches("[A-Z]{2}")) {
                return Optional.of(code);
            }
        } catch (IOException | RuntimeException e) {
            log.debug("Country lookup failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
