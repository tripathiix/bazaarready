package com.bazaarready.testing;

import com.bazaarready.profile.ProfileDtos.ProfileResponse;
import com.bazaarready.profile.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

/** Buttons on the owner's private page: start a test, and apply a suggested fix. */
@Controller
class TestController {

    private final com.bazaarready.account.OwnerAccess profiles;
    private final TestRunService tests;

    TestController(com.bazaarready.account.OwnerAccess profiles, TestRunService tests) {
        this.profiles = profiles;
        this.tests = tests;
    }

    @PostMapping("/owner/{ownerKey}/test")
    String start(@PathVariable UUID ownerKey,
                 @org.springframework.web.bind.annotation.RequestParam(defaultValue = "false") boolean deep) {
        ProfileResponse shop = profiles.open(ownerKey);
        tests.start(shop.id(), deep);
        return "redirect:/owner/" + ownerKey + "#tests";
    }

    /** The changes the AI customers' results suggest, for the "Change my website" box. */
    @org.springframework.web.bind.annotation.GetMapping("/api/owner/{ownerKey}/test-fix")
    @org.springframework.web.bind.annotation.ResponseBody
    java.util.Map<String, String> fixRequest(@PathVariable UUID ownerKey) {
        ProfileResponse shop = profiles.open(ownerKey);
        return java.util.Map.of("text", tests.fixRequest(shop.id()));
    }

    @PostMapping("/owner/{ownerKey}/test/fix/{findingId}")
    String applyFix(@PathVariable UUID ownerKey, @PathVariable UUID findingId) {
        ProfileResponse shop = profiles.open(ownerKey);
        try {
            tests.applyFix(shop.id(), findingId);
        } catch (com.bazaarready.common.NotFoundException e) {
            // Already applied, or not this owner's: nothing to do, just show the page again.
        }
        return "redirect:/owner/" + ownerKey + "#tests";
    }
}
