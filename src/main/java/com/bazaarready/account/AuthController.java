package com.bazaarready.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Sign-in, sign-up and the "My sites" page. */
@Controller
class AuthController {

    private final AccountService accounts;
    private final SecurityContextRepository contexts;
    private final JdbcTemplate jdbc;

    @org.springframework.beans.factory.annotation.Value("${bazaarready.google.client-id:}")
    private String googleId;

    @org.springframework.web.bind.annotation.ModelAttribute("googleEnabled")
    boolean googleEnabled() {
        return googleId != null && !googleId.isBlank();
    }

    AuthController(AccountService accounts, SecurityContextRepository contexts, JdbcTemplate jdbc) {
        this.accounts = accounts;
        this.contexts = contexts;
        this.jdbc = jdbc;
    }

    @GetMapping("/login")
    String login() {
        return accounts.me().signedIn() ? "redirect:/" : "auth/login";
    }

    @GetMapping("/signup")
    String signupPage() {
        return accounts.me().signedIn() ? "redirect:/" : "auth/signup";
    }

    @PostMapping("/signup")
    String signup(@RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "") String email,
                  @RequestParam(defaultValue = "") String password, @RequestParam(defaultValue = "") String confirm,
                  HttpServletRequest request, HttpServletResponse response, Model model) {
        try {
            accounts.signUp(name, email, password, confirm);
        } catch (AccountService.SignUpException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "auth/signup";
        }
        // Sign the new owner in straight away, on a fresh session id.
        UserDetails user = accounts.loadUserByUsername(email);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
        SecurityContextHolder.setContext(context);
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        contexts.saveContext(context, request, response);
        return "redirect:/?welcome";
    }

    /** "My sites": every site this account owns, with its public link and private dashboard. */
    @GetMapping("/account")
    String account(Model model) {
        UUID me = accounts.requireCurrentId();
        List<Map<String, Object>> sites = jdbc.queryForList("""
                select name, slug, status, owner_key, published_at, created_at
                from business where account_id = ? order by created_at desc
                """, me);
        model.addAttribute("me", accounts.me());
        model.addAttribute("sites", sites);
        return "auth/account";
    }

    @GetMapping("/api/me")
    @ResponseBody
    AccountService.Me me() {
        return accounts.me();
    }
}
