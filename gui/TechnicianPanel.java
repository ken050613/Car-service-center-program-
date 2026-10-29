package com.apu.asc.gui;

import com.apu.asc.model.Appointment;
import com.apu.asc.model.Technician;
import com.apu.asc.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TechnicianPanel extends JPanel {
    private final MainFrame frame;
    private JTextField profileName;
    private JTextField profileEmail;
    private JTextField profileSpec;
    private JPasswordField profileNp;
    private JPasswordField profileNp2;
    private JLabel profileIdLabel;
    private final JComboBox<IdLabelOption> workApptCombo = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Customer", "Status", "Work finished?", "Tier", "Slot", "Start", "End", "Add-ons", "Notes"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public TechnicianPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout());
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> frame.showLogin());
        top.add(new JLabel("Technician"), BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTextArea detail = new JTextArea(4, 50);
        detail.setEditable(false);
        detail.setLineWrap(true);
        workApptCombo.setPrototypeDisplayValue(
                new IdLabelOption("id", "appt-id — Customer — ASSIGNED"));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                String id = (String) model.getValueAt(table.getSelectedRow(), 0);
                frame.getServices().appointmentsSvc().find(id).ifPresent(a -> detail.setText(
                        "Status: " + AppointmentStatusLabels.friendly(a.getStatus())
                                + " — Work finished? " + AppointmentStatusLabels.workFinishedYesNo(a.getStatus()) + "\n"
                                + "Vehicle: " + a.getVehicleDescription() + "\n"
                                + "Tier: " + a.getPrimaryKind() + " (" + a.getDurationMinutes() + " min)\n"
                                + "Notes: " + a.getNotes()));
                selectWorkApptComboById(id);
            }
        });

        JTextArea techFb = new JTextArea(3, 40);
        JButton inProgress = new JButton("Start work (In progress)");
        inProgress.addActionListener(e -> {
            try {
                IdLabelOption sel = selectedWorkAppointment();
                if (sel == null) {
                    return;
                }
                frame.getServices().appointmentsSvc().markInProgressByTechnician(
                        sel.id(),
                        frame.getCurrentUser().getId()
                );
                refresh();
                JOptionPane.showMessageDialog(this, "Marked in progress.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton complete = new JButton("Mark COMPLETED");
        complete.addActionListener(e -> {
            try {
                IdLabelOption sel = selectedWorkAppointment();
                if (sel == null) {
                    return;
                }
                frame.getServices().appointmentsSvc().markCompletedByTechnician(
                        sel.id(),
                        frame.getCurrentUser().getId()
                );
                refresh();
                JOptionPane.showMessageDialog(this, "Marked completed.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton sendFb = new JButton("Submit feedback on appointment");
        sendFb.addActionListener(e -> {
            try {
                IdLabelOption sel = selectedWorkAppointment();
                if (sel == null) {
                    return;
                }
                frame.getServices().feedbackSvc().submitTechnicianJobFeedback(
                        frame.getCurrentUser().getId(),
                        sel.id(),
                        techFb.getText().trim()
                );
                JOptionPane.showMessageDialog(this, "Feedback saved.");
                techFb.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel actions = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridwidth = 2;
        g.gridx = 0;
        g.gridy = 0;
        actions.add(new JLabel("Appointment (select from list or table)"), g);
        g.gridy = 1;
        actions.add(workApptCombo, g);
        g.gridy = 2;
        actions.add(inProgress, g);
        g.gridy = 3;
        actions.add(complete, g);
        g.gridy = 4;
        actions.add(new JLabel("Your job feedback / notes"), g);
        g.gridy = 5;
        actions.add(new JScrollPane(techFb), g);
        g.gridy = 6;
        actions.add(sendFb, g);

        JPanel lower = new JPanel(new BorderLayout(8, 8));
        lower.add(new JScrollPane(detail), BorderLayout.NORTH);
        lower.add(actions, BorderLayout.CENTER);

        JPanel work = new JPanel(new BorderLayout());
        work.add(new JScrollPane(table), BorderLayout.CENTER);
        work.add(lower, BorderLayout.SOUTH);

        JButton ref = new JButton("Refresh");
        ref.addActionListener(e -> refresh());
        JPanel refRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refRow.add(ref);

        JLabel workHint = new JLabel(
                "<html><body style='width:520px'><b>Status</b> and <b>Work finished?</b> show job progress. "
                        + "Use <b>Start work</b> when you begin, then <b>Mark COMPLETED</b> when done.</body></html>");
        JPanel workNorth = new JPanel(new BorderLayout(0, 6));
        workNorth.add(refRow, BorderLayout.NORTH);
        workNorth.add(workHint, BorderLayout.SOUTH);

        JPanel workTab = new JPanel(new BorderLayout());
        workTab.add(workNorth, BorderLayout.NORTH);
        workTab.add(work, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Assigned work", workTab);
        tabs.addTab("My profile", buildProfileTab());

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildProfileTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        profileName = new JTextField(24);
        profileEmail = new JTextField(24);
        profileSpec = new JTextField(24);
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
        p.add(new JLabel("Specialty"), g);
        g.gridx = 1;
        p.add(profileSpec, g);
        g.gridx = 0;
        g.gridy = 3;
        p.add(new JLabel("Username (sign-in & password reset)"), g);
        g.gridx = 1;
        profileIdLabel = new JLabel(" ");
        profileIdLabel.setForeground(new Color(60, 60, 60));
        p.add(profileIdLabel, g);
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
                if (!(u instanceof Technician t)) {
                    return;
                }
                String np = new String(profileNp.getPassword());
                String np2 = new String(profileNp2.getPassword());
                String pe = AccountFormValidation.validateOptionalPasswordChange(np, np2);
                if (pe != null) {
                    AccountFormValidation.warn(this, pe);
                    return;
                }
                frame.getServices().staffAdmin().updateTechnician(
                        t.getId(), t.getUsername(), profileName.getText().trim(), profileEmail.getText().trim(),
                        profileSpec.getText().trim(),
                        AccountFormValidation.isBlank(np) ? null : np);
                frame.getServices().users().findById(t.getId()).ifPresent(frame::setCurrentUser);
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

    private void refreshProfileFields() {
        User u = frame.getCurrentUser();
        if (profileName == null || !(u instanceof Technician t)) {
            return;
        }
        profileName.setText(t.getFullName());
        profileEmail.setText(t.getEmail());
        profileSpec.setText(t.getSpecialty());
        profileNp.setText("");
        profileNp2.setText("");
        if (profileIdLabel != null) {
            profileIdLabel.setText(t.getUsername());
        }
    }

    public void onShow() {
        refresh();
        refreshProfileFields();
    }

    private void refresh() {
        model.setRowCount(0);
        workApptCombo.removeAllItems();
        String tid = frame.getCurrentUser().getId();
        var users = frame.getServices().users();
        for (Appointment a : frame.getServices().appointmentsSvc().listForTechnician(tid)) {
            model.addRow(new Object[]{
                    a.getId(),
                    UserDisplayFormat.fromId(users, a.getCustomerId()),
                    AppointmentStatusLabels.friendly(a.getStatus()),
                    AppointmentStatusLabels.workFinishedYesNo(a.getStatus()),
                    a.getPrimaryKind(),
                    a.getDurationMinutes() + " min",
                    a.getScheduledAt() != null ? a.getScheduledAt().toString() : "—",
                    a.getScheduledEnd() != null ? a.getScheduledEnd().toString() : "—",
                    String.join(", ", a.getServiceCodes()),
                    a.getNotes()
            });
            String label = a.getId() + " — "
                    + UserDisplayFormat.fromId(users, a.getCustomerId())
                    + " — " + AppointmentStatusLabels.friendly(a.getStatus());
            workApptCombo.addItem(new IdLabelOption(a.getId(), label));
        }
    }

    private void selectWorkApptComboById(String id) {
        for (int i = 0; i < workApptCombo.getItemCount(); i++) {
            if (workApptCombo.getItemAt(i).id().equals(id)) {
                workApptCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    /** @return null if nothing to select (after warning). */
    private IdLabelOption selectedWorkAppointment() {
        if (workApptCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No assigned appointments. Ask counter staff to assign you, then click Refresh.",
                    "Appointments", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        IdLabelOption sel = (IdLabelOption) workApptCombo.getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select an appointment from the list.",
                    "Appointments", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return sel;
    }
}
