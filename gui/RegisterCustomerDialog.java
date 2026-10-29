package com.apu.asc.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

final class RegisterCustomerDialog {
    private RegisterCustomerDialog() {}

    static void show(MainFrame frame) {
        String draftU = "";
        String draftName = "";
        String draftEmail = "";
        String draftPhone = "";
        String draftReg = "";

        while (true) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(new EmptyBorder(8, 8, 8, 8));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 6, 4, 6);
            g.fill = GridBagConstraints.HORIZONTAL;
            g.gridx = 0;
            g.gridy = 0;

            JTextField u = new JTextField(18);
            u.setText(draftU);
            JPasswordField pw = new JPasswordField(18);
            JPasswordField pw2 = new JPasswordField(18);
            JTextField name = new JTextField(18);
            name.setText(draftName);
            JTextField email = new JTextField(18);
            email.setText(draftEmail);
            JTextField phone = new JTextField(18);
            phone.setText(draftPhone);
            JTextField reg = new JTextField(18);
            reg.setText(draftReg);

            addRow(p, g, "Username *", u);
            addRow(p, g, "Password *", pw);
            addRow(p, g, "Re-type password *", pw2);
            addRow(p, g, "Full name *", name);
            addRow(p, g, "Email *", email);
            addRow(p, g, "Phone *", phone);
            addRow(p, g, "Vehicle registration *", reg);

            int opt = JOptionPane.showConfirmDialog(frame, p, "Customer registration",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) {
                return;
            }

            draftU = u.getText().trim();
            draftName = name.getText().trim();
            draftEmail = email.getText().trim();
            draftPhone = phone.getText().trim();
            draftReg = reg.getText().trim();
            String pass = new String(pw.getPassword());
            String pass2 = new String(pw2.getPassword());

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
            if (AccountFormValidation.isBlank(draftPhone)) {
                AccountFormValidation.warn(frame, "Phone is required.");
                continue;
            }
            if (AccountFormValidation.isBlank(draftReg)) {
                AccountFormValidation.warn(frame, "Vehicle registration is required.");
                continue;
            }
            String pe = AccountFormValidation.validateNewPasswords(pass, pass2);
            if (pe != null) {
                AccountFormValidation.warn(frame, pe);
                continue;
            }

            try {
                frame.getServices().registration().registerCustomer(
                        draftU, pass, draftName, draftEmail, draftPhone, draftReg);
                JOptionPane.showMessageDialog(frame, "Registration successful. You can sign in now.", "Done",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private static void addRow(JPanel p, GridBagConstraints g, String label, JComponent field) {
        g.gridwidth = 1;
        g.gridx = 0;
        g.weightx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(field, g);
        g.gridy++;
    }
}
