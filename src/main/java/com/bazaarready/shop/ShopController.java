package com.bazaarready.shop;

import com.bazaarready.common.NotFoundException;
import com.bazaarready.profile.Business;
import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import com.bazaarready.profile.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The public shop page. It is plain server-rendered HTML on purpose: that is what the AI test
 * customers read and what the accessibility checks inspect.
 */
@Controller
class ShopController {

    private final ProfileService profiles;

    ShopController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping("/s/{slug}")
    String shop(@PathVariable String slug, Model model) {
        ProfileResponse shop = profiles.getBySlug(slug);
        model.addAttribute("shop", shop);
        model.addAttribute("draft", shop.status() == Business.Status.DRAFT);
        model.addAttribute("hindi", !"en".equals(shop.ownerLanguage()));
        model.addAttribute("whatsappLink", whatsappLink(shop));
        model.addAttribute("money", new Money());
        return "shop";
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String notFound() {
        return "shop-not-found";
    }

    private static String whatsappLink(ProfileResponse shop) {
        if (shop.whatsappNumber() == null) {
            return null;
        }
        String digits = shop.whatsappNumber().replaceAll("[^0-9]", "");
        if (digits.length() == 10) {
            digits = "91" + digits;
        }
        String text = URLEncoder.encode("Namaste! " + shop.name() + " se order karna hai.", StandardCharsets.UTF_8);
        return "https://wa.me/" + digits + "?text=" + text;
    }

    /** Formats rupee amounts for the page: 850 becomes "₹850", 40.5 becomes "₹40.50". */
    public static final class Money {
        public String format(BigDecimal rupees) {
            BigDecimal value = rupees.stripTrailingZeros();
            return "₹" + (value.scale() <= 0 ? value.toBigInteger().toString() : rupees.setScale(2).toPlainString());
        }
    }
}
