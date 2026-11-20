package com.bazaarready.geo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The languages most people read in each country, most used first, limited to languages BazaarReady
 * offers. Used only to suggest a language; the visitor always chooses. Countries where English is the
 * main language, or that are not listed, get no suggestion.
 */
final class CountryLanguages {

    private static final Map<String, List<String>> BY_COUNTRY = new HashMap<>();

    static {
        // South Asia: India has many main languages, so several are offered.
        put("hi,bn,te,mr,ta,kn,gu,ml,pa", "IN");
        put("ur,pa", "PK");
        put("bn", "BD");
        put("ne", "NP");
        put("si,ta", "LK");
        // Spanish and Portuguese.
        put("es", "ES", "MX", "AR", "CO", "PE", "VE", "CL", "EC", "GT", "CU", "BO", "DO", "HN", "PY", "SV", "NI", "CR", "PA", "UY", "PR", "GQ");
        put("pt", "BR", "PT", "AO", "MZ", "GW", "CV", "ST", "TL");
        // French, German, Italian, Dutch and the rest of Europe.
        put("fr", "FR", "MC", "SN", "CI", "ML", "BF", "NE", "GN", "BJ", "TG", "CG", "GA", "CM", "MG", "HT", "BI", "DJ", "KM", "TD", "CF");
        put("fr,nl,de", "BE");
        put("de,fr,it", "CH");
        put("fr,de", "LU");
        put("de", "DE", "AT", "LI");
        put("it", "IT", "SM", "VA");
        put("nl", "NL", "SR");
        put("pl", "PL");
        put("ro", "RO", "MD");
        put("el", "GR", "CY");
        put("cs", "CZ");
        put("hu", "HU");
        put("sv", "SE");
        put("uk,ru", "UA");
        put("ru", "RU", "BY");
        put("tr", "TR");
        put("az,ru", "AZ");
        put("kk,ru", "KZ");
        put("uz,ru", "UZ");
        // Arabic-speaking countries.
        put("ar", "SA", "AE", "EG", "IQ", "JO", "KW", "LB", "LY", "OM", "PS", "QA", "SY", "YE", "BH", "SD", "MR");
        put("ar,fr", "MA", "DZ", "TN");
        put("fa", "IR", "AF");
        put("he,ar", "IL");
        // East and Southeast Asia.
        put("zh", "CN", "TW", "HK", "MO");
        put("zh,ms,ta", "SG");
        put("ja", "JP");
        put("ko", "KR");
        put("id", "ID");
        put("ms", "MY", "BN");
        put("th", "TH");
        put("vi", "VN");
        put("fil", "PH");
        put("my", "MM");
        put("km", "KH");
        // Africa.
        put("sw", "TZ", "KE");
        put("sw,fr", "CD");
        put("am", "ET");
        put("ha,yo,ig", "NG");
        put("so", "SO");
        put("zu", "ZA");
    }

    private CountryLanguages() {
    }

    private static void put(String languages, String... countries) {
        List<String> list = List.of(languages.split(","));
        for (String country : countries) {
            BY_COUNTRY.put(country, list);
        }
    }

    /** The languages to offer for a two-letter country code; empty when there's nothing to suggest. */
    static List<String> forCountry(String country) {
        return country == null ? List.of() : BY_COUNTRY.getOrDefault(country.toUpperCase(), List.of());
    }
}
