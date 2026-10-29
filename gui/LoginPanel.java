package com.apu.asc.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final MainFrame frame;
    private final JTextField username = new JTextField(24);
    private final JPasswordField password = new JPasswordField(24);

    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 48, 32, 48));

        JLabel title = new JLabel("APU Automotive Service Centre", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel sub = new JLabel("Sign in to continue", SwingConstants.CENTER);
        sub.setForeground(new Color(80, 80, 80));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("Username"), g);
        g.gridy = 1;
        form.add(username, g);
        g.gridy = 2;
        form.add(new JLabel("Password"), g);
        g.gridy = 3;
        form.add(password, g);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register as customer");
        JButton forgotBtn = new JButton("Forgot password");
        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> RegisterCustomerDialog.show(frame));
        forgotBtn.addActionListener(e -> ForgotPasswordDialog.show(this, frame));
        buttons.add(loginBtn);
        buttons.add(registerBtn);
        buttons.add(forgotBtn);

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.add(title, BorderLayout.NORTH);
        north.add(sub, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void doLogin() {
        String u = username.getText().trim();
        String p = new String(password.getPassword());
        if (u.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }
        frame.getServices().auth().login(u, p).ifPresentOrElse(
                user -> {
                    frame.showDashboardFor(user);
                    password.setText("");
                },
                () -> JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login", JOptionPane.WARNING_MESSAGE)
        );
    }

    public void clearFields() {
        username.setText("");
        password.setText("");
    }
}
