package com.apu.asc.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Immutable view / document generated from a completed payment (composition).
 */
public final class Receipt {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String receiptNumber;
    private final String centreName;
    private final Payment payment;
    private final String customerName;
    private final String appointmentSummary;

    public Receipt(String receiptNumber, String centreName, Payment payment, String customerName,
                   String appointmentSummary) {
        this.receiptNumber = Objects.requireNonNull(receiptNumber);
        this.centreName = centreName != null ? centreName : "APU Automotive Service Centre";
        this.payment = Objects.requireNonNull(payment);
        this.customerName = customerName != null ? customerName : "";
        this.appointmentSummary = appointmentSummary != null ? appointmentSummary : "";
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public Payment getPayment() {
        return payment;
    }

    public BigDecimal getAmount() {
        return payment.getAmount();
    }

    /** Printable receipt text for console or file export. */
    public String formatText() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== RECEIPT ==========\n");
        sb.append(centreName).append("\n");
        sb.append("Receipt No: ").append(receiptNumber).append("\n");
        sb.append("Date: ").append(payment.getPaidAt().format(FMT)).append("\n");
        sb.append("Customer: ").append(customerName).append("\n");
        sb.append("Appointment: ").append(appointmentSummary).append("\n");
        sb.append("Amount: ").append(payment.getAmount()).append("\n");
        sb.append("Method: ").append(payment.getMethod()).append("\n");
        sb.append("=============================\n");
        return sb.toString();
    }
}
