package com.apu.asc.gui;

import com.apu.asc.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Manager CRUD for managers, counter staff, and technicians (Table 1.0).
 */
final class ManagerStaffCrudPanel {
    private ManagerStaffCrudPanel() {}

    static JPanel create(MainFrame frame) {
        JPanel wrap = new JPanel(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Managers", buildTable(frame, Role.MANAGER));
        tabs.addTab("Counter staff", buildTable(frame, Role.COUNTER_STAFF));
        tabs.addTab("Technicians", buildTable(frame, Role.TECHNICIAN));
        wrap.add(tabs, BorderLayout.CENTER);
        return wrap;
    }

    private static JPanel buildTable(MainFrame frame, Role role) {
        DefaultTableModel model = switch (role) {
            case MANAGER -> new DefaultTableModel(new Object[]{"ID", "Username", "Name", "Email"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            case COUNTER_STAFF -> new DefaultTableModel(new Object[]{"ID", "Username", "Name", "Email"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            case TECHNICIAN -> new DefaultTableModel(new Object[]{"ID", "Username", "Name", "Email", "Specialty"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            default -> throw new IllegalStateException();
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            switch (role) {
                case MANAGER -> {
                    for (Manager m : frame.getServices().users().managers()) {
                        model.addRow(new Object[]{m.getId(), m.getUsername(), m.getFullName(), m.getEmail()});
                    }
                }
                case COUNTER_STAFF -> {
                    for (User u : frame.getServices().users().allUsers()) {
                        if (u instanceof CounterStaff c) {
                            model.addRow(new Object[]{c.getId(), c.getUsername(), c.getFullName(), c.getEmail()});
                        }
                    }
                }
                case TECHNICIAN -> {
                    frame.getServices().users().technicians().forEach(t ->
                            model.addRow(new Object[]{t.getId(), t.getUsername(), t.getFullName(), t.getEmail(), t.getSpecialty()}));
                }
                default -> {
                }
            }
        };
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        JButton ref = new JButton("Refresh");
        add.addActionListener(e -> {
            try {
                switch (role) {
                    case MANAGER -> showManagerDialog(frame, null, refresh);
                    case COUNTER_STAFF -> showCounterDialog(frame, null, refresh);
                    case TECHNICIAN -> showTechDialog(frame, null, refresh);
                    default -> {
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        edit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                return;
            }
            String id = (String) model.getValueAt(r, 0);
            frame.getServices().users().findById(id).ifPresent(u -> {
                try {
                    switch (role) {
                        case MANAGER -> showManagerDialog(frame, (Manager) u, refresh);
                        case COUNTER_STAFF -> showCounterDialog(frame, (CounterStaff) u, refresh);
                        case TECHNICIAN -> showTechDialog(frame, (Technician) u, refresh);
                        default -> {
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
        del.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                return;
            }
            String id = (String) model.getValueAt(r, 0);
            if (JOptionPane.showConfirmDialog(frame, "Delete this user?", "Confirm", JOptionPane.OK_CANCEL_OPTION)
                    != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                switch (role) {
                    case MANAGER -> frame.getServices().staffAdmin().deleteManager(id, frame.getCurrentUser().getId());
                    case COUNTER_STAFF -> frame.getServices().staffAdmin().deleteCounterStaff(id);
                    case TECHNICIAN -> frame.getServices().staffAdmin().deleteTechnician(id);
                    default -> {
                    }
                }
                refresh.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        ref.addActionListener(e -> refresh.run());
        btns.add(add);
        btns.add(edit);
        btns.add(del);
        btns.add(ref);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        refresh.run();
        return p;
    }

    private static void showManagerDialog(MainFrame frame, Manager existing, Runnable refresh) throws Exception {
        String draftU = existing != null ? existing.getUsername() : "";
        String draftName = existing != null ? existing.getFullName() : "";
        String draftEmail = existing != null ? existing.getEmail() : "";

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

            int opt = JOptionPane.showConfirmDialog(frame, form, existing == null ? "New manager" : "Edit manager",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) {
                return;
            }

            draftU = u.getText().trim();
            draftName = name.getText().trim();
            draftEmail = email.getText().trim();
            String p1 = new String(pw.getPassword());
            String p2 = new String(pw2.getPassword());

            if (AccountFormValidation.isBlank(draftU)) {
                AccountFormValidation.warn(frame, "Username is required.");
                continue;
            }
            if (AccountFormValidation.isBlank(draftName)) {
                AccountFormValidation.warn(frame, "Full name is required.");
                continue;
            }
            String emailErr = AccountFormValidation.validateEmailField(draftEmail);
            if (emailErr != null) {
                AccountFormValidation.warn(frame, emailErr);
                continue;
            }
            if (existing == null) {
                String pe = AccountFormValidation.validateNewPasswords(p1, p2);
                if (pe != null) {
                    AccountFormValidation.warn(frame, pe);
                    continue;
                }
                try {
                    frame.getServices().staffAdmin().createManager(draftU, p1, draftName, draftEmail);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                String pe = AccountFormValidation.validateOptionalPasswordChange(p1, p2);
                if (pe != null) {
                    AccountFormValidation.warn(frame, pe);
                    continue;
                }
                try {
                    frame.getServices().staffAdmin().updateManager(existing.getId(), draftU, draftName, draftEmail,
                            AccountFormValidation.isBlank(p1) ? null : p1);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            refresh.run();
            JOptionPane.showMessageDialog(frame, "Saved.");
            return;
        }
    }

    private static void showCounterDialog(MainFrame frame, CounterStaff existing, Runnable refresh) throws Exception {
        String draftU = existing != null ? existing.getUsername() : "";
        String draftName = existing != null ? existing.getFullName() : "";
        String draftEmail = existing != null ? existing.getEmail() : "";

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

            int opt = JOptionPane.showConfirmDialog(frame, form, existing == null ? "New counter" : "Edit counter",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) {
                return;
            }

            draftU = u.getText().trim();
            draftName = name.getText().trim();
            draftEmail = email.getText().trim();
            String p1 = new String(pw.getPassword());
            String p2 = new String(pw2.getPassword());

            if (AccountFormValidation.isBlank(draftU)) {
                AccountFormValidation.warn(frame, "Username is required.");
                continue;
            }
            if (AccountFormValidation.isBlank(draftName)) {
                AccountFormValidation.warn(frame, "Full name is required.");
                continue;
            }
            String emailErr = AccountFormValidation.validateEmailField(draftEmail);
            if (emailErr != null) {
                AccountFormValidation.warn(frame, emailErr);
                continue;
            }
            if (existing == null) {
                String pe = AccountFormValidation.validateNewPasswords(p1, p2);
                if (pe != null) {
                    AccountFormValidation.warn(frame, pe);
                    continue;
                }
                try {
                    frame.getServices().staffAdmin().createCounterStaff(draftU, p1, draftName, draftEmail);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                String pe = AccountFormValidation.validateOptionalPasswordChange(p1, p2);
                if (pe != null) {
                    AccountFormValidation.warn(frame, pe);
                    continue;
                }
                try {
                    frame.getServices().staffAdmin().updateCounterStaff(existing.getId(), draftU, draftName, draftEmail,
                            AccountFormValidation.isBlank(p1) ? null : p1);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            refresh.run();
            JOptionPane.showMessageDialog(frame, "Saved.");
            return;
        }
    }

    private static void showTechDialog(MainFrame frame, Technician existing, Runnable refresh) throws Exception {
        String draftU = existing != null ? existing.getUsername() : "";
        String draftName = existing != null ? existing.getFullName() : "";
        String draftEmail = existing != null ? existing.getEmail() : "";
        String draftSpec = existing != null ? existing.getSpecialty() : "";

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
            JTextField spec = new JTextField(16);
            spec.setText(draftSpec);

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
            form.add(new JLabel("Specialty *"));
            form.add(spec);

            int opt = JOptionPane.showConfirmDialog(frame, form, existing == null ? "New technician" : "Edit technician",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) {
                return;
            }

            draftU = u.getText().trim();
            draftName = name.getText().trim();
            draftEmail = email.getText().trim();
            draftSpec = spec.getText().trim();
            String p1 = new String(pw.getPassword());
            String p2 = new String(pw2.getPassword());

            if (AccountFormValidation.isBlank(draftU)) {
                AccountFormValidation.warn(frame, "Username is required.");
                continue;
            }
            if (AccountFormValidation.isBlank(draftName)) {
                AccountFormValidation.warn(frame, "Full name is required.");
                continue;
            }
            String emailErr = AccountFormValidation.validateEmailField(draftEmail);
            if (emailErr != null) {
                AccountFormValidation.warn(frame, emailErr);
                continue;
            }
            if (AccountFormValidation.isBlank(draftSpec)) {
                AccountFormValidation.warn(frame, "Specialty is required.");
                continue;
            }
            if (existing == null) {
                String pe = AccountFormValidation.validateNewPasswords(p1, p2);
                if (pe != null) {
                    AccountFormValidation.warn(frame, pe);
                    continue;
                }
                try {
                    frame.getServices().staffAdmin().createTechnician(draftU, p1, draftName, draftEmail, draftSpec);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                String pe = AccountFormValidation.validateOptionalPasswordChange(p1, p2);
                if (pe != null) {
                    AccountFormValidation.warn(frame, pe);
                    continue;
                }
                try {
                    frame.getServices().staffAdmin().updateTechnician(existing.getId(), draftU, draftName, draftEmail, draftSpec,
                            AccountFormValidation.isBlank(p1) ? null : p1);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            refresh.run();
            JOptionPane.showMessageDialog(frame, "Saved.");
            return;
        }
    }
}
