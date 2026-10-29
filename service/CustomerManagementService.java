package com.apu.asc.service;

import com.apu.asc.model.AppointmentStatus;
import com.apu.asc.model.Customer;
import com.apu.asc.model.User;
import com.apu.asc.repository.AppointmentRepository;
import com.apu.asc.repository.UserRepository;
import com.apu.asc.util.IdGenerator;
import com.apu.asc.util.PasswordUtil;
import com.apu.asc.util.ValidationUtil;

import java.io.IOException;

/**
 * Counter staff CRUD customers (Table 1.0).
 */
public class CustomerManagementService {
    private final UserRepository users;
    private final AppointmentRepository appointments;

    public CustomerManagementService(UserRepository users, AppointmentRepository appointments) {
        this.users = users;
        this.appointments = appointments;
    }

    public Customer createCustomer(String username, String password, String fullName, String email,
                                   String phone, String vehicleReg) throws IOException {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameTakenByOther(username, null)) {
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

    public void updateCustomer(String id, String username, String fullName, String email, String phone,
                               String vehicleReg, String newPasswordOrNull) throws IOException {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found."));
        if (!(u instanceof Customer c)) {
            throw new IllegalArgumentException("Not a customer.");
        }
        ValidationUtil.requireEmail(email, "Email");
        if (users.usernameTakenByOther(username, id)) {
            throw new IllegalStateException("Username already taken.");
        }
        c.setUsername(username);
        c.setFullName(fullName);
        c.setEmail(email);
        c.setPhone(phone);
        c.setVehicleRegistration(vehicleReg);
        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            c.setPasswordHash(PasswordUtil.hash(newPasswordOrNull));
        }
        users.replace(c);
    }

    public void deleteCustomer(String id) throws IOException {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Customer not found."));
        if (!(u instanceof Customer)) {
            throw new IllegalArgumentException("Not a customer.");
        }
        for (var a : appointments.byCustomer(id)) {
            if (a.getStatus() != AppointmentStatus.COMPLETED && a.getStatus() != AppointmentStatus.CANCELLED) {
                throw new IllegalStateException(
                        "Customer has active appointments; cancel or complete them first.");
            }
        }
        users.deleteById(id);
    }
}
