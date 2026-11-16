package com.bazaarready.account;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Owner accounts: sign-up, sign-in lookups, and who is signed in right now. */
@Service
public class AccountService implements UserDetailsService {

    /** Passwords too common to allow, whatever their length. */
    private static final Set<String> COMMON = Set.of("password", "password1", "password123", "12345678", "123456789",
            "1234567890", "qwerty123", "qwertyuiop", "iloveyou", "11111111", "00000000", "abcd1234", "bazaarready");

    public record Me(boolean signedIn, String name, String email) {
    }

    public static class SignUpException extends RuntimeException {
        public SignUpException(String message) {
            super(message);
        }
    }

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    AccountService(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    public static String normaliseEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Creates an account, or explains in plain words what is wrong. */
    public UUID signUp(String name, String email, String password, String confirm) {
        String n = name == null ? "" : name.trim();
        String e = normaliseEmail(email);
        if (n.isEmpty() || n.length() > 80) {
            throw new SignUpException("Please enter your name.");
        }
        if (e.length() > 200 || !e.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new SignUpException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 8) {
            throw new SignUpException("Choose a password of at least 8 characters.");
        }
        if (password.length() > 72) {
            throw new SignUpException("Choose a password of at most 72 characters.");
        }
        if (COMMON.contains(password.toLowerCase(Locale.ROOT)) || password.equalsIgnoreCase(e)) {
            throw new SignUpException("That password is too easy to guess. Choose another one.");
        }
        if (!password.equals(confirm)) {
            throw new SignUpException("The two passwords don't match.");
        }
        UUID id = UUID.randomUUID();
        try {
            jdbc.update("insert into account (id, email, name, password_hash) values (?, ?, ?, ?)", id, e, n, encoder.encode(password));
        } catch (DuplicateKeyException ex) {
            throw new SignUpException("There is already an account with this email. Sign in instead.");
        }
        return id;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        List<String> hash = jdbc.queryForList("select password_hash from account where email = ?", String.class, normaliseEmail(email));
        if (hash.isEmpty()) {
            throw new UsernameNotFoundException("No account");
        }
        return User.withUsername(normaliseEmail(email)).password(hash.get(0)).roles("OWNER").build();
    }

    /** The signed-in account, if any. */
    public Optional<UUID> currentId() {
        String email = currentEmail();
        if (email == null) {
            return Optional.empty();
        }
        return jdbc.queryForList("select id from account where email = ?", UUID.class, email).stream().findFirst();
    }

    public UUID requireCurrentId() {
        return currentId().orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Please sign in."));
    }

    /** The signed-in email, or null for visitors and background work. */
    public static String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    public Me me() {
        String email = currentEmail();
        if (email == null) {
            return new Me(false, null, null);
        }
        List<String> names = jdbc.queryForList("select name from account where email = ?", String.class, email);
        return names.isEmpty() ? new Me(false, null, null) : new Me(true, names.get(0), email);
    }
}
