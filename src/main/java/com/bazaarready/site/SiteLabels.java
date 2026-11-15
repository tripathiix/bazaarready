package com.bazaarready.site;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The fixed words on a generated site: buttons, headings, form labels. English is built in.
 * For any other language Claude translates this set once and the result is stored, so a site can
 * be in any language without hand-written translations. The business's own content is written
 * by the agent.
 */
public final class SiteLabels {

    static final Map<String, String> ENGLISH = new LinkedHashMap<>();

    static {
        put("nav_home", "Home");
        put("nav_about", "About");
        put("nav_contact", "Contact");
        put("nav_cart", "Cart");
        put("nav_book", "Book");
        put("draft_banner", "Preview. This site is not published yet.");
        put("offerings", "What we offer");
        put("about_title", "About us");
        put("details_title", "Good to know");
        put("order_ahead", "Please give {days} days' notice.");
        put("hours", "Timings");
        put("location", "Where we are");
        put("faq", "Common questions");
        put("contact_title", "Get in touch");
        put("whatsapp_button", "Message on WhatsApp");
        put("whatsapp_message", "Hello! I found {name} online and would like to know more.");
        put("search", "Search");
        put("all", "All");
        put("view", "View details");
        put("view_all", "See everything");
        put("nothing_found", "Nothing matches your search.");
        put("add_to_cart", "Add to cart");
        put("added", "Added");
        put("price_on_request", "Price on request");
        put("cart_title", "Your cart");
        put("cart_empty", "Your cart is empty.");
        put("quantity", "Quantity");
        put("remove", "Remove");
        put("total", "Total");
        put("continue_shopping", "Keep shopping");
        put("your_details", "Your details");
        put("name", "Your name");
        put("phone", "Phone number");
        put("address", "Delivery address");
        put("note", "Note (optional)");
        put("checkout", "Checkout");
        put("fill_all", "Please fill in your name, phone number and address.");
        put("pay_title", "Payment");
        put("pay_demo", "This is a demo payment page. No money will be taken and no card details are sent.");
        put("pay_card", "Card number");
        put("pay_now", "Pay {amount}");
        put("pay_later", "Pay on delivery");
        put("order_done", "Order received");
        put("booking_done", "Booking request sent");
        put("thanks", "Thank you! {name} will contact you to confirm.");
        put("order_number", "Reference number");
        put("paid_demo", "Paid (demo)");
        put("pay_on_delivery", "Payment on delivery");
        put("book_title", "Request a booking");
        put("book_now", "Book");
        put("choose", "Choose");
        put("date", "Date");
        put("time", "Preferred time");
        put("from", "From");
        put("to", "To");
        put("send_request", "Send request");
        put("your_contact", "Phone or email");
        put("message", "Your message");
        put("send_message", "Send message");
        put("message_done", "Message sent. Thank you!");
        put("back_home", "Back to home");
    }

    private final Map<String, String> values;

    SiteLabels(Map<String, String> values) {
        this.values = values;
    }

    private static void put(String key, String english) {
        ENGLISH.put(key, english);
    }

    /** The label for a key, falling back to English and then to the key itself. */
    public String of(String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            value = ENGLISH.getOrDefault(key, key);
        }
        return value;
    }

    /** The label with one placeholder filled in, for example of("pay_now", "amount", "₹850"). */
    public String of(String key, String placeholder, Object replacement) {
        return of(key).replace("{" + placeholder + "}", String.valueOf(replacement));
    }
}
