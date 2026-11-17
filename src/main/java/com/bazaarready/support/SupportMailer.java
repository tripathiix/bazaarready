package com.bazaarready.support;

import jakarta.annotation.PreDestroy;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Saves contact-page messages and emails them to the support mailbox through Titan's SMTP server.
 *
 * <p>Save first, send after: a message is stored as PENDING before any email is attempted, so a mail
 * outage or a wrong password never loses it. One background thread sends whatever is pending, right
 * after each new message and again every five minutes, and gives up on a message after five tries.
 * Because there is only one sending thread, the same message is never sent twice at once.
 *
 * <p>Mail is sent from the mailbox we sign in as (so SPF and DKIM checks pass), with Reply-To set to
 * the visitor. Visitors never get an automatic email: the form must not be usable to send mail to
 * any address someone types in.
 */
@Service
public class SupportMailer {

    private static final Logger log = LoggerFactory.getLogger(SupportMailer.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm 'UTC'");

    private final JdbcTemplate jdbc;
    private final JavaMailSenderImpl mail;
    private final String from;
    private final String to;
    private final ScheduledExecutorService sender = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "support-mail");
        t.setDaemon(true);
        return t;
    });

    SupportMailer(JdbcTemplate jdbc,
                  @Value("${bazaarready.smtp.host}") String host,
                  @Value("${bazaarready.smtp.port}") int port,
                  @Value("${bazaarready.smtp.username}") String username,
                  @Value("${bazaarready.smtp.password}") String password,
                  @Value("${bazaarready.smtp.to}") String to) {
        this.jdbc = jdbc;
        this.from = username.trim();
        this.to = to.trim();
        if (password.isBlank()) {
            this.mail = null;
            log.info("SMTP_PASSWORD is not set: contact messages will be saved but not emailed");
            return;
        }
        this.mail = new JavaMailSenderImpl();
        mail.setHost(host.trim());
        mail.setPort(port);
        mail.setUsername(from);
        mail.setPassword(password.trim());
        mail.setDefaultEncoding("UTF-8");
        Properties p = mail.getJavaMailProperties();
        p.put("mail.smtp.auth", "true");
        if (port == 465) {
            p.put("mail.smtp.ssl.enable", "true");          // encrypted from the first byte
        } else {
            p.put("mail.smtp.starttls.enable", "true");     // port 587: upgrade to encryption first
            p.put("mail.smtp.starttls.required", "true");
        }
        p.put("mail.smtp.connectiontimeout", "10000");
        p.put("mail.smtp.timeout", "15000");
        p.put("mail.smtp.writetimeout", "15000");
        sender.scheduleWithFixedDelay(this::sendPending, 1, 5, TimeUnit.MINUTES);
    }

    /** Saves the message, then emails it in the background. */
    public void submit(String name, String email, String topic, String message) {
        jdbc.update("insert into support_message (id, name, email, topic, message) values (?, ?, ?, ?, ?)",
                UUID.randomUUID(), name, email, topic, message);
        if (mail != null) {
            sender.execute(this::sendPending);
        }
    }

    private void sendPending() {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList("""
                    select id, name, email, topic, message, attempts, created_at from support_message
                    where status = 'PENDING' order by created_at limit 20
                    """);
            for (Map<String, Object> row : rows) {
                if (!sendOne(row)) {
                    return;
                }
            }
        } catch (RuntimeException e) {
            log.warn("Could not send pending contact messages: {}", e.getMessage());
        }
    }

    /** Sends one message and records the result. Returns false when the rest should wait (wrong password). */
    private boolean sendOne(Map<String, Object> row) {
        UUID id = (UUID) row.get("id");
        try {
            send(row);
            jdbc.update("update support_message set status = 'SENT', sent_at = now(), attempts = attempts + 1, last_error = null where id = ?", id);
            log.info("Contact message {} emailed to {}", id, to);
            return true;
        } catch (Exception e) {
            int attempts = ((Number) row.get("attempts")).intValue() + 1;
            String error = String.valueOf(e.getMessage());
            jdbc.update("update support_message set status = ?, attempts = ?, last_error = ? where id = ?",
                    attempts >= MAX_ATTEMPTS ? "FAILED" : "PENDING", attempts, error.substring(0, Math.min(500, error.length())), id);
            log.warn("Contact message {} not emailed (try {} of {}): {}", id, attempts, MAX_ATTEMPTS, error);
            return !(e instanceof MailAuthenticationException);
        }
    }

    private void send(Map<String, Object> row) throws Exception {
        String name = (String) row.get("name");
        String email = (String) row.get("email");
        String topic = (String) row.get("topic");
        String when = row.get("created_at") instanceof Timestamp ts ? ts.toInstant().atOffset(ZoneOffset.UTC).format(WHEN) : "";

        var message = mail.createMimeMessage();
        var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
        helper.setFrom(new InternetAddress(from, "BazaarReady contact form", StandardCharsets.UTF_8.name()));
        helper.setTo(to);
        helper.setReplyTo(new InternetAddress(email, name, StandardCharsets.UTF_8.name()));
        helper.setSubject("[" + topic + "] " + name);
        helper.setText(name + " <" + email + "> wrote through bazaarready.com/contact on " + when + ".\n"
                + "Topic: " + topic + "\n\n"
                + row.get("message") + "\n\n"
                + "--\nPress Reply to answer them directly.\n");
        mail.send(message);
    }

    @PreDestroy
    void stop() {
        sender.shutdown();
    }
}
