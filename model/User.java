package com.apu.asc.model;

import java.util.Objects;

/**
 * Base type for all portal users. Subclasses specialize behaviour and data
 * (inheritance + polymorphism).
 */
public abstract class User {
    private final String id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;

    protected User(String id, String username, String passwordHash, String fullName, String email) {
        this.id = Objects.requireNonNull(id);
        this.username = Objects.requireNonNull(username);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.fullName = Objects.requireNonNull(fullName);
        this.email = Objects.requireNonNull(email);
    }

    /** Role used for authorization and menu dispatch. */
    public abstract Role getRole();

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
