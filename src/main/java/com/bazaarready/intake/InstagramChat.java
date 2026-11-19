package com.bazaarready.intake;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads Instagram direct messages from the "Download your information" export (JSON format): either the
 * whole .zip, or the message_1.json files taken out of it. Each conversation folder becomes one chat.
 * Photos and videos in the zip are skipped without being read. The result goes through the same clean-up
 * as WhatsApp chats ({@link WhatsAppChat#clean}), so names, numbers and emails are removed before anything
 * reaches the model, and the files are never stored.
 */
public final class InstagramChat {

    /** message_1.json, message_2.json … inside messages/inbox/&lt;conversation&gt;/ (or message_requests/). */
    private static final Pattern MESSAGE_FILE = Pattern.compile("(?i)(^|.*/)message_\\d+\\.json$");
    /** Instagram's own notices, not things people wrote. */
    private static final Pattern SKIP = Pattern.compile("(?i)^(liked a message|reacted .{1,8} to your message|"
            + ".* sent an attachment\\.?|.* shared a story\\.?|you sent an attachment\\.?|.* replied to your story|"
            + "you missed a (video|audio) chat|.* started a (video|audio) chat|.* left the group|.* named the group.*)$");
    private static final long MAX_JSON_BYTES = 12L * 1024 * 1024;
    private static final ObjectMapper JSON = new ObjectMapper();

    private InstagramChat() {
    }

    /** Whether the uploaded files look like an Instagram export rather than WhatsApp text. */
    public static boolean looksLikeInstagram(List<byte[]> files, List<String> names) throws IOException {
        for (int i = 0; i < files.size(); i++) {
            if (isInstagram(files.get(i), names.get(i))) {
                return true;
            }
        }
        return false;
    }

    /** Whether one uploaded file is Instagram messages (a message JSON, or a zip holding them). */
    public static boolean isInstagram(byte[] data, String fileName) throws IOException {
        String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (name.endsWith(".json")) {
            return true;
        }
        if (data.length > 3 && data[0] == 'P' && data[1] == 'K') {
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (MESSAGE_FILE.matcher(entry.getName()).matches()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Every message in every conversation, oldest first within each conversation. */
    public static List<WhatsAppChat.Message> read(List<byte[]> files, List<String> names) throws IOException {
        // conversation folder -> its JSON files
        Map<String, List<byte[]>> byConversation = new LinkedHashMap<>();
        long total = 0;
        for (int i = 0; i < files.size(); i++) {
            byte[] data = files.get(i);
            String name = names.get(i) == null ? "upload-" + i : names.get(i);
            if (data.length > 3 && data[0] == 'P' && data[1] == 'K') {
                try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        if (entry.isDirectory() || !MESSAGE_FILE.matcher(entry.getName()).matches()) {
                            continue;
                        }
                        byte[] content = zip.readNBytes((int) MAX_JSON_BYTES);
                        total += content.length;
                        if (total > MAX_JSON_BYTES) {
                            throw new IOException("The Instagram messages are too large. Choose a shorter date range when downloading.");
                        }
                        byConversation.computeIfAbsent(folderOf(entry.getName()), k -> new ArrayList<>()).add(content);
                    }
                }
            } else if (name.toLowerCase(Locale.ROOT).endsWith(".json")) {
                total += data.length;
                if (total > MAX_JSON_BYTES) {
                    throw new IOException("The Instagram messages are too large. Choose a shorter date range when downloading.");
                }
                byConversation.computeIfAbsent("upload-" + i, k -> new ArrayList<>()).add(data);
            }
        }

        List<WhatsAppChat.Message> out = new ArrayList<>();
        int n = 0;
        for (List<byte[]> parts : byConversation.values()) {
            String chat = "instagram chat " + (++n);
            List<long[]> order = new ArrayList<>();
            List<WhatsAppChat.Message> messages = new ArrayList<>();
            for (byte[] part : parts) {
                JsonNode root;
                try {
                    root = JSON.readTree(part);
                } catch (IOException e) {
                    continue; // not a message file after all
                }
                for (JsonNode m : root.path("messages")) {
                    String text = fix(m.path("content").asText(""));
                    String sender = fix(m.path("sender_name").asText(""));
                    if (text.isBlank() || sender.isBlank() || SKIP.matcher(text.trim()).matches()) {
                        continue;
                    }
                    order.add(new long[] {m.path("timestamp_ms").asLong(0), messages.size()});
                    messages.add(new WhatsAppChat.Message(chat, sender, text.replaceAll("\\s+", " ").trim()));
                }
            }
            // Instagram writes newest first; put each conversation in time order.
            order.sort(Comparator.comparingLong(o -> o[0]));
            for (long[] o : order) {
                out.add(messages.get((int) o[1]));
            }
        }
        return out;
    }

    private static String folderOf(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(0, slash);
    }

    /**
     * Instagram's export writes text as UTF-8 bytes escaped one byte at a time ("Ã°Å¸Å½â€°" instead of an emoji,
     * broken Hindi). Turning the characters back into bytes and reading them as UTF-8 repairs it; text that
     * was already correct is left alone.
     */
    static String fix(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 0xFF) {
                return text; // real Unicode already: not the escaped form
            }
        }
        String repaired = new String(text.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return repaired.contains("�") ? text : repaired;
    }
}
