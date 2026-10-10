import config.DatabaseConnection;
import ui.LoginFrame;

import javax.swing.*;

/**
 * Main application entry point for UniFix.
 * Initializes the native System Look and Feel, verifies database connectivity,
 * and launches the LoginFrame on the Swing Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {
        // Set native System Look and Feel for clean desktop UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] Could not set System Look and Feel: " + e.getMessage());
        }

        // Launch on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Verify database connectivity
            if (!DatabaseConnection.isConnected()) {
                System.err.println("[Main WARNING] Could not establish connection to MySQL database 'unifix_db'.");
                JOptionPane.showMessageDialog(
                    null,
                    "Warning: Could not connect to MySQL database 'unifix_db'.\n\n" +
                    "Please ensure:\n" +
                    "1. MySQL server is running on localhost:3306\n" +
                    "2. Database 'unifix_db' has been imported (see sql/schema.sql)\n" +
                    "3. Credentials in DatabaseConnection.java match your MySQL configuration.",
                    "Database Connection Notice",
                    JOptionPane.WARNING_MESSAGE
                );
            } else {
                System.out.println("[Main INFO] Successfully connected to MySQL 'unifix_db'.");
            }

            // Display Login Frame
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
