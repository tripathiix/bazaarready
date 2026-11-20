package com.bazaarready.intake;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads WhatsApp "Export chat" files (Android and iPhone formats, as .txt or .zip) and removes
 * private details before anything is stored or sent to the model: customers become "Customer 1",
 * "Customer 2"; phone numbers and email addresses are replaced; media lines are dropped.
 * The uploaded files themselves are never stored.
 */
public final class WhatsAppChat {

    /** Android: "12/08/26, 10:14 - Name: text". iPhone: "[12/08/26, 10:14:05 AM] Name: text". */
    private static final Pattern LINE = Pattern.compile(
            "^\\u200E?\\[?(\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2,4}),?\\s+(\\d{1,2}[:.]\\d{2}(?:[:.]\\d{2})?(?:\\s?[AaPp]\\.?\\s?[Mm]\\.?)?)\\]?\\s*(?:[-–]\\s*)?([^:]{1,60}?):\\s(.*)$");
    private static final Pattern PHONE = Pattern.compile("(?<![\\w])\\+?\\d[\\d\\s().-]{7,}\\d");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern SKIP = Pattern.compile("(?i)(<media omitted>|image omitted|video omitted|audio omitted|sticker omitted|"
            + "document omitted|gif omitted|this message was deleted|messages and calls are end-to-end encrypted|"
            + "missed voice call|missed video call|<attached:)");
    private static final int MAX_TOTAL_BYTES = 8 * 1024 * 1024;
    private static final int MAX_TRANSCRIPT = 40_000;

    public record Message(String chat, String sender, String text) {
    }

    /** The cleaned result: a transcript for the agent, and counts for the activity log. */
    public record Result(String transcript, int messages, int customers, int chats, String owner) {
    }

    private WhatsAppChat() {
    }

    /** The .txt texts inside uploaded files; a .zip export is opened and only its .txt files are read. */
    public static List<String> readFiles(List<byte[]> files, List<String> names) throws IOException {
        List<String> texts = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < files.size(); i++) {
            byte[] data = files.get(i);
            String name = names.get(i) == null ? "" : names.get(i).toLowerCase(Locale.ROOT);
            if (name.endsWith(".zip") || SafeZip.isZip(data)) {
                // Opened from its table of contents: only .txt entries are decompressed (see SafeZip).
                for (SafeZip.Item item : SafeZip.read(data, n -> n.toLowerCase(Locale.ROOT).endsWith(".txt"),
                        MAX_TOTAL_BYTES - total, "The chat files are too large.")) {
                    total += item.data().length;
                    texts.add(new String(item.data(), StandardCharsets.UTF_8));
                }
            } else {
                total += data.length;
                if (total > MAX_TOTAL_BYTES) {
                    throw new IOException("The chat files are too large.");
                }
                texts.add(new String(data, StandardCharsets.UTF_8));
            }
        }
        return texts;
    }

    public static List<Message> parse(List<String> chats) {
        List<Message> out = new ArrayList<>();
        for (int c = 0; c < chats.size(); c++) {
            String chat = "chat " + (c + 1);
            StringBuilder current = null;
            String sender = null;
            for (String raw : chats.get(c).split("\\r?\\n")) {
                Matcher m = LINE.matcher(raw.replace(' ', ' ').replace(' ', ' '));
                if (m.matches()) {
                    if (current != null) {
                        add(out, chat, sender, current.toString());
                    }
                    sender = m.group(3).trim();
                    current = new StringBuilder(m.group(4));
                } else if (current != null && !raw.isBlank()) {
                    current.append(' ').append(raw.trim());
                }
            }
            if (current != null) {
                add(out, chat, sender, current.toString());
            }
        }
        return out;
    }

    private static void add(List<Message> out, String chat, String sender, String text) {
        if (!SKIP.matcher(text).find() && !text.isBlank()) {
            out.add(new Message(chat, sender, text.trim()));
        }
    }

    /**
     * Builds the cleaned transcript. The owner is the name given, or otherwise whoever appears in
     * the most chats (and then sends the most messages). Everyone else becomes "Customer N".
     */
    public static Result clean(List<Message> messages, String ownerHint) {
        Map<String, java.util.Set<String>> chatsBySender = new HashMap<>();
        Map<String, Integer> countBySender = new HashMap<>();
        for (Message m : messages) {
            chatsBySender.computeIfAbsent(m.sender(), k -> new java.util.HashSet<>()).add(m.chat());
            countBySender.merge(m.sender(), 1, Integer::sum);
        }
        String owner = null;
        if (ownerHint != null && !ownerHint.isBlank()) {
            String hint = ownerHint.trim().toLowerCase(Locale.ROOT);
            owner = countBySender.keySet().stream().filter(s -> s.toLowerCase(Locale.ROOT).contains(hint)).findFirst().orElse(null);
        }
        if (owner == null) {
            owner = countBySender.keySet().stream()
                    .max((a, b) -> {
                        int byChats = Integer.compare(chatsBySender.get(a).size(), chatsBySender.get(b).size());
                        return byChats != 0 ? byChats : Integer.compare(countBySender.get(a), countBySender.get(b));
                    }).orElse(null);
        }

        Map<String, String> alias = new LinkedHashMap<>();
        for (Message m : messages) {
            if (!m.sender().equals(owner)) {
                alias.computeIfAbsent(m.sender(), k -> "Customer " + (alias.size() + 1));
            }
        }

        StringBuilder transcript = new StringBuilder();
        String lastChat = null;
        // Newest messages matter most (current prices), so if the history is long, keep the end.
        List<String> lines = new ArrayList<>();
        for (Message m : messages) {
            String text = redact(m.text(), alias);
            String who = m.sender().equals(owner) ? "OWNER" : alias.get(m.sender());
            String header = m.chat().equals(lastChat) ? "" : "\n--- " + m.chat() + " ---\n";
            lastChat = m.chat();
            lines.add(header + who + ": " + text);
        }
        int size = 0;
        int start = lines.size();
        while (start > 0 && size + lines.get(start - 1).length() + 1 < MAX_TRANSCRIPT) {
            start--;
            size += lines.get(start).length() + 1;
        }
        if (start > 0) {
            transcript.append("[Older messages left out; this is the most recent part of the history.]\n");
        }
        for (int i = start; i < lines.size(); i++) {
            transcript.append(lines.get(i)).append('\n');
        }
        long chats = messages.stream().map(Message::chat).distinct().count();
        return new Result(transcript.toString(), messages.size(), alias.size(), (int) chats, owner);
    }

    private static String redact(String text, Map<String, String> alias) {
        String t = EMAIL.matcher(text).replaceAll("[email]");
        t = PHONE.matcher(t).replaceAll("[phone]");
        for (Map.Entry<String, String> entry : alias.entrySet()) {
            String first = entry.getKey().split("\\s+")[0];
            if (first.length() >= 3 && !first.matches("[+\\d].*")) {
                t = t.replaceAll("(?i)\\b" + Pattern.quote(first) + "\\b", entry.getValue());
            }
        }
        return t;
    }
}
