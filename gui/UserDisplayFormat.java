package com.apu.asc.gui;

import com.apu.asc.model.User;
import com.apu.asc.repository.UserRepository;

/**
 * Turns user ids in tables into readable labels (full name + username).
 */
public final class UserDisplayFormat {
    private UserDisplayFormat() {}

    public static String fromId(UserRepository users, String userId) {
        if (userId == null || userId.isBlank()) {
            return "—";
        }
        return users.findById(userId).map(UserDisplayFormat::friendlyLine).orElseGet(() -> "ID: " + userId);
    }

    public static String friendlyLine(User u) {
        String name = u.getFullName() != null && !u.getFullName().isBlank()
                ? u.getFullName()
                : u.getUsername();
        return name + " — @" + u.getUsername();
    }
}
