package apu.util;

import java.math.BigDecimal;

/** Basic input validation for GUI and services (coursework requirement). */
public final class ValidationUtil {
    private ValidationUtil() {}

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    /** Non-blank email must contain {@code @} (basic format check). */
    public static void requireEmail(String value, String fieldName) {
        requireNonBlank(value, fieldName);
        if (!value.trim().contains("@")) {
            throw new IllegalArgumentException(
                    fieldName + " must include @ (for example name@example.com).");
        }
    }

    public static void requirePositiveMoney(BigDecimal v, String fieldName) {
        if (v == null || v.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be zero or positive.");
        }
    }

    public static void requireRating(int r) {
        if (r < 1 || r > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
