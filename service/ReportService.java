package com.apu.asc.service;

import com.apu.asc.model.Appointment;
import com.apu.asc.model.AppointmentStatus;
import com.apu.asc.model.Payment;
import com.apu.asc.model.ServiceKind;
import com.apu.asc.repository.AppointmentRepository;
import com.apu.asc.repository.FeedbackRepository;
import com.apu.asc.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Analysed reports for managers: revenue, volumes, satisfaction.
 */
public class ReportService {
    private final PaymentRepository payments;
    private final AppointmentRepository appointments;
    private final FeedbackRepository feedback;

    public ReportService(PaymentRepository payments, AppointmentRepository appointments, FeedbackRepository feedback) {
        this.payments = payments;
        this.appointments = appointments;
        this.feedback = feedback;
    }

    public BigDecimal totalRevenue() {
        return payments.all().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<AppointmentStatus, Long> appointmentsByStatus() {
        Map<AppointmentStatus, Long> map = new EnumMap<>(AppointmentStatus.class);
        for (AppointmentStatus s : AppointmentStatus.values()) {
            map.put(s, 0L);
        }
        for (Appointment a : appointments.all()) {
            map.merge(a.getStatus(), 1L, Long::sum);
        }
        return map;
    }

    public long completedAppointments() {
        return appointments.all().stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
    }

    public long totalAppointmentCount() {
        return appointments.all().size();
    }

    /** Count by primary tier (NORMAL = 1h slot, MAJOR = 3h slot). */
    public Map<ServiceKind, Long> appointmentsByTier() {
        Map<ServiceKind, Long> map = new EnumMap<>(ServiceKind.class);
        for (ServiceKind k : ServiceKind.values()) {
            map.put(k, 0L);
        }
        for (Appointment a : appointments.all()) {
            map.merge(a.getPrimaryKind(), 1L, Long::sum);
        }
        return map;
    }

    /**
     * Most frequently used add-on catalogue code across all appointments (empty if none).
     */
    public String mostUsedAddOnCodeSummary() {
        Map<String, Long> counts = new HashMap<>();
        for (Appointment a : appointments.all()) {
            for (String code : a.getServiceCodes()) {
                String c = code != null ? code.trim().toUpperCase() : "";
                if (!c.isEmpty()) {
                    counts.merge(c, 1L, Long::sum);
                }
            }
        }
        if (counts.isEmpty()) {
            return "No add-on codes recorded yet.";
        }
        return counts.entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + " booking(s))")
                .orElse("—");
    }

    public double averageFeedbackRating() {
        var list = feedback.all();
        if (list.isEmpty()) {
            return 0;
        }
        double sum = list.stream().mapToInt(f -> f.getRating()).sum();
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public String buildSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- APU-ASC Management Report ---\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n");
        sb.append("Total revenue (all time): ").append(totalRevenue()).append("\n");
        sb.append("Total appointments (all time): ").append(totalAppointmentCount()).append("\n");
        sb.append("Completed appointments: ").append(completedAppointments()).append("\n");
        sb.append("Appointments by status:\n");
        appointmentsByStatus().forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
        sb.append("Appointments by service tier (slot length):\n");
        appointmentsByTier().forEach((k, v) -> sb.append("  ")
                .append(k.name()).append(" (").append(k.getDurationMinutes()).append(" min): ")
                .append(v).append("\n"));
        sb.append("Most used add-on (catalogue code): ").append(mostUsedAddOnCodeSummary()).append("\n");
        sb.append("Average feedback rating (1-5): ").append(averageFeedbackRating()).append("\n");
        return sb.toString();
    }
}
