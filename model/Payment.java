package apu.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Payment {
    private final String id;
    private final String appointmentId;
    private final String customerId;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final String receivedByStaffId;
    private final LocalDateTime paidAt;

    public Payment(String id, String appointmentId, String customerId, BigDecimal amount, PaymentMethod method,
                   String receivedByStaffId, LocalDateTime paidAt) {
        this.id = Objects.requireNonNull(id);
        this.appointmentId = Objects.requireNonNull(appointmentId);
        this.customerId = Objects.requireNonNull(customerId);
        this.amount = Objects.requireNonNull(amount);
        this.method = method != null ? method : PaymentMethod.CASH;
        this.receivedByStaffId = receivedByStaffId;
        this.paidAt = paidAt != null ? paidAt : LocalDateTime.now();
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

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public String getReceivedByStaffId() {
        return receivedByStaffId;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
