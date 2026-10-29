package com.apu.asc.service;

import com.apu.asc.model.*;
import com.apu.asc.repository.AppointmentRepository;
import com.apu.asc.repository.CustomerStaffCommentRepository;
import com.apu.asc.repository.FeedbackRepository;
import com.apu.asc.repository.TechnicianFeedbackRepository;
import com.apu.asc.repository.UserRepository;
import com.apu.asc.util.IdGenerator;
import com.apu.asc.util.ValidationUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FeedbackService {
    private final FeedbackRepository feedback;
    private final TechnicianFeedbackRepository technicianFeedback;
    private final CustomerStaffCommentRepository staffComments;
    private final AppointmentRepository appointments;
    private final UserRepository users;

    public FeedbackService(FeedbackRepository feedback, TechnicianFeedbackRepository technicianFeedback,
                           CustomerStaffCommentRepository staffComments, AppointmentRepository appointments,
                           UserRepository users) {
        this.feedback = feedback;
        this.technicianFeedback = technicianFeedback;
        this.staffComments = staffComments;
        this.appointments = appointments;
        this.users = users;
    }

    public Feedback submitCustomerRating(String customerId, String appointmentId, int rating, String comment)
            throws IOException {
        User u = users.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Unknown customer."));
        if (!(u instanceof Customer)) {
            throw new SecurityException("Only customers may submit this feedback.");
        }
        ValidationUtil.requireRating(rating);
        var ap = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (!ap.getCustomerId().equals(customerId)) {
            throw new SecurityException("Appointment does not belong to this customer.");
        }
        if (ap.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Feedback allowed after service completion.");
        }
        if (feedback.existsForAppointment(appointmentId)) {
            throw new IllegalStateException("Feedback already submitted for this appointment.");
        }
        Feedback f = new Feedback(
                IdGenerator.newId(),
                appointmentId,
                customerId,
                rating,
                comment,
                LocalDateTime.now()
        );
        feedback.add(f);
        return f;
    }

    public TechnicianJobFeedback submitTechnicianJobFeedback(String technicianId, String appointmentId, String comment)
            throws IOException {
        ValidationUtil.requireNonBlank(comment, "Comment");
        Appointment ap = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (!technicianId.equals(ap.getTechnicianId())) {
            throw new SecurityException("Appointment is not assigned to you.");
        }
        if (technicianFeedback.findByAppointmentAndTechnician(appointmentId, technicianId).isPresent()) {
            throw new IllegalStateException("You already submitted feedback for this appointment.");
        }
        TechnicianJobFeedback f = new TechnicianJobFeedback(
                IdGenerator.newId(),
                appointmentId,
                technicianId,
                comment,
                LocalDateTime.now()
        );
        technicianFeedback.add(f);
        return f;
    }

    public CustomerStaffComments saveCustomerStaffComments(String customerId, String appointmentId,
                                                           String commentCounter, String commentTechnician)
            throws IOException {
        Appointment ap = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (!ap.getCustomerId().equals(customerId)) {
            throw new SecurityException("Appointment does not belong to this customer.");
        }
        if (ap.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Comments allowed after service completion.");
        }
        Optional<CustomerStaffComments> existing = staffComments.findByAppointment(appointmentId);
        if (existing.isPresent()) {
            CustomerStaffComments c = existing.get();
            c.setCommentCounterStaff(commentCounter != null ? commentCounter : "");
            c.setCommentTechnician(commentTechnician != null ? commentTechnician : "");
            staffComments.replace(c);
            return c;
        }
        CustomerStaffComments c = new CustomerStaffComments(
                IdGenerator.newId(),
                appointmentId,
                customerId,
                commentCounter != null ? commentCounter : "",
                commentTechnician != null ? commentTechnician : "",
                LocalDateTime.now()
        );
        staffComments.add(c);
        return c;
    }

    public List<Feedback> allFeedback() {
        return feedback.all();
    }

    public List<Feedback> forCustomer(String customerId) {
        return feedback.byCustomer(customerId);
    }

    public List<TechnicianJobFeedback> technicianFeedbackAll() {
        return technicianFeedback.all();
    }

    public List<TechnicianJobFeedback> technicianFeedbackFor(String technicianId) {
        return technicianFeedback.byTechnician(technicianId);
    }

    public List<CustomerStaffComments> staffCommentsForCustomer(String customerId) {
        return staffComments.byCustomer(customerId);
    }

    public Optional<CustomerStaffComments> staffCommentsForAppointment(String appointmentId) {
        return staffComments.findByAppointment(appointmentId);
    }

    public Optional<Feedback> ratingForAppointment(String appointmentId) {
        return feedback.all().stream().filter(f -> f.getAppointmentId().equals(appointmentId)).findFirst();
    }
}
