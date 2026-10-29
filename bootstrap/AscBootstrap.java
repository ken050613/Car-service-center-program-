package apu.bootstrap;

import apu.model.Manager;
import apu.repository.*;
import apu.service.*;
import apu.util.IdGenerator;
import apu.util.PasswordUtil;

import java.io.IOException;

/** Loads text-file stores, seeds defaults, wires services. */
public final class AscBootstrap {
    private AscBootstrap() {}

    public static AscServices init() throws IOException {
        UserRepository users = new UserRepository();
        CatalogRepository catalog = new CatalogRepository();
        AppointmentRepository appointments = new AppointmentRepository();
        PaymentRepository payments = new PaymentRepository();
        FeedbackRepository feedback = new FeedbackRepository();
        TechnicianFeedbackRepository technicianFeedbackRepo = new TechnicianFeedbackRepository();
        CustomerStaffCommentRepository customerStaffCommentRepo = new CustomerStaffCommentRepository();

        users.load();
        catalog.load();
        appointments.load();
        payments.load();
        feedback.load();
        technicianFeedbackRepo.load();
        customerStaffCommentRepo.load();

        catalog.seedDefaultsIfEmpty();

        if (users.allUsers().stream().noneMatch(u -> u instanceof Manager)) {
            Manager admin = new Manager(
                    IdGenerator.newId(),
                    "admin",
                    PasswordUtil.hash("admin123"),
                    "Default Manager",
                    "manager@apu-asc.local"
            );
            users.add(admin);
        }

        AuthService auth = new AuthService(users);
        RegistrationService registration = new RegistrationService(users);
        StaffAdminService staffAdmin = new StaffAdminService(users);
        CustomerManagementService customerMgmt = new CustomerManagementService(users, appointments);
        ProfileService profile = new ProfileService(users);
        PricingService pricing = new PricingService(catalog);
        AccountAuditService accountAudit = new AccountAuditService();
        AppointmentService appointmentService = new AppointmentService(appointments, users, payments);
        PaymentService paymentService = new PaymentService(payments, appointments, catalog, users);
        FeedbackService feedbackService = new FeedbackService(
                feedback, technicianFeedbackRepo, customerStaffCommentRepo, appointments, users);
        ReportService reportService = new ReportService(payments, appointments, feedback);

        return new AscServices(users, catalog, appointments, payments, feedback,
                technicianFeedbackRepo, customerStaffCommentRepo,
                auth, registration, staffAdmin, customerMgmt, profile, pricing,
                appointmentService, paymentService, feedbackService, reportService, accountAudit);
    }
}
