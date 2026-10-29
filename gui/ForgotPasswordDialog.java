package apu.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Reset password using username + registered email (no login required).
 */
public final class ForgotPasswordDialog {
    private ForgotPasswordDialog() {}

    public static void show(Component parent, MainFrame frame) {
        String draftUser = "";
        String draftEmail = "";

        while (true) {
            JTextField usernameField = new JTextField(24);
            usernameField.setText(draftUser);
            JTextField email = new JTextField(24);
            email.setText(draftEmail);
            JPasswordField pw = new JPasswordField(24);
            JPasswordField pw2 = new JPasswordField(24);

            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(4, 6, 4, 6);
            g.anchor = GridBagConstraints.WEST;
            g.gridx = 0;
            g.gridy = 0;
            p.add(new JLabel("Username *"), g);
            g.gridx = 1;
            p.add(usernameField, g);
            g.gridx = 0;
            g.gridy = 1;
            p.add(new JLabel("Registered email *"), g);
            g.gridx = 1;
            p.add(email, g);
            g.gridx = 0;
            g.gridy = 2;
            p.add(new JLabel("New password *"), g);
            g.gridx = 1;
            p.add(pw, g);
            g.gridx = 0;
            g.gridy = 3;
            p.add(new JLabel("Re-type new password *"), g);
            g.gridx = 1;
            p.add(pw2, g);

            JLabel hint = new JLabel("<html><body style='width:340px'>"
                    + "Enter the <b>username</b> you use to sign in and the <b>email</b> registered on your account "
                    + "(must match exactly, apart from upper/lower case on the email)."
                    + "</body></html>");
            g.gridx = 0;
            g.gridy = 4;
            g.gridwidth = 2;
            p.add(hint, g);

            int opt = JOptionPane.showConfirmDialog(parent, p, "Forgot password — reset",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opt != JOptionPane.OK_OPTION) {
                return;
            }

            draftUser = usernameField.getText().trim();
            draftEmail = email.getText().trim();
            String p1 = new String(pw.getPassword());
            String p2 = new String(pw2.getPassword());

            if (AccountFormValidation.isBlank(draftUser)) {
                AccountFormValidation.warn(parent, "Username is required.");
                continue;
            }
            String emailErr = AccountFormValidation.validateEmailField(draftEmail);
            if (emailErr != null) {
                AccountFormValidation.warn(parent, emailErr);
                continue;
            }
            String pwErr = AccountFormValidation.validateNewPasswords(p1, p2);
            if (pwErr != null) {
                AccountFormValidation.warn(parent, pwErr);
                continue;
            }

            try {
                frame.getServices().auth().resetPasswordByUsernameAndEmail(draftUser, draftEmail, p1);
                JOptionPane.showMessageDialog(parent,
                        "Password updated. You can sign in with your username and the new password.",
                        "Forgot password", JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Reset failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }
}
