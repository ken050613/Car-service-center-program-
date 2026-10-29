package com.apu.asc.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Customer comments about counter staff and technicians involved in an appointment
 * (coursework requirement).
 */
public class CustomerStaffComments {
    private final String id;
    private final String appointmentId;
    private final String customerId;
    private String commentCounterStaff;
    private String commentTechnician;
    private final LocalDateTime createdAt;

    public CustomerStaffComments(String id, String appointmentId, String customerId,
                                 String commentCounterStaff, String commentTechnician, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.appointmentId = Objects.requireNonNull(appointmentId);
        this.customerId = Objects.requireNonNull(customerId);
        this.commentCounterStaff = commentCounterStaff != null ? commentCounterStaff : "";
        this.commentTechnician = commentTechnician != null ? commentTechnician : "";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCommentCounterStaff() {
        return commentCounterStaff;
    }

    public void setCommentCounterStaff(String commentCounterStaff) {
        this.commentCounterStaff = commentCounterStaff != null ? commentCounterStaff : "";
    }

    public String getCommentTechnician() {
        return commentTechnician;
    }

    public void setCommentTechnician(String commentTechnician) {
        this.commentTechnician = commentTechnician != null ? commentTechnician : "";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
