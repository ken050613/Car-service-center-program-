package com.apu.asc.service;

import com.apu.asc.model.*;
import com.apu.asc.repository.UserRepository;
import com.apu.asc.util.IdGenerator;
import com.apu.asc.util.PasswordUtil;
import com.apu.asc.util.ValidationUtil;

import java.io.IOException;

public class RegistrationService {
    private final UserRepository users;

    public RegistrationService(UserRepository users) {
        this.users = users;
    }

    public Customer registerCustomer(String username, String password, String fullName, String email,
                                     String phone, String vehicleReg) throws IOException {
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameExists(username)) {
            throw new IllegalStateException("Username already taken.");
        }
        Customer c = new Customer(
                IdGenerator.newId(),
                username,
                PasswordUtil.hash(password),
                fullName,
                email,
                phone,
                vehicleReg
        );
        users.add(c);
        return c;
    }

    public CounterStaff registerCounterStaff(String username, String password, String fullName, String email)
            throws IOException {
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameExists(username)) {
            throw new IllegalStateException("Username already taken.");
        }
        CounterStaff cs = new CounterStaff(
                IdGenerator.newId(),
                username,
                PasswordUtil.hash(password),
                fullName,
                email
        );
        users.add(cs);
        return cs;
    }

    public Technician registerTechnician(String username, String password, String fullName, String email,
                                           String specialty) throws IOException {
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameExists(username)) {
            throw new IllegalStateException("Username already taken.");
        }
        Technician t = new Technician(
                IdGenerator.newId(),
                username,
                PasswordUtil.hash(password),
                fullName,
                email,
                specialty
        );
        users.add(t);
        return t;
    }
}
