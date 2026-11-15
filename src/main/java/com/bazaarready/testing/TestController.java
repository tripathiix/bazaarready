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

    private final ProfileService profiles;
    private final TestRunService tests;

    TestController(ProfileService profiles, TestRunService tests) {
        this.profiles = profiles;
        this.tests = tests;
    }

    @PostMapping("/owner/{ownerKey}/test")
    String start(@PathVariable UUID ownerKey) {
        ProfileResponse shop = profiles.getByOwnerKey(ownerKey);
        tests.start(shop.id());
        return "redirect:/owner/" + ownerKey + "#tests";
    }

    @PostMapping("/owner/{ownerKey}/test/fix/{findingId}")
    String applyFix(@PathVariable UUID ownerKey, @PathVariable UUID findingId) {
        ProfileResponse shop = profiles.getByOwnerKey(ownerKey);
        tests.applyFix(shop.id(), findingId);
        return "redirect:/owner/" + ownerKey + "#tests";
    }
}
