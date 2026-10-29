package apu.gui;

import apu.model.Appointment;
import apu.model.AppointmentStatus;
import apu.model.CounterStaff;
import apu.model.Customer;
import apu.model.PaymentMethod;
import apu.model.ServiceKind;
import apu.model.User;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class CounterPanel extends JPanel {
    private final MainFrame frame;
    private final CustomerAccountsPanel customerAccountsPanel;
    private final AddonCatalogPicker bookAddOnPicker = new AddonCatalogPicker();
    private final DefaultTableModel apptModel = new DefaultTableModel(
            new Object[]{"ID", "Customer", "Status", "Work finished?", "Technician", "Tier", "Slot", "Start", "End", "Add-ons"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTextArea receiptArea = new JTextArea(12, 40);
    private JTextField profileName;
    private JTextField profileEmail;
    private JPasswordField profileCur;
    private JPasswordField profileNp;
    private JPasswordField profileNp2;
    private JLabel profileIdLabel;

    private final JComboBox<IdLabelOption> newApptCustomerCombo = new JComboBox<>();
    private final JComboBox<IdLabelOption> assignApptCombo = new JComboBox<>();
    private final JComboBox<TechnicianOption> assignTechCombo = new JComboBox<>();
    private final JComboBox<IdLabelOption> payApptCombo = new JComboBox<>();
    private final JComboBox<IdLabelOption> cancelApptCombo = new JComboBox<>();
    private Runnable refreshBookPriceEstimate;

    public CounterPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout());
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> frame.showLogin());
        top.add(new JLabel("Counter"), BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        customerAccountsPanel = new CustomerAccountsPanel(frame, this::refreshPickerCombos);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book", buildBookTab());
        tabs.addTab("Accounts", customerAccountsPanel);
        tabs.addTab("Appointments", buildApptTab());
        tabs.addTab("Assign", buildAssignTab());
        tabs.addTab("Payment", buildPayTab());
        tabs.addTab("Profile", buildProfileTab());

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    public void onShow() {
        bookAddOnPicker.reload(frame);
        if (refreshBookPriceEstimate != null) {
            refreshBookPriceEstimate.run();
        }
        refreshCustomers();
        refreshAppointments();
        refreshProfileFields();
    }

    private void refreshProfileFields() {
        User u = frame.getCurrentUser();
        if (profileName == null || !(u instanceof CounterStaff cs)) {
            return;
        }
        profileName.setText(cs.getFullName());
        profileEmail.setText(cs.getEmail());
        profileCur.setText("");
        profileNp.setText("");
        profileNp2.setText("");
        if (profileIdLabel != null) {
            profileIdLabel.setText(cs.getUsername());
        }
    }

    private JPanel buildBookTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        newApptCustomerCombo.setPrototypeDisplayValue(
                new IdLabelOption("id", "Customer Name — @username [customer-id]"));
        JTextField veh = new JTextField(28);
        JComboBox<ServiceKind> tier = new JComboBox<>(ServiceKind.values());
        ScheduleDateTimePicker schedulePicker = new ScheduleDateTimePicker(frame);
        JTextField notes = new JTextField(28);
        JLabel bookTotalLabel = new JLabel(" ");
        bookTotalLabel.setForeground(new Color(0, 100, 40));
        Runnable updateBookTotal = () -> {
            try {
                ServiceKind k = (ServiceKind) tier.getSelectedItem();
                BigDecimal total = frame.getServices().paymentsSvc().quoteBookingTotal(k, bookAddOnPicker.getSelectedCodes());
                bookTotalLabel.setText("Total (pay after service): " + total.toPlainString());
            } catch (Exception ex) {
                bookTotalLabel.setText("Total: —");
            }
        };
        bookAddOnPicker.setOnSelectionChange(updateBookTotal);
        tier.addActionListener(e -> updateBookTotal.run());
        this.refreshBookPriceEstimate = updateBookTotal;
        updateBookTotal.run();
        int y = 0;
        g.gridx = 0;
        g.gridy = y;
        p.add(new JLabel("Customer"), g);
        g.gridx = 1;
        p.add(newApptCustomerCombo, g);
        g.gridx = 0;
        g.gridy = ++y;
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
        g.gridx = 0;
        g.gridy = ++y;
        p.add(new JLabel("Total"), g);
        g.gridx = 1;
        g.anchor = GridBagConstraints.WEST;
        p.add(bookTotalLabel, g);
        g.anchor = GridBagConstraints.WEST;

        JButton go = new JButton("Create");
        go.addActionListener(e -> {
            try {
                IdLabelOption custSel = (IdLabelOption) newApptCustomerCombo.getSelectedItem();
                if (custSel == null) {
                    JOptionPane.showMessageDialog(this, "Add a customer first, or refresh the list.",
                            "No customer", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                LocalDateTime t = schedulePicker.getDateTime();
                ServiceKind k = (ServiceKind) tier.getSelectedItem();
                var a = frame.getServices().appointmentsSvc().createAppointment(
                        custSel.id(),
                        veh.getText().trim(),
                        k,
                        bookAddOnPicker.getSelectedCodes(),
                        t,
                        notes.getText().trim(),
                        frame.getCurrentUser().getId()
                );
                BigDecimal quoted = frame.getServices().paymentsSvc().quoteTotal(a);
                refreshAppointments();
                JOptionPane.showMessageDialog(this, "Created appointment ID:\n" + a.getId()
                        + "\nSlot: " + k.getDurationMinutes() / 60 + " h"
                        + "\nEstimated total: " + quoted.toPlainString());
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Use date format yyyy-MM-dd HH:mm (or use the picker).", "Invalid date", JOptionPane.WARNING_MESSAGE);
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

    private JPanel buildApptTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JLabel hint = new JLabel("Open jobs: cancel here if not paid and not completed.");
        JTable t = new JTable(apptModel);
        p.add(hint, BorderLayout.NORTH);
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton ref = new JButton("Refresh");
        ref.addActionListener(e -> refreshAppointments());
        south.add(ref);
        south.add(new JLabel("  Cancel:"));
        cancelApptCombo.setPrototypeDisplayValue(
                new IdLabelOption("id", "appt-id — Customer — SCHEDULED"));
        south.add(cancelApptCombo);
        JButton cancelAppt = new JButton("Cancel appointment");
        cancelAppt.addActionListener(e -> {
            IdLabelOption sel = (IdLabelOption) cancelApptCombo.getSelectedItem();
            if (sel == null) {
                JOptionPane.showMessageDialog(this, "No cancellable appointments (refresh if needed).",
                        "Cancel", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this,
                    "Cancel this appointment? It cannot be restored.",
                    "Confirm cancellation", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                User u = frame.getCurrentUser();
                frame.getServices().appointmentsSvc().cancelAppointment(sel.id(), u.getId(), u.getRole());
                refreshAppointments();
                JOptionPane.showMessageDialog(this, "Appointment cancelled.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        south.add(cancelAppt);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildAssignTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        assignApptCombo.setPrototypeDisplayValue(
                new IdLabelOption("id", "appt-id — Customer Name — @user — SCHEDULED"));
        assignTechCombo.setPrototypeDisplayValue(new TechnicianOption("id", "Tech Name — specialty"));
        JButton loadTech = new JButton("Refresh lists");
        loadTech.addActionListener(e -> {
            loadAssignTechnicianCombo();
            refreshAssignApptCombo();
            if (assignTechCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No technicians registered.");
            }
        });
        JButton assign = new JButton("Assign");
        assign.addActionListener(e -> {
            try {
                IdLabelOption apSel = (IdLabelOption) assignApptCombo.getSelectedItem();
                if (apSel == null) {
                    JOptionPane.showMessageDialog(this, "No appointment needs assignment (or refresh the list).",
                            "Assign", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                TechnicianOption sel = (TechnicianOption) assignTechCombo.getSelectedItem();
                if (sel == null) {
                    JOptionPane.showMessageDialog(this, "Select a technician (use Refresh lists if empty).",
                            "Assign", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                frame.getServices().appointmentsSvc().assignTechnician(apSel.id(), sel.id());
                refreshAppointments();
                JOptionPane.showMessageDialog(this, "Assigned.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("Appointment (needs technician)"), g);
        g.gridx = 1;
        p.add(assignApptCombo, g);
        g.gridx = 0;
        g.gridy = 1;
        p.add(new JLabel("Technician"), g);
        g.gridx = 1;
        p.add(assignTechCombo, g);
        g.gridx = 1;
        g.gridy = 2;
        p.add(loadTech, g);
        g.gridy = 3;
        p.add(assign, g);
        return p;
    }

    private JPanel buildPayTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JPanel north = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        payApptCombo.setPrototypeDisplayValue(
                new IdLabelOption("id", "appt-id — Customer — vehicle (completed, unpaid)"));
        JLabel quote = new JLabel("—");
        JComboBox<PaymentMethod> method = new JComboBox<>(PaymentMethod.values());
        JButton calc = new JButton("Quote");
        calc.addActionListener(e -> {
            try {
                IdLabelOption sel = (IdLabelOption) payApptCombo.getSelectedItem();
                if (sel == null) {
                    quote.setText("—");
                    JOptionPane.showMessageDialog(this,
                            "No completed unpaid appointments. Refresh or complete work first.",
                            "Quote", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                var ap = frame.getServices().appointmentsSvc().find(sel.id())
                        .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
                quote.setText(frame.getServices().paymentsSvc().quoteTotal(ap).toString());
            } catch (Exception ex) {
                quote.setText("—");
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton pay = new JButton("Collect payment & show receipt");
        pay.addActionListener(e -> {
            try {
                IdLabelOption sel = (IdLabelOption) payApptCombo.getSelectedItem();
                if (sel == null) {
                    JOptionPane.showMessageDialog(this,
                            "Select a completed appointment that has not been paid yet.",
                            "Payment", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                var r = frame.getServices().paymentsSvc().collectPayment(
                        sel.id(),
                        (PaymentMethod) method.getSelectedItem(),
                        frame.getCurrentUser().getId()
                );
                String receiptText = r.formatText();
                receiptArea.setText(receiptText);
                showReceiptDialog(receiptText);
                refreshAppointments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        g.gridx = 0;
        g.gridy = 0;
        north.add(new JLabel("Appointment (completed, not paid)"), g);
        g.gridx = 1;
        north.add(payApptCombo, g);
        g.gridx = 0;
        g.gridy = 1;
        north.add(new JLabel("Quoted total"), g);
        g.gridx = 1;
        north.add(quote, g);
        g.gridx = 0;
        g.gridy = 2;
        north.add(new JLabel("Method"), g);
        g.gridx = 1;
        north.add(method, g);
        g.gridx = 0;
        g.gridy = 3;
        north.add(calc, g);
        g.gridx = 1;
        north.add(pay, g);

        receiptArea.setEditable(false);
        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane receiptScroll = new JScrollPane(receiptArea);
        Border titled = BorderFactory.createTitledBorder("Receipt (also shown in a popup after payment)");
        receiptScroll.setBorder(BorderFactory.createCompoundBorder(titled, receiptScroll.getBorder()));

        JButton saveReceipt = new JButton("Save current receipt to file…");
        saveReceipt.addActionListener(e -> {
            String t = receiptArea.getText();
            if (t == null || t.isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "No receipt text yet. Collect a payment first, or paste content here.",
                        "Receipt", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("apu-asc-receipt.txt"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.writeString(fc.getSelectedFile().toPath(), t, StandardCharsets.UTF_8);
                    JOptionPane.showMessageDialog(this, "Receipt saved.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JPanel receiptSouth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        receiptSouth.add(saveReceipt);
        JPanel receiptWrap = new JPanel(new BorderLayout());
        receiptWrap.add(receiptScroll, BorderLayout.CENTER);
        receiptWrap.add(receiptSouth, BorderLayout.SOUTH);

        p.add(north, BorderLayout.NORTH);
        p.add(receiptWrap, BorderLayout.CENTER);
        return p;
    }

    private void showReceiptDialog(String receiptText) {
        JTextArea ta = new JTextArea(receiptText);
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(440, 300));
        JOptionPane.showMessageDialog(this, sp, "Payment recorded — receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel buildProfileTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        profileName = new JTextField(24);
        profileEmail = new JTextField(24);
        profileCur = new JPasswordField(24);
        profileNp = new JPasswordField(24);
        profileNp2 = new JPasswordField(24);
        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("Full name"), g);
        g.gridx = 1;
        p.add(profileName, g);
        g.gridx = 0;
        g.gridy = 1;
        p.add(new JLabel("Email"), g);
        g.gridx = 1;
        p.add(profileEmail, g);
        g.gridx = 0;
        g.gridy = 2;
        p.add(new JLabel("Username (sign-in & password reset)"), g);
        g.gridx = 1;
        profileIdLabel = new JLabel(" ");
        profileIdLabel.setForeground(new Color(60, 60, 60));
        p.add(profileIdLabel, g);
        g.gridx = 0;
        g.gridy = 3;
        p.add(new JLabel("Current password (if changing)"), g);
        g.gridx = 1;
        p.add(profileCur, g);
        g.gridx = 0;
        g.gridy = 4;
        p.add(new JLabel("New password (optional)"), g);
        g.gridx = 1;
        p.add(profileNp, g);
        g.gridx = 0;
        g.gridy = 5;
        p.add(new JLabel("Re-type new password"), g);
        g.gridx = 1;
        p.add(profileNp2, g);
        JButton save = new JButton("Save profile");
        save.addActionListener(e -> {
            try {
                User u = frame.getCurrentUser();
                if (!(u instanceof CounterStaff cs)) {
                    return;
                }
                String np = new String(profileNp.getPassword());
                String np2 = new String(profileNp2.getPassword());
                String pe = AccountFormValidation.validateOptionalPasswordChange(np, np2);
                if (pe != null) {
                    AccountFormValidation.warn(this, pe);
                    return;
                }
                frame.getServices().profile().updateProfile(
                        u,
                        profileName.getText().trim(),
                        profileEmail.getText().trim(),
                        null,
                        null,
                        new String(profileCur.getPassword()),
                        AccountFormValidation.isBlank(np) ? null : np
                );
                frame.getServices().users().findById(cs.getId()).ifPresent(frame::setCurrentUser);
                refreshProfileFields();
                JOptionPane.showMessageDialog(this, "Profile updated.");
                profileNp.setText("");
                profileNp2.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        g.gridx = 0;
        g.gridy = 6;
        g.gridwidth = 2;
        p.add(save, g);
        return p;
    }

    private void refreshCustomers() {
        customerAccountsPanel.refreshTable();
        refreshPickerCombos();
    }

    private void refreshAppointments() {
        apptModel.setRowCount(0);
        var users = frame.getServices().users();
        for (Appointment a : frame.getServices().appointmentsSvc().listAll()) {
            apptModel.addRow(new Object[]{
                    a.getId(),
                    UserDisplayFormat.fromId(users, a.getCustomerId()),
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
        refreshPickerCombos();
    }

    /** Keeps customer / appointment pickers in sync with tables (avoids typos). */
    private void refreshPickerCombos() {
        refreshNewApptCustomerCombo();
        refreshAssignApptCombo();
        refreshPayApptCombo();
        refreshCancelApptCombo();
        loadAssignTechnicianCombo();
    }

    private boolean canCancelAppointment(Appointment a) {
        if (a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.CANCELLED) {
            return false;
        }
        return frame.getServices().paymentsSvc().findPaymentForAppointment(a.getId()).isEmpty();
    }

    private void refreshCancelApptCombo() {
        cancelApptCombo.removeAllItems();
        var users = frame.getServices().users();
        for (Appointment a : frame.getServices().appointmentsSvc().listAll()) {
            if (!canCancelAppointment(a)) {
                continue;
            }
            String label = a.getId() + " — "
                    + UserDisplayFormat.fromId(users, a.getCustomerId())
                    + " — " + AppointmentStatusLabels.friendly(a.getStatus());
            cancelApptCombo.addItem(new IdLabelOption(a.getId(), label));
        }
    }

    private void refreshNewApptCustomerCombo() {
        newApptCustomerCombo.removeAllItems();
        for (Customer c : frame.getServices().users().customers()) {
            String label = UserDisplayFormat.friendlyLine(c) + "  [" + c.getId() + "]";
            newApptCustomerCombo.addItem(new IdLabelOption(c.getId(), label));
        }
    }

    private void refreshAssignApptCombo() {
        assignApptCombo.removeAllItems();
        var users = frame.getServices().users();
        for (Appointment a : frame.getServices().appointmentsSvc().listAll()) {
            if (a.getStatus() == AppointmentStatus.CANCELLED || a.getScheduledAt() == null) {
                continue;
            }
            if (a.getTechnicianId() != null) {
                continue;
            }
            String label = a.getId() + " — "
                    + UserDisplayFormat.fromId(users, a.getCustomerId())
                    + " — " + a.getStatus();
            assignApptCombo.addItem(new IdLabelOption(a.getId(), label));
        }
    }

    private void refreshPayApptCombo() {
        payApptCombo.removeAllItems();
        var paySvc = frame.getServices().paymentsSvc();
        var users = frame.getServices().users();
        for (Appointment a : frame.getServices().appointmentsSvc().listAll()) {
            if (a.getStatus() != AppointmentStatus.COMPLETED) {
                continue;
            }
            if (paySvc.findPaymentForAppointment(a.getId()).isPresent()) {
                continue;
            }
            String veh = a.getVehicleDescription() != null && !a.getVehicleDescription().isBlank()
                    ? a.getVehicleDescription()
                    : "(no vehicle note)";
            String label = a.getId() + " — "
                    + UserDisplayFormat.fromId(users, a.getCustomerId())
                    + " — " + veh;
            payApptCombo.addItem(new IdLabelOption(a.getId(), label));
        }
    }

    private void loadAssignTechnicianCombo() {
        assignTechCombo.removeAllItems();
        frame.getServices().users().technicians().forEach(t ->
                assignTechCombo.addItem(new TechnicianOption(t.getId(), t.getFullName() + " — " + t.getSpecialty())));
    }

    private record TechnicianOption(String id, String label) {
        @Override
        public String toString() {
            return label + " [" + id + "]";
        }
    }
}
