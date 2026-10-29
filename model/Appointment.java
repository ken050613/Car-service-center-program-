package apu.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Appointment {
    private final String id;
    private final String customerId;
    private String vehicleDescription;
    /** Add-on service codes (oil, brakes, etc.); base price comes from {@link #primaryKind}. */
    private final List<String> serviceCodes;
    /** Normal = 1h slot, Major = 3h slot (Table 1.0). */
    private ServiceKind primaryKind;
    private AppointmentStatus status;
    private String technicianId;
    /** Counter who registered the booking (optional). */
    private String createdByCounterStaffId;
    private LocalDateTime scheduledAt;
    private String notes;
    private final LocalDateTime createdAt;

    public Appointment(String id, String customerId, String vehicleDescription, List<String> serviceCodes,
                       ServiceKind primaryKind, AppointmentStatus status, String technicianId,
                       String createdByCounterStaffId, LocalDateTime scheduledAt, String notes,
                       LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.vehicleDescription = vehicleDescription != null ? vehicleDescription : "";
        this.serviceCodes = serviceCodes != null ? new ArrayList<>(serviceCodes) : new ArrayList<>();
        this.primaryKind = primaryKind;
        this.status = status != null ? status : AppointmentStatus.SCHEDULED;
        this.technicianId = technicianId;
        this.createdByCounterStaffId = createdByCounterStaffId;
        this.scheduledAt = scheduledAt;
        this.notes = notes != null ? notes : "";
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public ServiceKind getPrimaryKind() {
        return primaryKind != null ? primaryKind : ServiceKind.NORMAL;
    }

    public void setPrimaryKind(ServiceKind primaryKind) {
        this.primaryKind = primaryKind;
    }

    public int getDurationMinutes() {
        return getPrimaryKind().getDurationMinutes();
    }

    public LocalDateTime getScheduledEnd() {
        if (scheduledAt == null) {
            return null;
        }
        return scheduledAt.plusMinutes(getDurationMinutes());
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getVehicleDescription() {
        return vehicleDescription;
    }

    public void setVehicleDescription(String vehicleDescription) {
        this.vehicleDescription = vehicleDescription;
    }

    public List<String> getServiceCodes() {
        return new ArrayList<>(serviceCodes);
    }

    public void setServiceCodes(List<String> serviceCodes) {
        this.serviceCodes.clear();
        if (serviceCodes != null) {
            this.serviceCodes.addAll(serviceCodes);
        }
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public String getCreatedByCounterStaffId() {
        return createdByCounterStaffId;
    }

    public void setCreatedByCounterStaffId(String createdByCounterStaffId) {
        this.createdByCounterStaffId = createdByCounterStaffId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
