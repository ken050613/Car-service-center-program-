package apu.bootstrap;

import apu.repository.*;
import apu.service.*;

public record AscServices(
        UserRepository users,
        CatalogRepository catalog,
        AppointmentRepository appointments,
        PaymentRepository payments,
        FeedbackRepository feedback,
        TechnicianFeedbackRepository technicianFeedbackRepo,
        CustomerStaffCommentRepository customerStaffCommentRepo,
        AuthService auth,
        RegistrationService registration,
        StaffAdminService staffAdmin,
        CustomerManagementService customerMgmt,
        ProfileService profile,
        PricingService pricing,
        AppointmentService appointmentsSvc,
        PaymentService paymentsSvc,
        FeedbackService feedbackSvc,
        ReportService reports,
        AccountAuditService accountAudit
) {}
