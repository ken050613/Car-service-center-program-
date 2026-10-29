package apu.service;

import apu.model.*;
import apu.repository.UserRepository;
import apu.util.PasswordUtil;
import apu.util.ValidationUtil;

import java.io.IOException;

/**
 * Edit personal profile for any role (Table 1.0).
 */
public class ProfileService {
    private final UserRepository users;

    public ProfileService(UserRepository users) {
        this.users = users;
    }

    public void updateProfile(User user, String fullName, String email, String phoneOrNull, String vehicleOrNull,
                              String currentPassword, String newPasswordOrNull) throws IOException {
        ValidationUtil.requireNonBlank(fullName, "Full name");
        ValidationUtil.requireEmail(email, "Email");
        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            ValidationUtil.requireNonBlank(currentPassword, "Current password");
            if (!PasswordUtil.matches(currentPassword, user.getPasswordHash())) {
                throw new SecurityException("Current password is incorrect.");
            }
            user.setPasswordHash(PasswordUtil.hash(newPasswordOrNull));
        }
        user.setFullName(fullName);
        user.setEmail(email);
        if (user instanceof Customer c) {
            if (phoneOrNull != null) {
                c.setPhone(phoneOrNull);
            }
            if (vehicleOrNull != null) {
                c.setVehicleRegistration(vehicleOrNull);
            }
        }
        users.replace(user);
    }
}
