package com.bazaarready.intake;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Opens an uploaded zip from its table of contents, so files we don't need are never decompressed.
 * Reading a zip front to back (ZipInputStream) has to inflate every entry just to skip it, which lets a small
 * crafted zip (a "zip bomb") keep a CPU busy for minutes. Here only the wanted entries are inflated, and only
 * up to a byte limit; the entry count is capped too. The zip is written to a temporary file for the moment
 * it is read and deleted straight after.
 */
public final class SafeZip {

    /** More entries than any real chat export has. */
    static final int MAX_ENTRIES = 20_000;

    /** A wanted file inside the zip: its path and its bytes. */
    public record Item(String name, byte[] data) {
    }

    private SafeZip() {
    }

    /** Whether the bytes start like a zip file. */
    public static boolean isZip(byte[] data) {
        return data.length > 3 && data[0] == 'P' && data[1] == 'K';
    }

    /** The names of every entry, read from the table of contents only (nothing is decompressed). */
    public static List<String> names(byte[] zip) throws IOException {
        List<String> names = new ArrayList<>();
        withZip(zip, file -> {
            Enumeration<? extends ZipEntry> entries = file.entries();
            int count = 0;
            while (entries.hasMoreElements()) {
                if (++count > MAX_ENTRIES) {
                    throw new IOException("This zip has too many files in it.");
                }
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    names.add(entry.getName());
                }
            }
        });
        return names;
    }

    /**
     * The wanted entries' contents. Stops with {@code tooLarge} as soon as their decompressed total passes
     * {@code maxTotal}; an entry is never inflated past that limit.
     */
    public static List<Item> read(byte[] zip, Predicate<String> wanted, long maxTotal, String tooLarge) throws IOException {
        List<Item> items = new ArrayList<>();
        withZip(zip, file -> {
            Enumeration<? extends ZipEntry> entries = file.entries();
            long total = 0;
            int count = 0;
            while (entries.hasMoreElements()) {
                if (++count > MAX_ENTRIES) {
                    throw new IOException("This zip has too many files in it.");
                }
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !wanted.test(entry.getName())) {
                    continue;
                }
                long left = maxTotal - total;
                if (entry.getSize() > left) {
                    throw new IOException(tooLarge);
                }
                byte[] content;
                try (InputStream in = file.getInputStream(entry)) {
                    // One byte more than allowed tells us it was too big, without inflating the rest.
                    content = in.readNBytes((int) Math.min(Integer.MAX_VALUE - 8, left + 1));
                }
                total += content.length;
                if (total > maxTotal) {
                    throw new IOException(tooLarge);
                }
                items.add(new Item(entry.getName(), content));
            }
        });
        return items;
    }

    private interface ZipWork {
        void run(ZipFile file) throws IOException;
    }

    private static void withZip(byte[] zip, ZipWork work) throws IOException {
        Path tmp = Files.createTempFile("upload-", ".zip");
        try {
            Files.write(tmp, zip);
            try (ZipFile file = new ZipFile(tmp.toFile())) {
                work.run(file);
            }
        } catch (java.util.zip.ZipException e) {
            throw new IOException("This file doesn't look like a valid zip.");
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
