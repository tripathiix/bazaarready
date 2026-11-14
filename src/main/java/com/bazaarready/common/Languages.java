package com.bazaarready.common;

import java.util.LinkedHashMap;
import java.util.Map;

/** The languages BazaarReady supports. Codes are used in the API, the database and the shop page. */
public final class Languages {

    /** Regular expression of every accepted language code, for request validation. */
    public static final String CODE_PATTERN = "en|hi|hinglish|kn|ta|te|mr|bn|gu";

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

    /** Returns the language for a code, or English when the code is missing or unknown. */
    public static Language of(String code) {
        return ALL.getOrDefault(code == null ? DEFAULT : code, ALL.get(DEFAULT));
    }
}
