package apu.gui;

import apu.model.AppointmentStatus;

/** Short labels for appointment tables (easy to see if work is finished). */
public final class AppointmentStatusLabels {
    private AppointmentStatusLabels() {}

    public static String friendly(AppointmentStatus s) {
        if (s == null) {
            return "—";
        }
        return switch (s) {
            case SCHEDULED -> "Scheduled";
            case ASSIGNED -> "Assigned";
            case IN_PROGRESS -> "In progress";
            case COMPLETED -> "Completed";
            case CANCELLED -> "Cancelled";
        };
    }

    /** Technician / customer: is the service finished? */
    public static String workFinishedYesNo(AppointmentStatus s) {
        return s == AppointmentStatus.COMPLETED ? "Yes" : "No";
    }
}
