package com.apu.asc.persistence;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Plain UTF-8 text files under {@code data/} (no JSON/XML).
 */
public final class DataPaths {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"), "data");

    private DataPaths() {}

    public static Path root() {
        return ROOT;
    }

    public static Path users() {
        return ROOT.resolve("users.txt");
    }

    public static Path catalog() {
        return ROOT.resolve("catalog.txt");
    }

    public static Path appointments() {
        return ROOT.resolve("appointments.txt");
    }

    public static Path payments() {
        return ROOT.resolve("payments.txt");
    }

    public static Path feedback() {
        return ROOT.resolve("feedback.txt");
    }

    public static Path technicianFeedback() {
        return ROOT.resolve("technician_feedback.txt");
    }

    public static Path customerStaffComments() {
        return ROOT.resolve("customer_staff_comments.txt");
    }
}
