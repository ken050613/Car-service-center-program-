package apu.gui;

import apu.bootstrap.AscBootstrap;

import javax.swing.*;

/** Starts the Swing desktop UI. */
public final class SwingApp {
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
                        "ASC",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
