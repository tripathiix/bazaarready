package com.bazaarready.extras;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Estimated costs of moving a site from bazaarready.com to the owner's own domain and server, for the
 * "Your own domain" tab on the owner dashboard. These are estimates, not quotes: registrars change prices
 * with every promotion, so the page always says so and links to GoDaddy for the real checkout price.
 *
 * <p>Checked November 2026 against GoDaddy's pricing pages and recent reviews:
 * .com about US$11.99 the first year and US$22.99 a year after, plus a US$0.20 ICANN fee; optional
 * domain privacy about US$9.99 a year; GoDaddy VPS 1 vCPU / 2 GB about US$8.99 a month on a 3-year term,
 * rising about 40% on renewal; Titan / Professional Email about US$1.99 per mailbox a month for the first
 * year. In India, GoDaddy sells .com for about ₹999 the first year and ₹1,599 after, and .in for about
 * ₹299 and ₹899, with 18% GST on top.
 *
 * <p>Other currencies are converted from US dollars at approximate rates, and local taxes are not included.
 */
public final class DomainPrices {

    /** When the numbers above were last checked. Shown on the page. */
    public static final String CHECKED = "November 2026";

    private DomainPrices() {
    }

    /** Everything the dashboard needs, as plain maps that serialise to JSON. */
    public static Map<String, Object> table() {
        Map<String, Object> usd = new LinkedHashMap<>();
        usd.put("comFirst", 11.99);
        usd.put("comRenew", 22.99);
        usd.put("icann", 0.20);
        usd.put("privacy", 9.99);
        usd.put("serverFirst", 8.99);
        usd.put("serverRenew", 12.99);
        usd.put("mailbox", 1.99);

        // India has its own GoDaddy prices in rupees, a country domain (.in) and 18% GST.
        Map<String, Object> inr = new LinkedHashMap<>();
        inr.put("comFirst", 999);
        inr.put("comRenew", 1599);
        inr.put("localFirst", 299);
        inr.put("localRenew", 899);
        inr.put("icann", 18);
        inr.put("privacy", 879);
        inr.put("serverFirst", 791);
        inr.put("serverRenew", 1143);
        inr.put("mailbox", 175);

        Map<String, Object> table = new LinkedHashMap<>();
        table.put("checked", CHECKED);
        table.put("usd", usd);
        table.put("india", inr);
        table.put("currencies", List.of(
                currency("INR", "₹", "India", 88, 0, 18, ".in"),
                currency("USD", "$", "United States", 1, 2, 0, null),
                currency("EUR", "€", "Europe", 0.92, 2, 0, null),
                currency("GBP", "£", "United Kingdom", 0.79, 2, 0, null),
                currency("AED", "AED ", "United Arab Emirates", 3.67, 2, 0, null),
                currency("SGD", "S$", "Singapore", 1.34, 2, 0, null),
                currency("CAD", "C$", "Canada", 1.38, 2, 0, null),
                currency("AUD", "A$", "Australia", 1.52, 2, 0, null),
                currency("KES", "KSh ", "Kenya", 129, 0, 0, null)));
        return table;
    }

    private static Map<String, Object> currency(String code, String symbol, String place, double perUsd, int decimals, int taxPercent, String local) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("code", code);
        c.put("symbol", symbol);
        c.put("place", place);
        c.put("perUsd", perUsd);
        c.put("decimals", decimals);
        c.put("tax", taxPercent);
        c.put("local", local);
        return c;
    }

    /** The shop's own currency, as one of the codes above (US dollars when unsure). */
    public static String codeFor(String shopCurrency) {
        if (shopCurrency == null) {
            return "USD";
        }
        return switch (shopCurrency.trim()) {
            case "₹", "Rs", "INR" -> "INR";
            case "€", "EUR" -> "EUR";
            case "£", "GBP" -> "GBP";
            case "AED", "د.إ" -> "AED";
            case "S$", "SGD" -> "SGD";
            case "C$", "CAD" -> "CAD";
            case "A$", "AUD" -> "AUD";
            case "KSh", "KES" -> "KES";
            default -> "USD";
        };
    }
}
