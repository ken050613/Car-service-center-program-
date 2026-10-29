package com.apu.asc.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Immediate validation for registration / account forms (Swing dialogs).
 */
public final class AccountFormValidation {
    private AccountFormValidation() {}

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Check your input", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Non-blank email must contain {@code @} (basic format check for Swing forms). */
    public static String validateEmailField(String email) {
        if (isBlank(email)) {
            return "Email is required.";
        }
        if (!email.trim().contains("@")) {
            return "Email must include @ (for example name@example.com).";
        }
        return null;
    }

    /** New account: password required; must match confirmation. */
    public static String validateNewPasswords(String password, String confirm) {
        if (isBlank(password)) {
            return "Password is required.";
        }
        if (isBlank(confirm)) {
            return "Please re-type your password to confirm.";
        }
        if (!password.equals(confirm)) {
            return "Password and re-type password do not match. Please try again.";
        }
        return null;
    }

    /**
     * Edit account: if user enters a new password, both fields required and must match.
     * If both empty, password is unchanged (OK).
     */
    public static String validateOptionalPasswordChange(String password, String confirm) {
        boolean hasPw = !isBlank(password);
        boolean hasCf = !isBlank(confirm);
        if (!hasPw && !hasCf) {
            return null;
        }
        if (hasPw != hasCf) {
            return "Enter both new password and re-type password, or leave both empty.";
        }
        if (!password.equals(confirm)) {
            return "New password and re-type password do not match.";
        }
        return null;
    }
}
