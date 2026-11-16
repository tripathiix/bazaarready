package com.bazaarready.site;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** "How BazaarReady works": a plain guide to every feature, linked from the home page. */
@Controller
class GuideController {

    @GetMapping("/guide")
    String guide() {
        return "guide";
    }
}
