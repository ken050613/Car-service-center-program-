package com.apu.asc.service;

import com.apu.asc.model.User;
import com.apu.asc.repository.UserRepository;
import com.apu.asc.util.PasswordUtil;
import com.apu.asc.util.ValidationUtil;

import java.io.IOException;
import java.util.Optional;

public class AuthService {
    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public Optional<User> login(String username, String password) {
        Optional<User> u = users.findByUsername(username);
        if (u.isEmpty()) {
            return Optional.empty();
        }
        if (!PasswordUtil.matches(password, u.get().getPasswordHash())) {
            return Optional.empty();
        }
        return u;
    }

    /**
     * Self-service reset: username must exist and email must match the account (case-insensitive).
     */
    public void resetPasswordByUsernameAndEmail(String username, String email, String newPassword) throws IOException {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireEmail(email, "Email");
        ValidationUtil.requireNonBlank(newPassword, "New password");
        User u = users.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("No account with this username."));
        if (!u.getEmail().trim().equalsIgnoreCase(email.trim())) {
            throw new SecurityException("Email does not match this account.");
        }
        u.setPasswordHash(PasswordUtil.hash(newPassword));
        users.replace(u);
    }
}
