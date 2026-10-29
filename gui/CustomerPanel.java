package com.apu.asc.gui;

import com.apu.asc.model.Appointment;
import com.apu.asc.model.AppointmentStatus;
import com.apu.asc.model.Customer;
import com.apu.asc.model.CustomerStaffComments;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.Payment;
import com.apu.asc.model.ServiceKind;
import com.apu.asc.model.TechnicianJobFeedback;
import com.apu.asc.model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;

public class CustomerPanel extends JPanel {
    private final MainFrame frame;
    private final AddonCatalogPicker bookAddOnPicker = new AddonCatalogPicker();
    private final DefaultTableModel apptModel = new DefaultTableModel(
            new Object[]{"ID", "Status", "Work finished?", "Technician", "Tier", "Slot", "Start", "End", "Add-ons"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel payModel = new DefaultTableModel(
            new Object[]{"Payment ID", "Appointment", "Amount", "Paid at"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private JTextField custProfileName;
    private JTextField custProfileEmail;
    private JTextField custProfilePhone;
    private JTextField custProfileVeh;
    private JPasswordField custProfileCur;
    private JPasswordField custProfileNp;
    private JPasswordField custProfileNp2;
    private JLabel custProfileUsernameLabel;

    private final JComboBox<IdLabelOption> ratingApptCombo = new JComboBox<>();
    private final JComboBox<IdLabelOption> staffCommentApptCombo = new JComboBox<>();

    private final DefaultTableModel summaryModel = new DefaultTableModel(
            new Object[]{"Appointment", "Rating", "Your comment", "Tech feedback", "Counter comment", "Tech comment"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public CustomerPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout());
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> frame.showLogin());
        top.add(new JLabel("Customer"), BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book appointment", buildBook());
        tabs.addTab("My appointments", buildAppts());
        tabs.addTab("Payment history", buildPayments());
        tabs.addTab("Rate service (1–5)", buildRating());
        tabs.addTab("Comments: counter & technician", buildStaffComments());
        tabs.addTab("View appointment feedback", buildSummary());
        tabs.addTab("My profile", buildProfile());

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    public void onShow() {
        bookAddOnPicker.reload(frame);
        refreshAppointments();
        refreshPayments();
        refreshSummary();
        refreshCustomerCompletedAppointmentCombos();
        refreshCustomerProfileFields();
    }

    private void refreshCustomerProfileFields() {
        User u = frame.getCurrentUser();
        if (custProfileName == null || !(u instanceof Customer c)) {
            return;
        }
        custProfileName.setText(c.getFullName());
        custProfileEmail.setText(c.getEmail());
        custProfilePhone.setText(c.getPhone());
        custProfileVeh.setText(c.getVehicleRegistration());
        custProfileCur.setText("");
        custProfileNp.setText("");
        custProfileNp2.setText("");
        if (custProfileUsernameLabel != null) {
            custProfileUsernameLabel.setText(c.getUsername());
        }
    }

    private JPanel buildBook() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        JTextField veh = new JTextField(28);
        JComboBox<ServiceKind> tier = new JComboBox<>(ServiceKind.values());
        ScheduleDateTimePicker schedulePicker = new ScheduleDateTimePicker();
        JTextField notes = new JTextField(28);
        int y = 0;
        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Service tier"), g);
        g.gridx = 1;
        p.add(tier, g);
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Vehicle description"), g);
        g.gridx = 1;
        p.add(veh, g);
        g.gridx = 0;
        g.gridy = ++y;
        g.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Add-ons"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(bookAddOnPicker, g);
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Scheduled start"), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(schedulePicker, g);
        g.fill = GridBagConstraints.NONE;
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Notes"), g);
        g.gridx = 1;
        p.add(notes, g);
        JButton go = new JButton("Submit");
        go.addActionListener(e -> {
            Customer c = (Customer) frame.getCurrentUser();
            try {
                LocalDateTime t = schedulePicker.getDateTime();
                ServiceKind k = (ServiceKind) tier.getSelectedItem();
                Appointment a = frame.getServices().appointmentsSvc().createAppointment(
                        c.getId(),
                        veh.getText().trim(),
                        k,
                        bookAddOnPicker.getSelectedCodes(),
                        t,
                        notes.getText().trim(),
                        null
                );
                refreshAppointments();
                refreshCustomerCompletedAppointmentCombos();
                JOptionPane.showMessageDialog(this, "Appointment ID: " + a.getId());
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Use format yyyy-MM-dd HH:mm (or use the picker).", "Invalid date", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        g.gridx = 0;
        g.gridy = ++y;
        g.gridwidth = 2;
        p.add(go, g);
        return p;
    }

    private JPanel buildAppts() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JLabel hint = new JLabel(
                "<html><body style='width:520px'><b>Status</b> shows your booking progress. "
                        + "<b>Work finished?</b> is Yes when the technician has completed the service.</body></html>");
        JTable t = new JTable(apptModel);
        p.add(hint, BorderLayout.NORTH);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JButton ref = new JButton("Refresh");
        ref.addActionListener(e -> refreshAppointments());
        p.add(ref, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildPayments() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = new JTable(payModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JButton ref = new JButton("Refresh");
        ref.addActionListener(e -> refreshPayments());
        p.add(ref, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildRating() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        ratingApptCombo.setPrototypeDisplayValue(
                new IdLabelOption("id", "appt-id — Completed — vehicle"));
        JSpinner rating = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        JTextArea comment = new JTextArea(4, 28);
        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("Appointment (completed)"), g);
        g.gridx = 1;
        p.add(ratingApptCombo, g);
        g.gridx = 0;
        g.gridy = 1;
        p.add(new JLabel("Rating (1-5)"), g);
        g.gridx = 1;
        p.add(rating, g);
        g.gridx = 0;
        g.gridy = 2;
        p.add(new JLabel("Comment"), g);
        g.gridx = 1;
        p.add(new JScrollPane(comment), g);
        JButton send = new JButton("Submit rating");
        send.addActionListener(e -> {
            Customer c = (Customer) frame.getCurrentUser();
            try {
                IdLabelOption sel = (IdLabelOption) ratingApptCombo.getSelectedItem();
                if (sel == null) {
                    JOptionPane.showMessageDialog(this, "No completed appointments to rate yet.",
                            "Rating", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                frame.getServices().feedbackSvc().submitCustomerRating(
                        c.getId(),
                        sel.id(),
                        (Integer) rating.getValue(),
                        comment.getText().trim()
                );
                JOptionPane.showMessageDialog(this, "Thank you.");
                refreshSummary();
                refreshCustomerCompletedAppointmentCombos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        p.add(send, g);
        return p;
    }

    private JPanel buildStaffComments() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        staffCommentApptCombo.setPrototypeDisplayValue(
                new IdLabelOption("id", "appt-id — Completed — vehicle"));
        JTextArea counter = new JTextArea(3, 28);
        JTextArea technician = new JTextArea(3, 28);
        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("Appointment (completed)"), g);
        g.gridx = 1;
        p.add(staffCommentApptCombo, g);
        g.gridx = 0;
        g.gridy = 1;
        p.add(new JLabel("Comment on counter staff"), g);
        g.gridx = 1;
        p.add(new JScrollPane(counter), g);
        g.gridx = 0;
        g.gridy = 2;
        p.add(new JLabel("Comment on technician"), g);
        g.gridx = 1;
        p.add(new JScrollPane(technician), g);
        JButton save = new JButton("Save comments");
        save.addActionListener(e -> {
            Customer c = (Customer) frame.getCurrentUser();
            try {
                IdLabelOption sel = (IdLabelOption) staffCommentApptCombo.getSelectedItem();
                if (sel == null) {
                    JOptionPane.showMessageDialog(this, "No completed appointments yet.",
                            "Comments", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                frame.getServices().feedbackSvc().saveCustomerStaffComments(
                        c.getId(),
                        sel.id(),
                        counter.getText(),
                        technician.getText()
                );
                JOptionPane.showMessageDialog(this, "Saved.");
                refreshSummary();
                refreshCustomerCompletedAppointmentCombos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        p.add(save, g);
        return p;
    }

    private JPanel buildSummary() {
        JPanel p = new JPanel(new BorderLayout());
        JTable t = new JTable(summaryModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JButton ref = new JButton("Refresh");
        ref.addActionListener(e -> refreshSummary());
        p.add(ref, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildProfile() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        custProfileName = new JTextField(24);
        custProfileEmail = new JTextField(24);
        custProfilePhone = new JTextField(24);
        custProfileVeh = new JTextField(24);
        custProfileCur = new JPasswordField(24);
        custProfileNp = new JPasswordField(24);
        custProfileNp2 = new JPasswordField(24);
        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("Full name"), g);
        g.gridx = 1;
        p.add(custProfileName, g);
        g.gridx = 0;
        g.gridy = 1;
        p.add(new JLabel("Email"), g);
        g.gridx = 1;
        p.add(custProfileEmail, g);
        g.gridx = 0;
        g.gridy = 2;
        p.add(new JLabel("Phone"), g);
        g.gridx = 1;
        p.add(custProfilePhone, g);
        g.gridx = 0;
        g.gridy = 3;
        p.add(new JLabel("Vehicle registration"), g);
        g.gridx = 1;
        p.add(custProfileVeh, g);
        g.gridx = 0;
        g.gridy = 4;
        p.add(new JLabel("Username (sign-in & password reset)"), g);
        g.gridx = 1;
        custProfileUsernameLabel = new JLabel(" ");
        custProfileUsernameLabel.setForeground(new Color(60, 60, 60));
        p.add(custProfileUsernameLabel, g);
        g.gridx = 0;
        g.gridy = 5;
        p.add(new JLabel("Current password (if changing)"), g);
        g.gridx = 1;
        p.add(custProfileCur, g);
        g.gridx = 0;
        g.gridy = 6;
        p.add(new JLabel("New password (optional)"), g);
        g.gridx = 1;
        p.add(custProfileNp, g);
        g.gridx = 0;
        g.gridy = 7;
        p.add(new JLabel("Re-type new password"), g);
        g.gridx = 1;
        p.add(custProfileNp2, g);
        JButton save = new JButton("Save profile");
        save.addActionListener(e -> {
            try {
                User u = frame.getCurrentUser();
                if (!(u instanceof Customer c)) {
                    return;
                }
                String np = new String(custProfileNp.getPassword());
                String np2 = new String(custProfileNp2.getPassword());
                String pe = AccountFormValidation.validateOptionalPasswordChange(np, np2);
                if (pe != null) {
                    AccountFormValidation.warn(this, pe);
                    return;
                }
                String newPwOrNull = AccountFormValidation.isBlank(np) ? null : np;
                frame.getServices().profile().updateProfile(
                        u,
                        custProfileName.getText().trim(),
                        custProfileEmail.getText().trim(),
                        custProfilePhone.getText().trim(),
                        custProfileVeh.getText().trim(),
                        new String(custProfileCur.getPassword()),
                        newPwOrNull
                );
                frame.getServices().users().findById(c.getId()).ifPresent(frame::setCurrentUser);
                refreshCustomerProfileFields();
                JOptionPane.showMessageDialog(this, "Profile updated.");
                custProfileNp.setText("");
                custProfileNp2.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        g.gridx = 0;
        g.gridy = 8;
        g.gridwidth = 2;
        p.add(save, g);
        return p;
    }

    private void refreshCustomerCompletedAppointmentCombos() {
        ratingApptCombo.removeAllItems();
        staffCommentApptCombo.removeAllItems();
        User u = frame.getCurrentUser();
        if (!(u instanceof Customer c)) {
            return;
        }
        for (Appointment a : frame.getServices().appointmentsSvc().listForCustomer(c.getId())) {
            if (a.getStatus() != AppointmentStatus.COMPLETED) {
                continue;
            }
            String veh = a.getVehicleDescription() != null && !a.getVehicleDescription().isBlank()
                    ? a.getVehicleDescription()
                    : "(no vehicle note)";
            String label = a.getId() + " — Completed — " + veh;
            IdLabelOption opt = new IdLabelOption(a.getId(), label);
            ratingApptCombo.addItem(opt);
            staffCommentApptCombo.addItem(opt);
        }
    }

    private void refreshAppointments() {
        apptModel.setRowCount(0);
        String cid = frame.getCurrentUser().getId();
        var users = frame.getServices().users();
        for (Appointment a : frame.getServices().appointmentsSvc().listForCustomer(cid)) {
            apptModel.addRow(new Object[]{
                    a.getId(),
                    AppointmentStatusLabels.friendly(a.getStatus()),
                    AppointmentStatusLabels.workFinishedYesNo(a.getStatus()),
                    UserDisplayFormat.fromId(users, a.getTechnicianId()),
                    a.getPrimaryKind(),
                    a.getDurationMinutes() + " min",
                    a.getScheduledAt() != null ? a.getScheduledAt().toString() : "—",
                    a.getScheduledEnd() != null ? a.getScheduledEnd().toString() : "—",
                    String.join(", ", a.getServiceCodes())
            });
        }
    }

    private void refreshPayments() {
        payModel.setRowCount(0);
        String cid = frame.getCurrentUser().getId();
        for (Payment p : frame.getServices().paymentsSvc().paymentHistoryForCustomer(cid)) {
            payModel.addRow(new Object[]{p.getId(), p.getAppointmentId(), p.getAmount(), p.getPaidAt().toString()});
        }
    }

    private void refreshSummary() {
        summaryModel.setRowCount(0);
        String cid = frame.getCurrentUser().getId();
        for (Appointment a : frame.getServices().appointmentsSvc().listForCustomer(cid)) {
            String aid = a.getId();
            String rating = "";
            String custC = "";
            var fbOpt = frame.getServices().feedbackSvc().ratingForAppointment(aid);
            if (fbOpt.isPresent()) {
                Feedback f = fbOpt.get();
                rating = String.valueOf(f.getRating());
                custC = f.getComment();
            }
            String techFb = frame.getServices().feedbackSvc().technicianFeedbackAll().stream()
                    .filter(t -> t.getAppointmentId().equals(aid))
                    .map(TechnicianJobFeedback::getComment)
                    .findFirst().orElse("");
            var sc = frame.getServices().feedbackSvc().staffCommentsForAppointment(aid);
            String cc = sc.map(CustomerStaffComments::getCommentCounterStaff).orElse("");
            String tc = sc.map(CustomerStaffComments::getCommentTechnician).orElse("");
            summaryModel.addRow(new Object[]{aid, rating, custC, techFb, cc, tc});
        }
    }
}
