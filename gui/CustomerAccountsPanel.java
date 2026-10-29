package apu.gui;

import apu.model.Customer;
import apu.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

/**
 * Customer list with add / edit / delete — shared by counter staff and manager.
 */
public class CustomerAccountsPanel extends JPanel {
    private final MainFrame frame;
    private final Runnable onCustomerListChanged;
    private final DefaultTableModel custModel = new DefaultTableModel(
            new Object[]{"Customer ID", "Username", "Name", "Phone", "Vehicle"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public CustomerAccountsPanel(MainFrame frame, Runnable onCustomerListChanged) {
        this.frame = frame;
        this.onCustomerListChanged = onCustomerListChanged;
        setLayout(new BorderLayout(6, 6));
        JTable t = new JTable(custModel);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add customer");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        JButton ref = new JButton("Refresh");
        add.addActionListener(e -> showCustomerDialog(null));
        edit.addActionListener(e -> {
            int r = t.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Select a row.");
                return;
            }
            String id = (String) custModel.getValueAt(r, 0);
            frame.getServices().users().findById(id).ifPresent(u -> {
                if (u instanceof Customer c) {
                    showCustomerDialog(c);
                }
            });
        });
        del.addActionListener(e -> {
            int r = t.getSelectedRow();
            if (r < 0) {
                return;
            }
            String id = (String) custModel.getValueAt(r, 0);
            String username = (String) custModel.getValueAt(r, 1);
            JTextField reason = new JTextField(36);
            JPanel ask = new JPanel(new BorderLayout(0, 8));
            ask.add(new JLabel("Reason (required, saved for manager):"), BorderLayout.NORTH);
            ask.add(reason, BorderLayout.CENTER);
            if (JOptionPane.showConfirmDialog(this, ask, "Delete customer", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                return;
            }
            String rs = reason.getText().trim();
            if (rs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a reason for the audit log.", "Delete",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                User actor = frame.getCurrentUser();
                frame.getServices().customerMgmt().deleteCustomer(id);
                try {
                    frame.getServices().accountAudit().log(actor, "DELETE", id, username, "account removed", rs);
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(this,
                            "Customer removed, but audit log failed: " + ioe.getMessage(),
                            "Warning", JOptionPane.WARNING_MESSAGE);
                }
                refreshTable();
                notifyChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        ref.addActionListener(e -> refreshTable());
        btns.add(add);
        btns.add(edit);
        btns.add(del);
        btns.add(ref);
        add(new JScrollPane(t), BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
    }

    private void notifyChanged() {
        if (onCustomerListChanged != null) {
            onCustomerListChanged.run();
        }
    }

    public void refreshTable() {
        custModel.setRowCount(0);
        for (Customer c : frame.getServices().users().customers()) {
            custModel.addRow(new Object[]{c.getId(), c.getUsername(), c.getFullName(), c.getPhone(), c.getVehicleRegistration()});
        }
    }

    private void showCustomerDialog(Customer existing) {
        String draftU = existing != null ? existing.getUsername() : "";
        String draftName = existing != null ? existing.getFullName() : "";
        String draftEmail = existing != null ? existing.getEmail() : "";
        String draftPhone = existing != null ? existing.getPhone() : "";
        String draftReg = existing != null ? existing.getVehicleRegistration() : "";

        while (true) {
            JTextField u = new JTextField(16);
            u.setText(draftU);
            u.setEditable(existing == null);
            JPasswordField pw = new JPasswordField(16);
            JPasswordField pw2 = new JPasswordField(16);
            JTextField name = new JTextField(16);
            name.setText(draftName);
            JTextField email = new JTextField(16);
            email.setText(draftEmail);
            JTextField phone = new JTextField(16);
            phone.setText(draftPhone);
            JTextField reg = new JTextField(16);
            reg.setText(draftReg);

            JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
            form.add(new JLabel(existing == null ? "Username *" : "Username"));
            form.add(u);
            form.add(new JLabel(existing == null ? "Password *" : "New password (optional)"));
            form.add(pw);
            form.add(new JLabel(existing == null ? "Re-type password *" : "Re-type new password"));
            form.add(pw2);
            form.add(new JLabel("Full name *"));
            form.add(name);
            form.add(new JLabel("Email *"));
            form.add(email);
            form.add(new JLabel("Phone *"));
            form.add(phone);
            form.add(new JLabel("Vehicle registration *"));
            form.add(reg);

            int opt = JOptionPane.showConfirmDialog(this, form, existing == null ? "New customer" : "Edit customer",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) {
                return;
            }

            draftU = u.getText().trim();
            draftName = name.getText().trim();
            draftEmail = email.getText().trim();
            draftPhone = phone.getText().trim();
            draftReg = reg.getText().trim();
            String p1 = new String(pw.getPassword());
            String p2 = new String(pw2.getPassword());

            if (AccountFormValidation.isBlank(draftU)) {
                AccountFormValidation.warn(this, "Username is required.");
                continue;
            }
            if (AccountFormValidation.isBlank(draftName)) {
                AccountFormValidation.warn(this, "Full name is required.");
                continue;
            }
            String emailErr = AccountFormValidation.validateEmailField(draftEmail);
            if (emailErr != null) {
                AccountFormValidation.warn(this, emailErr);
                continue;
            }
            if (AccountFormValidation.isBlank(draftPhone)) {
                AccountFormValidation.warn(this, "Phone is required.");
                continue;
            }
            if (AccountFormValidation.isBlank(draftReg)) {
                AccountFormValidation.warn(this, "Vehicle registration is required.");
                continue;
            }

            try {
                User actor = frame.getCurrentUser();
                if (existing == null) {
                    String pe = AccountFormValidation.validateNewPasswords(p1, p2);
                    if (pe != null) {
                        AccountFormValidation.warn(this, pe);
                        continue;
                    }
                    Customer created = frame.getServices().customerMgmt().createCustomer(
                            draftU, p1, draftName, draftEmail, draftPhone, draftReg);
                    try {
                        frame.getServices().accountAudit().log(actor, "CREATE", created.getId(), created.getUsername(),
                                "new customer account", "");
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(this,
                                "Saved, but audit log failed: " + ioe.getMessage(),
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        refreshTable();
                        notifyChanged();
                        return;
                    }
                } else {
                    String pe = AccountFormValidation.validateOptionalPasswordChange(p1, p2);
                    if (pe != null) {
                        AccountFormValidation.warn(this, pe);
                        continue;
                    }
                    frame.getServices().customerMgmt().updateCustomer(
                            existing.getId(), draftU, draftName, draftEmail, draftPhone, draftReg,
                            AccountFormValidation.isBlank(p1) ? null : p1);
                    try {
                        frame.getServices().accountAudit().log(actor, "UPDATE", existing.getId(), draftU,
                                "profile or credentials updated", "");
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(this,
                                "Saved, but audit log failed: " + ioe.getMessage(),
                                "Warning", JOptionPane.WARNING_MESSAGE);
                        refreshTable();
                        notifyChanged();
                        return;
                    }
                }
                refreshTable();
                notifyChanged();
                JOptionPane.showMessageDialog(this, "Saved.");
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
