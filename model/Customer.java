package com.apu.asc.model;

public class Customer extends User {
    private String phone;
    private String vehicleRegistration;

    public Customer(String id, String username, String passwordHash, String fullName, String email,
                    String phone, String vehicleRegistration) {
        super(id, username, passwordHash, fullName, email);
        this.phone = phone != null ? phone : "";
        this.vehicleRegistration = vehicleRegistration != null ? vehicleRegistration : "";
    }

    @Override
    public Role getRole() {
        return Role.CUSTOMER;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }
}
