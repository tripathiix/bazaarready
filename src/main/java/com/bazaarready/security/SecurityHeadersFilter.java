package com.bazaarready.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * A Content-Security-Policy with a fresh nonce for every page. Only scripts from this site, or inline
 * scripts carrying the nonce, can run, so injected script in a business name or a customer message
 * would not execute even if it slipped through escaping. Server templates read the nonce from the
 * {@code cspNonce} request attribute.
 */
class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        byte[] bytes = new byte[18];
        RANDOM.nextBytes(bytes);
        String nonce = Base64.getEncoder().encodeToString(bytes);
        request.setAttribute("cspNonce", nonce);
        response.setHeader("Content-Security-Policy", String.join("; ",
                "default-src 'self'",
                "script-src 'self' 'nonce-" + nonce + "'",
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
                "font-src 'self' data: https://fonts.gstatic.com",
                "img-src 'self' data: blob: https://*.wikimedia.org https://images.pexels.com",
                "connect-src 'self'",
                "object-src 'none'",
                "base-uri 'self'",
                "form-action 'self'",
                "frame-ancestors 'self'"));
        // The microphone is used for "Speak instead"; nothing else is needed.
        response.setHeader("Permissions-Policy", "microphone=(self), camera=(), geolocation=(), payment=(), usb=()");
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        chain.doFilter(request, response);
    }
}
