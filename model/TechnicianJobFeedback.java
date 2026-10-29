package apu.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Feedback from a technician about an assigned appointment (coursework requirement). */
public class TechnicianJobFeedback {
    private final String id;
    private final String appointmentId;
    private final String technicianId;
    private final String comment;
    private final LocalDateTime createdAt;

    public TechnicianJobFeedback(String id, String appointmentId, String technicianId, String comment,
                                 LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.appointmentId = Objects.requireNonNull(appointmentId);
        this.technicianId = Objects.requireNonNull(technicianId);
        this.comment = comment != null ? comment : "";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
