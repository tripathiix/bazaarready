package com.bazaarready.guard;

import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import com.bazaarready.profile.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/** The "Is this collab real?" box on the owner's private page. */
@Controller
class GuardController {

    private final com.bazaarready.account.OwnerAccess profiles;
    private final MessageCheckService checks;

    GuardController(com.bazaarready.account.OwnerAccess profiles, MessageCheckService checks) {
        this.profiles = profiles;
        this.checks = checks;
    }

    @PostMapping("/owner/{ownerKey}/check")
    String check(@PathVariable UUID ownerKey, @RequestParam(defaultValue = "") String sender, @RequestParam String message) {
        ProfileResponse shop = profiles.open(ownerKey);
        String body = message.trim();
        if (body.isEmpty()) {
            return "redirect:/owner/" + ownerKey + "?check=empty#collabs";
        }
        checks.checkPasted(shop.id(), clip(sender, 200), clip(body, 6000));
        return "redirect:/owner/" + ownerKey + "#collabs";
    }

    private static String clip(String value, int max) {
        String v = value == null ? "" : value.trim();
        return v.length() > max ? v.substring(0, max) : v;
    }
}
