package com.apu.asc.service;

import com.apu.asc.model.*;
import com.apu.asc.repository.UserRepository;
import com.apu.asc.util.IdGenerator;
import com.apu.asc.util.PasswordUtil;
import com.apu.asc.util.ValidationUtil;

import java.io.IOException;

/**
 * Managers CRUD managers, counter staff, and technicians (Table 1.0).
 */
public class StaffAdminService {
    private final UserRepository users;

    public StaffAdminService(UserRepository users) {
        this.users = users;
    }

    public void createManager(String username, String password, String fullName, String email) throws IOException {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireNonBlank(password, "Password");
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameTakenByOther(username, null)) {
            throw new IllegalStateException("Username already taken.");
        }
        users.add(new Manager(IdGenerator.newId(), username, PasswordUtil.hash(password), fullName, email));
    }

    public void updateManager(String id, String username, String fullName, String email, String newPasswordOrNull)
            throws IOException {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!(u instanceof Manager m)) {
            throw new IllegalArgumentException("Not a manager.");
        }
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameTakenByOther(username, id)) {
            throw new IllegalStateException("Username already taken.");
        }
        m.setUsername(username);
        m.setFullName(fullName);
        m.setEmail(email);
        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            m.setPasswordHash(PasswordUtil.hash(newPasswordOrNull));
        }
        users.replace(m);
    }

    public void deleteManager(String id, String actingManagerId) throws IOException {
        if (users.managerCount() <= 1) {
            throw new IllegalStateException("Cannot delete the last manager.");
        }
        if (id.equals(actingManagerId)) {
            throw new IllegalStateException("Cannot delete your own account while logged in.");
        }
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!(u instanceof Manager)) {
            throw new IllegalArgumentException("Not a manager.");
        }
        users.deleteById(id);
    }

    public void createCounterStaff(String username, String password, String fullName, String email) throws IOException {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameTakenByOther(username, null)) {
            throw new IllegalStateException("Username already taken.");
        }
        users.add(new CounterStaff(IdGenerator.newId(), username, PasswordUtil.hash(password), fullName, email));
    }

    public void updateCounterStaff(String id, String username, String fullName, String email, String newPasswordOrNull)
            throws IOException {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!(u instanceof CounterStaff c)) {
            throw new IllegalArgumentException("Not counter staff.");
        }
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameTakenByOther(username, id)) {
            throw new IllegalStateException("Username already taken.");
        }
        c.setUsername(username);
        c.setFullName(fullName);
        c.setEmail(email);
        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            c.setPasswordHash(PasswordUtil.hash(newPasswordOrNull));
        }
        users.replace(c);
    }

    public void deleteCounterStaff(String id) throws IOException {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!(u instanceof CounterStaff)) {
            throw new IllegalArgumentException("Not counter staff.");
        }
        users.deleteById(id);
    }

    public void createTechnician(String username, String password, String fullName, String email, String specialty)
            throws IOException {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameTakenByOther(username, null)) {
            throw new IllegalStateException("Username already taken.");
        }
        users.add(new Technician(IdGenerator.newId(), username, PasswordUtil.hash(password), fullName, email, specialty));
    }

    public void updateTechnician(String id, String username, String fullName, String email, String specialty,
                                 String newPasswordOrNull) throws IOException {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!(u instanceof Technician t)) {
            throw new IllegalArgumentException("Not a technician.");
        }
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameTakenByOther(username, id)) {
            throw new IllegalStateException("Username already taken.");
        }
        t.setUsername(username);
        t.setFullName(fullName);
        t.setEmail(email);
        t.setSpecialty(specialty);
        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            t.setPasswordHash(PasswordUtil.hash(newPasswordOrNull));
        }
        users.replace(t);
    }

    public void deleteTechnician(String id) throws IOException {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!(u instanceof Technician)) {
            throw new IllegalArgumentException("Not a technician.");
        }
        users.deleteById(id);
    }
}
