package com.bazaarready.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Who can open what, and the protections every response gets.
 *
 * <ul>
 *   <li>Public: BazaarReady's home page and guide, and every generated customer site. Customers never sign in.</li>
 *   <li>Signed in: building a site, the idea coach, and everything on an owner's dashboard.</li>
 *   <li>CSRF: required on every form and API call made by a signed-in owner. The React screens send
 *       the token from the XSRF-TOKEN cookie; server pages carry it as a hidden field. Customer
 *       forms on generated sites (/s/...) are exempt: their visitors are anonymous, so there is no
 *       signed-in session to abuse.</li>
 *   <li>Headers: frames denied, strict referrer, HSTS over HTTPS, and a nonce-based
 *       Content-Security-Policy (see {@link SecurityHeadersFilter}).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        // BCrypt by default, with the algorithm stored next to each hash so it can be upgraded later.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository contexts) throws Exception {
        CookieCsrfTokenRepository csrfTokens = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokens.setCookieCustomizer(cookie -> cookie.sameSite("Lax"));
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        // Load the token on every request, so the cookie is always there for the React screens.
        csrfHandler.setCsrfRequestAttributeName(null);

        http
                .securityContext(c -> c.securityContextRepository(contexts))
                .csrf(c -> c.csrfTokenRepository(csrfTokens).csrfTokenRequestHandler(csrfHandler)
                        .ignoringRequestMatchers("/s/**"))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/owner/**", "/api/owner/**", "/account", "/api/agent/**", "/api/coach/**",
                                "/api/businesses/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(f -> f.loginPage("/login").loginProcessingUrl("/login")
                        .usernameParameter("email").passwordParameter("password")
                        .defaultSuccessUrl("/", false).failureUrl("/login?error").permitAll())
                .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/?signedout").deleteCookies("JSESSIONID"))
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), request -> request.getRequestURI().startsWith("/api/")))
                .headers(h -> h
                        .frameOptions(f -> f.deny())
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .httpStrictTransportSecurity(s -> s.maxAgeInSeconds(31_536_000).includeSubDomains(true)))
                .addFilterBefore(new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new SecurityHeadersFilter(), RateLimitFilter.class);
        return http.build();
    }
}
