package com.bazaarready.extras;

import com.bazaarready.common.NotFoundException;
import com.bazaarready.profile.ProfileDtos.ProductResponse;
import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import com.bazaarready.profile.ProfileService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The small JSON endpoints a generated site's own script calls: the shutter's live state, haggling,
 * and the two-language chat. Shoppers are anonymous; rate limits and the daily AI cap still apply.
 */
@RestController
class ShopperController {

    record HaggleRequest(UUID productId, Integer quantity, List<HaggleService.Turn> history, String message) {
    }

    record ChatStart(String name, String language) {
    }

    record ChatSend(String text) {
    }

    private final ProfileService profiles;
    private final ExtrasService extras;
    private final HaggleService haggle;
    private final ChatService chat;

    ShopperController(ProfileService profiles, ExtrasService extras, HaggleService haggle, ChatService chat) {
        this.profiles = profiles;
        this.extras = extras;
        this.haggle = haggle;
        this.chat = chat;
    }

    /** Polled every so often by open pages, so the shutter comes down live when the owner closes. */
    @GetMapping("/s/{slug}/status")
    ResponseEntity<Map<String, Object>> status(@PathVariable String slug) {
        ProfileResponse shop = profiles.getBySlug(slug);
        ExtrasService.Extras ex = extras.get(shop.id());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("open", ex.openNow());
        body.put("note", ex.closedNote());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }

    @PostMapping("/s/{slug}/haggle")
    ResponseEntity<Map<String, Object>> haggle(@PathVariable String slug, @RequestBody HaggleRequest request) {
        ProfileResponse shop = profiles.getBySlug(slug);
        ExtrasService.Extras ex = extras.get(shop.id());
        if (!ex.haggleOn()) {
            return error("This shop doesn't bargain online.");
        }
        if (request.productId() == null || blank(request.message())) {
            return error("Write your offer first.");
        }
        ProductResponse product = shop.products().stream().filter(p -> p.id().equals(request.productId()))
                .findFirst().orElseThrow(() -> new NotFoundException("No such item"));
        if (product.priceRupees() == null) {
            return error("This item has no fixed price to bargain on.");
        }
        List<HaggleService.Turn> history = request.history() == null ? List.of() : request.history();
        HaggleService.Reply reply = haggle.haggle(shop, ex, product, request.quantity() == null ? 1 : request.quantity(),
                history, request.message());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reply", reply.text());
        body.put("offer", com.bazaarready.profile.ProfileService.toUnits(reply.offerMinor()));
        body.put("quantity", reply.quantity());
        if (reply.dealId() != null) {
            body.put("dealId", reply.dealId().toString());
            body.put("name", product.name());
        }
        return ResponseEntity.ok(body);
    }

    @PostMapping("/s/{slug}/chat")
    ResponseEntity<Map<String, Object>> startChat(@PathVariable String slug, @RequestBody ChatStart request) {
        ProfileResponse shop = profiles.getBySlug(slug);
        if (profiles.isSample(shop.id())) {
            return error("This is a sample site, so chat is switched off here.");
        }
        String name = clip(request.name(), 80);
        ChatService.Thread thread = chat.start(shop.id(), blank(name) ? null : name, request.language());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("token", thread.token().toString());
        body.put("language", thread.shopperLanguage());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/s/{slug}/chat/{token}")
    ResponseEntity<Map<String, Object>> readChat(@PathVariable String slug, @PathVariable UUID token,
                                                 @RequestParam(defaultValue = "0") long after) {
        ProfileResponse shop = profiles.getBySlug(slug);
        ChatService.Thread thread = chat.byToken(shop.id(), token);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("messages", chat.messages(thread, after, false)));
    }

    @PostMapping("/s/{slug}/chat/{token}")
    ResponseEntity<Map<String, Object>> sendChat(@PathVariable String slug, @PathVariable UUID token,
                                                 @RequestBody ChatSend request) {
        ProfileResponse shop = profiles.getBySlug(slug);
        ChatService.Thread thread = chat.byToken(shop.id(), token);
        String text = clip(request.text(), 1000);
        if (blank(text)) {
            return error("Write a message first.");
        }
        try {
            chat.fromShopper(thread, shop.ownerLanguage(), text);
        } catch (IllegalStateException e) {
            return error(e.getMessage());
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** The daily AI cap, or Claude being unreachable: the page shows the message instead of failing. */
    @org.springframework.web.bind.annotation.ExceptionHandler(com.bazaarready.claude.ClaudeException.class)
    ResponseEntity<Map<String, Object>> claudeDown(com.bazaarready.claude.ClaudeException e) {
        return ResponseEntity.status(503).body(Map.of("error", "The shop's assistant is resting right now. Please try again in a little while."));
    }

    private static ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String clip(String s, int max) {
        if (s == null) {
            return null;
        }
        String t = s.strip();
        return t.length() > max ? t.substring(0, max) : t;
    }
}
