package apu.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Feedback {
    private final String id;
    private final String appointmentId;
    private final String customerId;
    private final int rating;
    private final String comment;
    private final LocalDateTime createdAt;

    public Feedback(String id, String appointmentId, String customerId, int rating, String comment,
                    LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.appointmentId = Objects.requireNonNull(appointmentId);
        this.customerId = Objects.requireNonNull(customerId);
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be 1-5");
        }
        this.rating = rating;
        this.comment = comment != null ? comment : "";
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

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
