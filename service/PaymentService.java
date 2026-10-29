package com.apu.asc.service;

import com.apu.asc.model.*;
import com.apu.asc.repository.AppointmentRepository;
import com.apu.asc.repository.CatalogRepository;
import com.apu.asc.repository.PaymentRepository;
import com.apu.asc.repository.UserRepository;
import com.apu.asc.util.IdGenerator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class PaymentService {
    private final PaymentRepository payments;
    private final AppointmentRepository appointments;
    private final CatalogRepository catalog;
    private final UserRepository users;

    public PaymentService(PaymentRepository payments, AppointmentRepository appointments,
                          CatalogRepository catalog, UserRepository users) {
        this.payments = payments;
        this.appointments = appointments;
        this.catalog = catalog;
        this.users = users;
    }

    /**
     * Base price for NORMAL or MAJOR tier plus optional add-on codes (OIL, BRAKE, …).
     */
    public BigDecimal quoteTotal(Appointment a) {
        ServiceKind tier = a.getPrimaryKind();
        BigDecimal base = catalog.find(tier.catalogCode()).map(ServiceItem::getUnitPrice).orElse(BigDecimal.ZERO);
        BigDecimal addOns = BigDecimal.ZERO;
        for (String code : a.getServiceCodes()) {
            if (code.equalsIgnoreCase(ServiceKind.NORMAL.catalogCode())
                    || code.equalsIgnoreCase(ServiceKind.MAJOR.catalogCode())) {
                continue;
            }
            addOns = addOns.add(catalog.find(code).map(ServiceItem::getUnitPrice).orElse(BigDecimal.ZERO));
        }
        return base.add(addOns);
    }

    public Receipt collectPayment(String appointmentId, PaymentMethod method, String counterStaffId)
            throws IOException {
        Appointment a = appointments.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found."));
        if (a.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Payment allowed only after service is completed.");
        }
        if (payments.findByAppointmentId(appointmentId).isPresent()) {
            throw new IllegalStateException("Payment already recorded for this appointment.");
        }
        BigDecimal amount = quoteTotal(a);
        Payment p = new Payment(
                IdGenerator.newId(),
                appointmentId,
                a.getCustomerId(),
                amount,
                method,
                counterStaffId,
                LocalDateTime.now()
        );
        payments.add(p);
        String customerName = users.findById(a.getCustomerId()).map(User::getFullName).orElse("—");
        String summary = a.getId() + " — " + a.getVehicleDescription() + " (" + a.getPrimaryKind() + ")";
        return new Receipt(p.getId(), "APU Automotive Service Centre (ASC)", p, customerName, summary);
    }

    public java.util.List<Payment> paymentHistoryForCustomer(String customerId) {
        return payments.byCustomer(customerId);
    }

    public java.util.List<Payment> allPayments() {
        return payments.all();
    }

    public Optional<Payment> findPaymentForAppointment(String appointmentId) {
        return payments.findByAppointmentId(appointmentId);
    }
}
