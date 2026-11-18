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
        put("delivery_date", "Preferred delivery date (optional)");
        put("err_required", "Please fill this in.");
        put("err_phone", "Enter a phone number with at least 6 digits.");
        put("err_date", "Choose a date from today onwards.");
        put("check_below", "Some details need fixing. See the highlighted fields.");
        put("whatsapp_number", "WhatsApp number");
        put("search_placeholder", "What are you looking for?");
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
        // Living shutter
        put("closed_sign", "Closed");
        put("closed_title", "We're closed right now");
        put("closed_preorder", "You can still look around and leave a pre-order. We'll confirm when we open.");
        put("browse_anyway", "Look around anyway");
        put("closed_bar", "Closed now. Orders placed now are pre-orders.");
        put("reopened", "We're open again!");
        // Haggle mode
        put("make_offer", "Make an offer");
        put("haggle_title", "Let's bargain");
        put("haggle_hint", "Say your price, like at a real market. Write in any language.");
        put("haggle_placeholder", "Can you do it for less?");
        put("haggle_quantity", "How many?");
        put("send", "Send");
        put("deal_done", "Deal! This price is saved in your cart for 30 minutes.");
        put("bargained", "bargained");
        put("go_to_cart", "Go to cart");
        put("close", "Close");
        put("thinking", "Thinking…");
        // Two-language chat
        put("chat_open", "Chat with us");
        put("chat_hint", "Write in your own language. We translate both ways, so the owner reads it in theirs.");
        put("chat_language", "Your language");
        put("chat_name", "Your name (optional)");
        put("chat_start", "Start chat");
        put("chat_placeholder", "Type a message");
        put("chat_waiting", "Sent. The reply will appear here. You can close this and come back later.");
        put("see_original", "Original");
        // Creator media kit
        put("nav_media_kit", "Media kit");
        put("kit_title", "Work with me");
        put("kit_audience", "My audience");
        put("kit_followers", "Followers");
        put("kit_views", "Average views");
        put("kit_rates", "Rates");
        put("collab_title", "Send a collab request");
        put("brand", "Brand or company");
        put("budget", "Budget (optional)");
        put("collab_message", "What do you have in mind?");
        // Coming soon
        put("soon_badge", "Coming soon");
        put("waitlist_title", "Be the first to know");
        put("waitlist_lead", "We're launching soon. Join the waitlist for first access.");
        put("launch_price", "Launch price");
        put("join", "Join the waitlist");
        put("nav_waitlist", "Join waitlist");
        put("waitlist_done", "You're on the list! We'll tell you first when we launch.");
    }

    private final Map<String, String> values;

    SiteLabels(Map<String, String> values) {
        this.values = values;
    }

    private static void put(String key, String english) {
        ENGLISH.put(key, english);
    }

    /** Whether every built-in label has a translation; false after new labels are added. */
    boolean complete() {
        return values.keySet().containsAll(ENGLISH.keySet());
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
