package com.apu.asc.gui;

import com.apu.asc.bootstrap.AscBootstrap;

import javax.swing.*;

public final class AscSwingApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // default L&F
            }
            try {
                var services = AscBootstrap.init();
                MainFrame frame = new MainFrame(services);
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to start: " + e.getMessage(),
                        "APU-ASC",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
