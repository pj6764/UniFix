package ui;

import dao.IssueDAO;
import models.Issue;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Student Dashboard for UniFix.
 * Allows students to view campus issues, upvote issues, and report new issues.
 */
public class StudentDashboard extends JFrame {

    private final User currentUser;
    private final IssueDAO issueDAO;

    private JTable issueTable;
    private DefaultTableModel tableModel;
    private List<Issue> currentIssueList;
    private JLabel statusFeedbackLabel;

    public StudentDashboard(User user) {
        this.currentUser = user;
        this.issueDAO = new IssueDAO();
        initComponents();
        loadIssues();
    }

    private void initComponents() {
        setTitle("UniFix - Student Dashboard (" + currentUser.getName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(new Color(248, 249, 250));

        // 1. Top Bar Panel
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("UniFix Student Portal");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(24, 76, 120));

        JLabel welcomeLabel = new JLabel("| Logged in as: " + currentUser.getName() + " (" + currentUser.getEmail() + ")");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeLabel.setForeground(new Color(100, 105, 115));

        userPanel.add(titleLabel);
        userPanel.add(welcomeLabel);

        JButton logoutBtn = UIUtils.createDangerButton("Logout", new Dimension(85, 32));
        logoutBtn.addActionListener(this::handleLogout);

        topBar.add(userPanel, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);
        contentPane.add(topBar, BorderLayout.NORTH);

        // 2. Center Table Panel
        JPanel tableContainer = new JPanel(new BorderLayout(5, 5));
        tableContainer.setOpaque(false);

        String[] columns = {"ID", "Title", "Category", "Location", "Status", "Upvotes", "Reported By", "Date Reported"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };

        issueTable = new JTable(tableModel);
        issueTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        issueTable.setRowHeight(28);
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issueTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        issueTable.getTableHeader().setBackground(new Color(235, 240, 245));

        // Column widths
        issueTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        issueTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        issueTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        issueTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        issueTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        issueTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        issueTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        issueTable.getColumnModel().getColumn(7).setPreferredWidth(120);

        // Center align ID and Upvotes
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        issueTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        issueTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(issueTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Double click listener to view full issue description
        issueTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showSelectedIssueDetails();
                }
            }
        });

        contentPane.add(tableContainer, BorderLayout.CENTER);

        // 3. Bottom Action Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionsPanel.setOpaque(false);

        JButton upvoteBtn = UIUtils.createSuccessButton("▲ Upvote Selected Issue", null);
        upvoteBtn.addActionListener(this::handleUpvote);

        JButton reportBtn = UIUtils.createPrimaryButton("+ Report New Issue", null);
        reportBtn.addActionListener(e -> openReportIssueDialog());

        JButton viewDetailsBtn = UIUtils.createSecondaryButton("View Details", null);
        viewDetailsBtn.addActionListener(e -> showSelectedIssueDetails());

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh", null);
        refreshBtn.addActionListener(e -> loadIssues());

        actionsPanel.add(upvoteBtn);
        actionsPanel.add(reportBtn);
        actionsPanel.add(viewDetailsBtn);
        actionsPanel.add(refreshBtn);

        statusFeedbackLabel = new JLabel("Tip: Double-click an issue to view full description.");
        statusFeedbackLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusFeedbackLabel.setForeground(new Color(110, 115, 125));
        statusFeedbackLabel.setBorder(new EmptyBorder(0, 5, 5, 0));

        bottomPanel.add(actionsPanel, BorderLayout.WEST);
        bottomPanel.add(statusFeedbackLabel, BorderLayout.SOUTH);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void loadIssues() {
        tableModel.setRowCount(0);
        // Student view: show sorted by urgency (highest upvotes first)
        currentIssueList = issueDAO.getAllIssues(true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (Issue issue : currentIssueList) {
            String dateStr = issue.getCreatedAt() != null ? dateFormat.format(issue.getCreatedAt()) : "N/A";
            tableModel.addRow(new Object[]{
                issue.getIssueId(),
                issue.getTitle(),
                issue.getCategory(),
                issue.getLocation(),
                issue.getStatus(),
                issue.getUpvotes(),
                issue.getCreatorName(),
                dateStr
            });
        }
        statusFeedbackLabel.setText("Loaded " + currentIssueList.size() + " issue(s). Showing highest upvoted first.");
    }

    private Issue getSelectedIssue() {
        int selectedRow = issueTable.getSelectedRow();
        if (selectedRow == -1 || currentIssueList == null || selectedRow >= currentIssueList.size()) {
            return null;
        }
        return currentIssueList.get(selectedRow);
    }

    private void handleUpvote(ActionEvent e) {
        Issue selected = getSelectedIssue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an issue to upvote.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (issueDAO.hasUserUpvoted(selected.getIssueId(), currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "You have already upvoted this issue.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean success = issueDAO.upvoteIssue(selected.getIssueId(), currentUser.getUserId());
        if (success) {
            JOptionPane.showMessageDialog(this, "Upvoted successfully! Thank you for prioritizing campus issues.", "Upvoted", JOptionPane.INFORMATION_MESSAGE);
            loadIssues();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to upvote. You may have already upvoted or connection was interrupted.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedIssueDetails() {
        Issue selected = getSelectedIssue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an issue to view details.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog detailsDialog = new JDialog(this, "Issue #" + selected.getIssueId() + " - " + selected.getTitle(), true);
        detailsDialog.setSize(500, 420);
        detailsDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel metaPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        metaPanel.setOpaque(false);
        metaPanel.add(new JLabel("Category: " + selected.getCategory()));
        metaPanel.add(new JLabel("Status: " + selected.getStatus()));
        metaPanel.add(new JLabel("Location: " + selected.getLocation()));
        metaPanel.add(new JLabel("Total Upvotes: " + selected.getUpvotes()));
        metaPanel.add(new JLabel("Reported By: " + selected.getCreatorName()));
        metaPanel.add(new JLabel("Created At: " + selected.getCreatedAt()));

        JTextArea descArea = new JTextArea(selected.getDescription());
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createTitledBorder("Full Description"));

        JButton closeBtn = UIUtils.createSecondaryButton("Close", new Dimension(90, 32));
        closeBtn.addActionListener(e -> detailsDialog.dispose());

        panel.add(metaPanel, BorderLayout.NORTH);
        panel.add(descScroll, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);

        detailsDialog.setContentPane(panel);
        detailsDialog.setVisible(true);
    }

    private void openReportIssueDialog() {
        JDialog reportDialog = new JDialog(this, "Report New Campus Issue", true);
        reportDialog.setSize(480, 480);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 0.3;

        // Title
        gbc.gridy = 0;
        panel.add(new JLabel("Issue Title:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField titleField = new JTextField();
        panel.add(titleField, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        String[] categories = {"Hostel", "Mess", "IT", "Sanitation", "Infrastructure"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        panel.add(categoryCombo, gbc);

        // Location
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        JTextField locationField = new JTextField();
        panel.add(locationField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextArea descArea = new JTextArea(5, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        panel.add(descScroll, gbc);

        // Submit & Cancel Buttons
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 6, 6, 6);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel", new Dimension(90, 32));
        cancelBtn.addActionListener(e -> reportDialog.dispose());

        JButton submitBtn = UIUtils.createPrimaryButton("Submit Issue", new Dimension(120, 32));
        submitBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            String location = locationField.getText().trim();
            String description = descArea.getText().trim();

            if (title.isEmpty() || location.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(reportDialog, "Please fill in all required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Issue newIssue = new Issue(title, category, location, description, currentUser.getUserId());
            boolean success = issueDAO.createIssue(newIssue);
            if (success) {
                JOptionPane.showMessageDialog(reportDialog, "Issue reported successfully! It is now open for campus upvoting.", "Success", JOptionPane.INFORMATION_MESSAGE);
                reportDialog.dispose();
                loadIssues();
            } else {
                JOptionPane.showMessageDialog(reportDialog, "Failed to submit issue. Please check database connectivity.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);
        panel.add(btnPanel, gbc);

        reportDialog.setContentPane(panel);
        reportDialog.setVisible(true);
    }

    private void handleLogout(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
