package apu.ui;

import apu.bootstrap.AscServices;
import apu.model.*;
import apu.util.PasswordUtil;
import apu.util.ServiceCodeParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUi {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AscServices s;
    private final Scanner in = new Scanner(System.in);
    private User session;

    public ConsoleUi(AscServices s) {
        this.s = s;
    }

    public void run() {
        System.out.println("=== APU Automotive Service Centre (APU-ASC) ===");
        while (true) {
            if (session == null) {
                if (!mainMenu()) {
                    break;
                }
            } else {
                Role r = session.getRole();
                boolean stay = switch (r) {
                    case MANAGER -> managerMenu();
                    case COUNTER_STAFF -> counterMenu();
                    case TECHNICIAN -> technicianMenu();
                    case CUSTOMER -> customerMenu();
                };
                if (!stay) {
                    session = null;
                }
            }
        }
        System.out.println("Goodbye.");
    }

    private boolean mainMenu() {
        System.out.println("\n[1] Login  [2] Register (customer)  [0] Exit");
        String c = in.nextLine().trim();
        try {
            switch (c) {
                case "1" -> login();
                case "2" -> registerCustomer();
                case "0" -> {
                    return false;
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    private void login() {
        System.out.print("Username: ");
        String u = in.nextLine().trim();
        System.out.print("Password: ");
        String p = in.nextLine();
        s.auth().login(u, p).ifPresentOrElse(
                user -> {
                    session = user;
                    System.out.println("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
                },
                () -> System.out.println("Invalid credentials.")
        );
    }

    private void registerCustomer() throws IOException {
        System.out.print("Username: ");
        String u = in.nextLine().trim();
        System.out.print("Password: ");
        String p = in.nextLine();
        System.out.print("Full name: ");
        String name = in.nextLine().trim();
        System.out.print("Email: ");
        String email = in.nextLine().trim();
        System.out.print("Phone: ");
        String phone = in.nextLine().trim();
        System.out.print("Vehicle registration: ");
        String reg = in.nextLine().trim();
        s.registration().registerCustomer(u, p, name, email, phone, reg);
        System.out.println("Registration successful. You can login now.");
    }

    private boolean managerMenu() {
        System.out.println("\n--- Manager ---");
        System.out.println("[1] Set / update service price");
        System.out.println("[2] View price catalogue");
        System.out.println("[3] Register counter staff");
        System.out.println("[4] Register technician");
        System.out.println("[5] View all appointments");
        System.out.println("[6] View analysed report");
        System.out.println("[7] View customer feedback");
        System.out.println("[0] Logout");
        String c = in.nextLine().trim();
        try {
            switch (c) {
                case "1" -> setPrice();
                case "2" -> viewCatalog();
                case "3" -> registerCounter();
                case "4" -> registerTech();
                case "5" -> viewAllAppointments();
                case "6" -> System.out.println(s.reports().buildSummaryReport());
                case "7" -> viewFeedback();
                case "0" -> {
                    return false;
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    private void setPrice() throws IOException {
        System.out.print("Service code (e.g. OIL): ");
        String code = in.nextLine().trim().toUpperCase();
        System.out.print("Description: ");
        String desc = in.nextLine().trim();
        System.out.print("Unit price: ");
        BigDecimal price = new BigDecimal(in.nextLine().trim());
        s.pricing().setOrUpdatePrice(code, desc, price);
        System.out.println("Price saved.");
    }

    private void viewCatalog() {
        s.pricing().listPrices().forEach(item ->
                System.out.println(item.getCode() + " | " + item.getDescription() + " | " + item.getUnitPrice()));
    }

    private void registerCounter() throws IOException {
        System.out.print("Username: ");
        String u = in.nextLine().trim();
        System.out.print("Password: ");
        String p = in.nextLine();
        System.out.print("Full name: ");
        String name = in.nextLine().trim();
        System.out.print("Email: ");
        String email = in.nextLine().trim();
        s.registration().registerCounterStaff(u, p, name, email);
        System.out.println("Counter staff registered.");
    }

    private void registerTech() throws IOException {
        System.out.print("Username: ");
        String u = in.nextLine().trim();
        System.out.print("Password: ");
        String p = in.nextLine();
        System.out.print("Full name: ");
        String name = in.nextLine().trim();
        System.out.print("Email: ");
        String email = in.nextLine().trim();
        System.out.print("Specialty: ");
        String spec = in.nextLine().trim();
        s.registration().registerTechnician(u, p, name, email, spec);
        System.out.println("Technician registered.");
    }

    private void viewAllAppointments() {
        s.appointmentsSvc().listAll().forEach(this::printAppointment);
    }

    private boolean counterMenu() {
        System.out.println("\n--- Counter Staff ---");
        System.out.println("[1] Create appointment (walk-in / by customer id)");
        System.out.println("[2] List customers");
        System.out.println("[3] List appointments");
        System.out.println("[4] Assign technician to appointment");
        System.out.println("[5] Collect payment & print receipt");
        System.out.println("[0] Logout");
        String c = in.nextLine().trim();
        try {
            switch (c) {
                case "1" -> createAppointmentCounter();
                case "2" -> listCustomers();
                case "3" -> viewAllAppointments();
                case "4" -> assignAppointment();
                case "5" -> collectPayment();
                case "0" -> {
                    return false;
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    private void listCustomers() {
        s.users().customers().forEach(c ->
                System.out.println(c.getId() + " | " + c.getUsername() + " | " + c.getFullName()));
    }

    private void createAppointmentCounter() throws IOException {
        System.out.print("Customer user id: ");
        String cid = in.nextLine().trim();
        System.out.print("Tier NORMAL or MAJOR: ");
        ServiceKind tier = ServiceKind.valueOf(in.nextLine().trim().toUpperCase());
        System.out.print("Vehicle description: ");
        String veh = in.nextLine().trim();
        System.out.println("Add-on codes:");
        viewCatalog();
        System.out.print("Comma-separated add-on codes: ");
        String codes = in.nextLine().trim();
        List<String> list = ServiceCodeParser.parseCommaSeparated(codes);
        System.out.print("Scheduled at (" + DT + "): ");
        LocalDateTime when = LocalDateTime.parse(in.nextLine().trim(), DT);
        System.out.print("Notes: ");
        String notes = in.nextLine().trim();
        Appointment a = s.appointmentsSvc().createAppointment(cid, veh, tier, list, when, notes, session.getId());
        System.out.println("Created appointment id: " + a.getId() + " slot " + tier.getDurationMinutes() + " min");
    }

    private void assignAppointment() throws IOException {
        System.out.print("Appointment id: ");
        String aid = in.nextLine().trim();
        System.out.println("Technicians:");
        s.users().technicians().forEach(t ->
                System.out.println(t.getId() + " | " + t.getFullName() + " | " + t.getSpecialty()));
        System.out.print("Technician id: ");
        String tid = in.nextLine().trim();
        s.appointmentsSvc().assignTechnician(aid, tid);
        System.out.println("Assigned.");
    }

    private void collectPayment() throws IOException {
        System.out.print("Appointment id: ");
        String aid = in.nextLine().trim();
        var ap = s.appointmentsSvc().find(aid).orElseThrow(() -> new IllegalArgumentException("Not found."));
        System.out.println("Quoted total: " + s.paymentsSvc().quoteTotal(ap));
        System.out.print("Payment method (CASH/CARD/ONLINE): ");
        PaymentMethod m = PaymentMethod.valueOf(in.nextLine().trim().toUpperCase());
        Receipt r = s.paymentsSvc().collectPayment(aid, m, session.getId());
        System.out.println(r.formatText());
    }

    private boolean technicianMenu() {
        System.out.println("\n--- Technician ---");
        System.out.println("[1] My assigned appointments");
        System.out.println("[2] Mark appointment COMPLETED");
        System.out.println("[0] Logout");
        String c = in.nextLine().trim();
        try {
            switch (c) {
                case "1" -> s.appointmentsSvc().listForTechnician(session.getId()).forEach(this::printAppointment);
                case "2" -> markCompletedTechnician();
                case "0" -> {
                    return false;
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    private void markCompletedTechnician() throws IOException {
        System.out.print("Appointment id: ");
        String aid = in.nextLine().trim();
        s.appointmentsSvc().markCompletedByTechnician(aid, session.getId());
        System.out.println("Marked COMPLETED.");
    }

    private boolean customerMenu() {
        System.out.println("\n--- Customer ---");
        System.out.println("[1] New appointment");
        System.out.println("[2] My appointments & service history");
        System.out.println("[3] Payment history");
        System.out.println("[4] Submit feedback");
        System.out.println("[5] Change password");
        System.out.println("[0] Logout");
        String c = in.nextLine().trim();
        try {
            switch (c) {
                case "1" -> createAppointmentCustomer();
                case "2" -> myServiceHistory();
                case "3" -> myPaymentHistory();
                case "4" -> submitFeedback();
                case "5" -> changePassword();
                case "0" -> {
                    return false;
                }
                default -> System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return true;
    }

    private void createAppointmentCustomer() throws IOException {
        Customer cust = (Customer) session;
        System.out.print("Tier NORMAL or MAJOR: ");
        ServiceKind tier = ServiceKind.valueOf(in.nextLine().trim().toUpperCase());
        System.out.print("Vehicle description: ");
        String veh = in.nextLine().trim();
        System.out.println("Add-on codes:");
        viewCatalog();
        System.out.print("Comma-separated add-on codes: ");
        String codes = in.nextLine().trim();
        List<String> list = ServiceCodeParser.parseCommaSeparated(codes);
        System.out.print("Scheduled at (" + DT + "): ");
        LocalDateTime when;
        try {
            when = LocalDateTime.parse(in.nextLine().trim(), DT);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
            return;
        }
        System.out.print("Notes: ");
        String notes = in.nextLine().trim();
        Appointment a = s.appointmentsSvc().createAppointment(cust.getId(), veh, tier, list, when, notes, null);
        System.out.println("Appointment id: " + a.getId() + " (status: " + a.getStatus() + ")");
    }

    private void myServiceHistory() {
        Customer cust = (Customer) session;
        s.appointmentsSvc().listForCustomer(cust.getId()).forEach(this::printAppointment);
    }

    private void myPaymentHistory() {
        Customer cust = (Customer) session;
        s.paymentsSvc().paymentHistoryForCustomer(cust.getId()).forEach(p ->
                System.out.println(p.getId() + " | " + p.getAppointmentId() + " | " + p.getAmount() + " | " + p.getPaidAt()));
    }

    private void submitFeedback() throws IOException {
        Customer cust = (Customer) session;
        System.out.print("Appointment id: ");
        String aid = in.nextLine().trim();
        System.out.print("Rating 1-5: ");
        int r = Integer.parseInt(in.nextLine().trim());
        System.out.print("Comment: ");
        String comment = in.nextLine().trim();
        s.feedbackSvc().submitCustomerRating(cust.getId(), aid, r, comment);
        System.out.println("Thank you for your feedback.");
    }

    private void changePassword() throws IOException {
        System.out.print("Current password: ");
        String cur = in.nextLine();
        if (!PasswordUtil.matches(cur, session.getPasswordHash())) {
            System.out.println("Wrong password.");
            return;
        }
        System.out.print("New password: ");
        String np = in.nextLine();
        session.setPasswordHash(PasswordUtil.hash(np));
        s.users().replace(session);
        System.out.println("Password updated.");
    }

    private void viewFeedback() {
        s.feedbackSvc().allFeedback().forEach(f ->
                System.out.println(f.getId() + " | appt " + f.getAppointmentId() + " | " + f.getRating() + " | " + f.getComment()));
    }

    private void printAppointment(Appointment a) {
        System.out.println(a.getId()
                + " | customer=" + a.getCustomerId()
                + " | status=" + a.getStatus()
                + " | tier=" + a.getPrimaryKind()
                + " | tech=" + a.getTechnicianId()
                + " | scheduled=" + a.getScheduledAt()
                + " | end=" + a.getScheduledEnd()
                + " | addOns=" + a.getServiceCodes());
    }

}
