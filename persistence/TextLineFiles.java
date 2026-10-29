package com.apu.asc.persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-text storage: one logical record per line, fields separated by {@code |}.
 * Pipe characters and line breaks in values are replaced so each record stays on one line.
 */
public final class TextLineFiles {
    private TextLineFiles() {}

    public static String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace('|', '/').replace('\n', ' ').replace('\r', ' ');
    }

    public static String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(safe(parts[i]));
        }
        return sb.toString();
    }

    public static String[] split(String line) {
        return line.split("\\|", -1);
    }

    public static List<String> readNonCommentLines(java.nio.file.Path path) throws java.io.IOException {
        List<String> out = new ArrayList<>();
        if (!java.nio.file.Files.isRegularFile(path)) {
            return out;
        }
        for (String raw : java.nio.file.Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String t = raw.trim();
            if (t.isEmpty() || t.startsWith("#")) {
                continue;
            }
            out.add(raw);
        }
        return out;
    }

    public static String emptyToNull(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return s;
    }
}
