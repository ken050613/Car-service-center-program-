package apu.service;

import apu.model.*;
import apu.repository.AppointmentRepository;
import apu.repository.PaymentRepository;
import apu.repository.UserRepository;
import apu.util.IdGenerator;
import apu.util.ValidationUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AppointmentService {
    private final AppointmentRepository appointments;
    private final UserRepository users;
    private final PaymentRepository payments;

    public AppointmentService(AppointmentRepository appointments, UserRepository users, PaymentRepository payments) {
        this.appointments = appointments;
        this.users = users;
        this.payments = payments;
    }

    public Appointment createAppointment(String customerId, String vehicleDescription, ServiceKind primaryKind,
                                         List<String> addOnServiceCodes, LocalDateTime scheduledAt, String notes,
                                         String createdByCounterStaffId) throws IOException {
        ValidationUtil.requireNonBlank(customerId, "Customer id");
        users.findById(customerId).filter(u -> u instanceof Customer)
                .orElseThrow(() -> new IllegalArgumentException("Invalid customer id."));
        ServiceKind tier = primaryKind != null ? primaryKind : ServiceKind.NORMAL;
        Appointment a = new Appointment(
                IdGenerator.newId(),
                customerId,
                vehicleDescription,
                addOnServiceCodes,
                tier,
                AppointmentStatus.SCHEDULED,
                null,
                createdByCounterStaffId,
                scheduledAt,
                notes,
                LocalDateTime.now()
        );
        appointments.add(a);
        return a;
    }

    /** Counter staff assigns a technician; status becomes ASSIGNED. Enforces no overlapping slots for the technician. */
    public Appointment assignTechnician(String appointmentId, String technicianId) throws IOException {
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        users.findById(technicianId).filter(u -> u instanceof Technician)
                .orElseThrow(() -> new IllegalArgumentException("Invalid technician id."));
        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot assign a cancelled appointment.");
        }
        if (a.getScheduledAt() == null) {
            throw new IllegalStateException("Appointment has no scheduled time.");
        }
        for (Appointment o : appointments.byTechnician(technicianId)) {
            if (o.getId().equals(a.getId())) {
                continue;
            }
            if (o.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }
            if (timeOverlap(a, o)) {
                throw new IllegalStateException("Technician is not available (time overlaps another appointment).");
            }
        }
        a.setTechnicianId(technicianId);
        a.setStatus(AppointmentStatus.ASSIGNED);
        appointments.update(a);
        return a;
    }

    private static boolean timeOverlap(Appointment a, Appointment b) {
        if (a.getScheduledAt() == null || b.getScheduledAt() == null) {
            return false;
        }
        LocalDateTime aEnd = a.getScheduledEnd();
        LocalDateTime bEnd = b.getScheduledEnd();
        if (aEnd == null || bEnd == null) {
            return false;
        }
        return a.getScheduledAt().isBefore(bEnd) && aEnd.isAfter(b.getScheduledAt());
    }

    /** Technician starts work (ASSIGNED → IN_PROGRESS). */
    public Appointment markInProgressByTechnician(String appointmentId, String technicianId) throws IOException {
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (!technicianId.equals(a.getTechnicianId())) {
            throw new SecurityException("Not assigned to this technician.");
        }
        if (a.getStatus() != AppointmentStatus.ASSIGNED) {
            throw new IllegalStateException("Only ASSIGNED jobs can be marked in progress.");
        }
        a.setStatus(AppointmentStatus.IN_PROGRESS);
        appointments.update(a);
        return a;
    }

    /**
     * Technicians may move assigned work to COMPLETED (from ASSIGNED or IN_PROGRESS).
     */
    public Appointment markCompletedByTechnician(String appointmentId, String technicianId) throws IOException {
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (!technicianId.equals(a.getTechnicianId())) {
            throw new SecurityException("Not assigned to this technician.");
        }
        if (a.getStatus() != AppointmentStatus.ASSIGNED && a.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Mark in progress first, or job is already finished / not assignable.");
        }
        a.setStatus(AppointmentStatus.COMPLETED);
        appointments.update(a);
        return a;
    }

    public List<Appointment> listForCustomer(String customerId) {
        return appointments.byCustomer(customerId);
    }

    public List<Appointment> listForTechnician(String technicianId) {
        return appointments.byTechnician(technicianId);
    }

    public List<Appointment> listAll() {
        return appointments.all();
    }

    public Optional<Appointment> find(String id) {
        return appointments.findById(id);
    }

    public Appointment cancelAppointment(String appointmentId, String actorUserId, Role actorRole) throws IOException {
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (actorRole == Role.CUSTOMER && !actorUserId.equals(a.getCustomerId())) {
            throw new SecurityException("You can only cancel your own appointments.");
        }
        if (actorRole == Role.TECHNICIAN) {
            throw new SecurityException("Cancellation is not available from the technician portal.");
        }
        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Already cancelled.");
        }
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed appointment.");
        }
        if (payments.findByAppointmentId(appointmentId).isPresent()) {
            throw new IllegalStateException("Cannot cancel: a payment exists for this appointment.");
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        appointments.update(a);
        return a;
    }
}
