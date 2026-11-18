package com.bazaarready.common;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The languages BazaarReady works in. The ones listed here have hand-checked builder screens; any other
 * language with a standard two- or three-letter code also works, with its screens translated by Claude.
 * Codes are used in the API, the database and the site pages.
 */
public final class Languages {

    /** Regular expression of every accepted language code, for request validation. */
    public static final String CODE_PATTERN = "hinglish|[a-z]{2,3}";

    public static final String DEFAULT = "en";

    public record Language(String code, String englishName, String script, String htmlLang) {
    }

    private static final Map<String, Language> ALL = new LinkedHashMap<>();

    static {
        add("en", "English", "Latin", "en");
        add("hi", "Hindi", "Devanagari", "hi");
        add("hinglish", "Hinglish (Hindi written in English letters)", "Latin", "hi-Latn");
        add("kn", "Kannada", "Kannada", "kn");
        add("ta", "Tamil", "Tamil", "ta");
        add("te", "Telugu", "Telugu", "te");
        add("mr", "Marathi", "Devanagari", "mr");
        add("bn", "Bengali", "Bengali", "bn");
        add("gu", "Gujarati", "Gujarati", "gu");
    }

    private Languages() {
    }

    private static void add(String code, String englishName, String script, String htmlLang) {
        ALL.put(code, new Language(code, englishName, script, htmlLang));
    }

    /**
     * Returns the language for a code. Codes outside the hand-checked list are named from the standard
     * locale data ("sw" is Swahili); unknown or malformed codes give English.
     */
    public static Language of(String code) {
        if (code == null) {
            return ALL.get(DEFAULT);
        }
        Language known = ALL.get(code);
        if (known != null) {
            return known;
        }
        if (code.matches("[a-z]{2,3}")) {
            String name = Locale.forLanguageTag(code).getDisplayLanguage(Locale.ENGLISH);
            if (!name.isBlank() && !name.equalsIgnoreCase(code)) {
                return new Language(code, name, "its usual", code);
            }
        }
        return ALL.get(DEFAULT);
    }
}
