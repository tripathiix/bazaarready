package com.bazaarready.support;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** BazaarReady's own contact page. Messages are saved and emailed to the support mailbox (see {@link SupportMailer}). */
@Controller
class SupportController {

    static final List<String> TOPICS = List.of("General question", "Help with my website", "Report a problem", "Own domain and hosting", "Partnership or press");

    private final SupportMailer mailer;

    SupportController(SupportMailer mailer) {
        this.mailer = mailer;
    }

    @GetMapping("/contact")
    String page(@RequestParam(required = false) String topic, @RequestParam(required = false) String message, Model model) {
        model.addAttribute("topics", TOPICS);
        // A link can pick the topic and start the message, e.g. "Request my own domain" on the dashboard.
        if (topic != null && TOPICS.contains(topic)) {
            model.addAttribute("topic", topic);
        }
        if (message != null && !message.isBlank()) {
            model.addAttribute("message", message.length() > 1500 ? message.substring(0, 1500) : message);
        }
        return "contact-us";
    }

    @PostMapping("/contact")
    String send(@RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "") String email,
                @RequestParam(defaultValue = "") String topic, @RequestParam(defaultValue = "") String message,
                @RequestParam(defaultValue = "") String website, Model model) {
        // "website" is a honeypot: hidden from people, but bots fill in every field. Pretend it worked.
        if (!website.isBlank()) {
            return "redirect:/contact?sent";
        }
        String cleanName = oneLine(name, 100);
        String cleanEmail = oneLine(email, 200);
        String cleanTopic = TOPICS.contains(topic) ? topic : TOPICS.get(0);
        String cleanMessage = message.strip();

        String error = null;
        if (cleanName.isEmpty()) {
            error = "Please tell us your name.";
        } else if (!validEmail(cleanEmail)) {
            error = "Please check your email address, so we can reply.";
        } else if (cleanMessage.length() < 10) {
            error = "Please write a little more, so we can help.";
        } else if (cleanMessage.length() > 5000) {
            error = "Please keep your message under 5,000 characters.";
        }
        if (error != null) {
            model.addAttribute("topics", TOPICS);
            model.addAttribute("error", error);
            model.addAttribute("name", cleanName);
            model.addAttribute("email", cleanEmail);
            model.addAttribute("topic", cleanTopic);
            model.addAttribute("message", cleanMessage);
            return "contact-us";
        }
        mailer.submit(cleanName, cleanEmail, cleanTopic, cleanMessage);
        return "redirect:/contact?sent";
    }

    /** Line breaks are removed so a name or address can't add extra email headers. */
    private static String oneLine(String value, int max) {
        String s = value.replaceAll("[\\r\\n\\t]+", " ").strip();
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static boolean validEmail(String email) {
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return false;
        }
        try {
            new InternetAddress(email, true).validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
    }
}
