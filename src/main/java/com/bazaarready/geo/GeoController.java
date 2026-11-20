package com.bazaarready.geo;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Suggests languages for a visitor from the country of their address, so pages can offer them, for
 * example Hindi, Tamil and Kannada to a visitor in India. Pages only offer; nothing switches by itself.
 * The address comes from Tomcat's RemoteIpValve behind the proxy; it is looked up and not kept.
 */
@RestController
class GeoController {

    private final GeoLookup lookup;

    GeoController(GeoLookup lookup) {
        this.lookup = lookup;
    }

    @GetMapping("/api/geo/languages")
    ResponseEntity<Map<String, Object>> languages(HttpServletRequest request, @RequestParam(required = false) String ip) {
        String address = request.getRemoteAddr();
        // For testing on a laptop, where every visitor is local: ?ip= is honoured only for local requests,
        // never for real visitors coming through the proxy.
        if (ip != null && isLocal(address)) {
            address = ip;
        }
        String country = lookup.country(address).orElse(null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("country", country);
        body.put("languages", country == null ? List.of() : CountryLanguages.forCountry(country));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore().cachePrivate()).body(body);
    }

    private static boolean isLocal(String address) {
        try {
            if (address == null || !address.matches("[0-9a-fA-F:.]+")) {
                return false;
            }
            InetAddress ip = InetAddress.getByName(address);
            return ip.isLoopbackAddress() || ip.isSiteLocalAddress();
        } catch (Exception e) {
            return false;
        }
    }
}
