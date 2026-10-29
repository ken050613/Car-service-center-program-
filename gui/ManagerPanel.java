package com.apu.asc.gui;

import com.apu.asc.model.Appointment;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.ServiceItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class ManagerPanel extends JPanel {
    private final MainFrame frame;
    private final CustomerAccountsPanel customerAccountsPanel;
    private final DefaultTableModel catalogModel = new DefaultTableModel(new Object[]{"Code", "Description", "Price"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel apptModel = new DefaultTableModel(
            new Object[]{"ID", "Customer", "Status", "Work finished?", "Technician", "Tier", "Slot", "Start", "End", "Add-ons"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel feedbackModel = new DefaultTableModel(
            new Object[]{"ID", "Appointment", "Rating", "Comment"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTextArea reportArea = new JTextArea(18, 40);

    public ManagerPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout());
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> frame.showLogin());
        top.add(new JLabel("Manager dashboard"), BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        customerAccountsPanel = new CustomerAccountsPanel(frame, null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Prices (NORMAL / MAJOR + add-ons)", buildPricesTab());
        tabs.addTab("Staff (CRUD)", ManagerStaffCrudPanel.create(frame));
        tabs.addTab("Customer accounts", customerAccountsPanel);
        tabs.addTab("Appointments", buildApptTab());
        tabs.addTab("Reports", buildReportTab());
        tabs.addTab("Feedback", buildFeedbackTab());

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    public void onShow() {
        refreshCatalog();
        customerAccountsPanel.refreshTable();
        refreshAppointments();
        refreshFeedback();
        reportArea.setText(frame.getServices().reports().buildSummaryReport());
    }

    private JPanel buildPricesTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JTable table = new JTable(catalogModel);
        table.setFillsViewportHeight(true);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.gridx = 0;
        g.gridy = 0;
        JTextField code = new JTextField(8);
        JTextField desc = new JTextField(20);
        JTextField price = new JTextField(8);
        form.add(new JLabel("Code"), g);
        g.gridx = 1;
        form.add(code, g);
        g.gridx = 0;
        g.gridy = 1;
        form.add(new JLabel("Description"), g);
        g.gridx = 1;
        form.add(desc, g);
        g.gridx = 0;
        g.gridy = 2;
        form.add(new JLabel("Unit price"), g);
        g.gridx = 1;
        form.add(price, g);

        JButton save = new JButton("Save / update price");
        save.addActionListener(e -> {
            try {
                frame.getServices().pricing().setOrUpdatePrice(
                        code.getText().trim().toUpperCase(),
                        desc.getText().trim(),
                        new BigDecimal(price.getText().trim())
                );
                refreshCatalog();
                JOptionPane.showMessageDialog(this, "Saved.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        form.add(save, g);

        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildApptTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JLabel hint = new JLabel(
                "<html><body style='width:560px'><b>Appointment status</b> — <i>Scheduled / Assigned / In progress / Completed / Cancelled</i>. "
                        + "<b>Work finished?</b> is Yes only when the technician has marked the job completed.</body></html>");
        JTable table = new JTable(apptModel);
        p.add(hint, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton ref = new JButton("Refresh");
        ref.addActionListener(e -> refreshAppointments());
        p.add(ref, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildReportTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JLabel hint = new JLabel(
                "<html><body style='width:520px'>"
                        + "<b>Management analytics</b> — revenue, appointment volumes, status breakdown, "
                        + "service tier mix (1h vs 3h slots), most-used add-on code, and average feedback. "
                        + "Refreshes when you open the dashboard; use the button below after new activity."
                        + "</body></html>");
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        p.add(hint, BorderLayout.NORTH);
        p.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        JButton ref = new JButton("Refresh report");
        ref.addActionListener(e -> reportArea.setText(frame.getServices().reports().buildSummaryReport()));
        p.add(ref, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFeedbackTab() {
        JPanel p = new JPanel(new BorderLayout());
        JTable table = new JTable(feedbackModel);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton ref = new JButton("Refresh");
        ref.addActionListener(e -> refreshFeedback());
        p.add(ref, BorderLayout.SOUTH);
        return p;
    }

    private void refreshCatalog() {
        catalogModel.setRowCount(0);
        for (ServiceItem item : frame.getServices().pricing().listPrices()) {
            catalogModel.addRow(new Object[]{item.getCode(), item.getDescription(), item.getUnitPrice()});
        }
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
    }

    private void refreshFeedback() {
        feedbackModel.setRowCount(0);
        for (Feedback f : frame.getServices().feedbackSvc().allFeedback()) {
            feedbackModel.addRow(new Object[]{f.getId(), f.getAppointmentId(), f.getRating(), f.getComment()});
        }
    }
}
