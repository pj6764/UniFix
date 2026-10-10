package ui;

import dao.UserDAO;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Login and Registration window for UniFix.
 * Toggles between Login and Registration tabs, performs input validation,
 * and redirects to the appropriate dashboard based on user role.
 */
public class LoginFrame extends JFrame {

    private final UserDAO userDAO;

    // Login tab components
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;

    // Register tab components
    private JTextField regNameField;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JComboBox<String> regRoleComboBox;

    public LoginFrame() {
        this.userDAO = new UserDAO();
        initComponents();
    }

    private void initComponents() {
        setTitle("UniFix - Campus Issue Resolution System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 540);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(245, 247, 250));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("UniFix", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(24, 76, 120));

        JLabel subtitleLabel = new JLabel("Crowdsourced Campus Issue Resolution System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 110, 120));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane for Login / Register
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Login", createLoginPanel());
        tabbedPane.addTab("Register", createRegisterPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Footer info
        JLabel footerLabel = new JLabel("Default logins: admin@college.edu / alice@college.edu", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerLabel.setForeground(new Color(130, 140, 150));
        footerLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;

        // Email Label & Field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        loginEmailField = new JTextField("alice@college.edu");
        loginEmailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(loginEmailField, gbc);

        // Password Label & Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        loginPasswordField = new JPasswordField("student123");
        loginPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(loginPasswordField, gbc);

        // Login Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);

        JButton loginBtn = new JButton("Sign In");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(new Color(24, 119, 242));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(120, 38));
        loginBtn.addActionListener(this::handleLogin);
        panel.add(loginBtn, gbc);

        // Bind Enter key in password field to login button
        loginPasswordField.addActionListener(this::handleLogin);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 0.35;

        // Name
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        regNameField = new JTextField();
        regNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(regNameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel emailLabel = new JLabel("Campus Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        regEmailField = new JTextField();
        regEmailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(regEmailField, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(roleLabel, gbc);

        gbc.gridx = 1;
        regRoleComboBox = new JComboBox<>(new String[]{"STUDENT", "ADMIN"});
        regRoleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(regRoleComboBox, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(passLabel, gbc);

        gbc.gridx = 1;
        regPasswordField = new JPasswordField();
        regPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(regPasswordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(confirmPassLabel, gbc);

        gbc.gridx = 1;
        regConfirmPasswordField = new JPasswordField();
        regConfirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(regConfirmPasswordField, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 6, 6, 6);

        JButton registerBtn = new JButton("Create Account");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerBtn.setBackground(new Color(34, 139, 34));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setPreferredSize(new Dimension(140, 36));
        registerBtn.addActionListener(this::handleRegister);
        panel.add(registerBtn, gbc);

        return panel;
    }

    private void handleLogin(ActionEvent e) {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.", "Input Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.authenticate(email, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Welcome back, " + user.getName() + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();

            if (user.isAdmin()) {
                SwingUtilities.invokeLater(() -> new AdminDashboard(user).setVisible(true));
            } else {
                SwingUtilities.invokeLater(() -> new StudentDashboard(user).setVisible(true));
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password. Please verify your credentials.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister(ActionEvent e) {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirmPass = new String(regConfirmPasswordField.getPassword());
        String role = (String) regRoleComboBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.isEmailTaken(email)) {
            JOptionPane.showMessageDialog(this, "An account with this email already exists.", "Registration Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User newUser = new User(name, email, role);
        boolean success = userDAO.registerUser(newUser, password);
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful! You may now sign in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            regNameField.setText("");
            regEmailField.setText("");
            regPasswordField.setText("");
            regConfirmPasswordField.setText("");
            loginEmailField.setText(email);
            loginPasswordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create account. Please check database connectivity.", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
