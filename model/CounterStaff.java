package com.apu.asc.model;

public class CounterStaff extends User {
    public CounterStaff(String id, String username, String passwordHash, String fullName, String email) {
        super(id, username, passwordHash, fullName, email);
    }

    @Override
    public Role getRole() {
        return Role.COUNTER_STAFF;
    }
}
