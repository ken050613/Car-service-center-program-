package com.apu.asc.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Simple salted hash for coursework (not for production). */
public final class PasswordUtil {
    private static final String SALT = "apu-asc-local";

    private PasswordUtil() {}

    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(SALT.getBytes(StandardCharsets.UTF_8));
            byte[] d = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean matches(String plain, String storedHash) {
        return hash(plain).equalsIgnoreCase(storedHash);
    }
}
