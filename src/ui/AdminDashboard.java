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
 * Admin Dashboard for UniFix.
 * Allows campus authorities to view crowdsourced issues prioritized by upvotes,
 * filter issues by category/status, and update resolution statuses.
 */
public class AdminDashboard extends JFrame {

    private final User adminUser;
    private final IssueDAO issueDAO;

    private JTable issueTable;
    private DefaultTableModel tableModel;
    private List<Issue> currentIssueList;

    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> categoryFilterCombo;
    private JComboBox<String> updateStatusCombo;
    private JLabel selectedIssueInfoLabel;
    private JLabel statusFeedbackLabel;

    public AdminDashboard(User admin) {
        this.adminUser = admin;
        this.issueDAO = new IssueDAO();
        initComponents();
        loadIssues();
    }

    private void initComponents() {
        setTitle("UniFix - Administrative Resolution Dashboard (" + adminUser.getName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 680);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(new Color(245, 247, 250));

        // 1. Top Bar Panel
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("UniFix Administration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(180, 40, 40));

        JLabel welcomeLabel = new JLabel("| Authority: " + adminUser.getName() + " (" + adminUser.getEmail() + ")");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        welcomeLabel.setForeground(new Color(100, 105, 115));

        titlePanel.add(titleLabel);
        titlePanel.add(welcomeLabel);

        JButton logoutBtn = UIUtils.createDangerButton("Logout", new Dimension(85, 32));
        logoutBtn.addActionListener(this::handleLogout);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);
        contentPane.add(topBar, BorderLayout.NORTH);

        // 2. Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 235), 1),
            new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel filterCategoryLabel = new JLabel("Category:");
        filterCategoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] categories = {"All", "Hostel", "Mess", "IT", "Sanitation", "Infrastructure"};
        categoryFilterCombo = new JComboBox<>(categories);
        categoryFilterCombo.addActionListener(e -> loadIssues());

        JLabel filterStatusLabel = new JLabel("Status:");
        filterStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] statuses = {"All", "PENDING", "IN_PROGRESS", "RESOLVED"};
        statusFilterCombo = new JComboBox<>(statuses);
        statusFilterCombo.addActionListener(e -> loadIssues());

        JButton resetFiltersBtn = UIUtils.createSecondaryButton("Reset Filters", null);
        resetFiltersBtn.addActionListener(e -> {
            categoryFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            loadIssues();
        });

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh", null);
        refreshBtn.addActionListener(e -> loadIssues());

        filterPanel.add(filterCategoryLabel);
        filterPanel.add(categoryFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(filterStatusLabel);
        filterPanel.add(statusFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(resetFiltersBtn);
        filterPanel.add(refreshBtn);

        // 3. Center Table
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setOpaque(false);
        centerPanel.add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Urgency (Upvotes)", "Title", "Category", "Location", "Status", "Reporter", "Reported Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        issueTable = new JTable(tableModel);
        issueTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        issueTable.setRowHeight(28);
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issueTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        issueTable.getTableHeader().setBackground(new Color(235, 240, 245));

        // Column widths
        issueTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        issueTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        issueTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        issueTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        issueTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        issueTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        issueTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        issueTable.getColumnModel().getColumn(7).setPreferredWidth(120);

        // Alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        issueTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        issueTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(issueTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Listen for row selections to update action panel
        issueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedIssueControls();
            }
        });

        // Double click to view full description
        issueTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewSelectedIssueDetails();
                }
            }
        });

        contentPane.add(centerPanel, BorderLayout.CENTER);

        // 4. South Controls & Status Update Panel
        JPanel southPanel = new JPanel(new BorderLayout(8, 8));
        southPanel.setBackground(Color.WHITE);
        southPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            new EmptyBorder(12, 16, 12, 16)
        ));

        // Action controls
        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionControls.setOpaque(false);

        JLabel updateLabel = new JLabel("Update Selected Issue Status:");
        updateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        updateStatusCombo = new JComboBox<>(new String[]{"PENDING", "IN_PROGRESS", "RESOLVED"});
        updateStatusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton updateStatusBtn = UIUtils.createPrimaryButton("Save Status Change", null);
        updateStatusBtn.addActionListener(this::handleStatusUpdate);

        JButton viewDetailsBtn = UIUtils.createSecondaryButton("View Full Details", null);
        viewDetailsBtn.addActionListener(e -> viewSelectedIssueDetails());

        actionControls.add(updateLabel);
        actionControls.add(updateStatusCombo);
        actionControls.add(updateStatusBtn);
        actionControls.add(viewDetailsBtn);

        // Selected Issue info and table stats
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        infoPanel.setOpaque(false);
        selectedIssueInfoLabel = new JLabel("No issue currently selected.");
        selectedIssueInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        selectedIssueInfoLabel.setForeground(new Color(70, 75, 85));

        statusFeedbackLabel = new JLabel("Displaying campus issues prioritized by student upvotes.");
        statusFeedbackLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusFeedbackLabel.setForeground(new Color(110, 115, 125));

        infoPanel.add(selectedIssueInfoLabel);
        infoPanel.add(statusFeedbackLabel);

        southPanel.add(actionControls, BorderLayout.NORTH);
        southPanel.add(infoPanel, BorderLayout.SOUTH);
        contentPane.add(southPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void loadIssues() {
        String category = (String) categoryFilterCombo.getSelectedItem();
        String status = (String) statusFilterCombo.getSelectedItem();
        statusFeedbackLabel.setText("Loading issues in background worker thread...");

        SwingWorker<List<Issue>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Issue> doInBackground() {
                // Background worker thread: database query without freezing UI
                return issueDAO.getFilteredIssues(category, status, true);
            }

            @Override
            protected void done() {
                try {
                    currentIssueList = get();
                    tableModel.setRowCount(0);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                    for (Issue issue : currentIssueList) {
                        String dateStr = issue.getCreatedAt() != null ? dateFormat.format(issue.getCreatedAt()) : "N/A";
                        tableModel.addRow(new Object[]{
                            issue.getIssueId(),
                            "★ " + issue.getUpvotes() + " upvotes",
                            issue.getTitle(),
                            issue.getCategory(),
                            issue.getLocation(),
                            issue.getStatus(),
                            issue.getCreatorName(),
                            dateStr
                        });
                    }
                    statusFeedbackLabel.setText("Loaded " + currentIssueList.size() + " issue(s). Highest upvoted items prioritized on top.");
                    updateSelectedIssueControls();
                } catch (Exception e) {
                    statusFeedbackLabel.setText("Error loading issues: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private Issue getSelectedIssue() {
        int selectedRow = issueTable.getSelectedRow();
        if (selectedRow == -1 || currentIssueList == null || selectedRow >= currentIssueList.size()) {
            return null;
        }
        return currentIssueList.get(selectedRow);
    }

    private void updateSelectedIssueControls() {
        Issue selected = getSelectedIssue();
        if (selected != null) {
            selectedIssueInfoLabel.setText("Selected: #" + selected.getIssueId() + " - " + selected.getTitle() + 
                " [Location: " + selected.getLocation() + " | Current Status: " + selected.getStatus() + "]");
            updateStatusCombo.setSelectedItem(selected.getStatus());
        } else {
            selectedIssueInfoLabel.setText("No issue selected. Click a table row to view or update status.");
        }
    }

    private void handleStatusUpdate(ActionEvent e) {
        Issue selected = getSelectedIssue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an issue from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newStatus = (String) updateStatusCombo.getSelectedItem();
        if (newStatus.equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this, "The issue is already marked as " + newStatus + ".", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Change status of Issue #" + selected.getIssueId() + " from " + selected.getStatus() + " to " + newStatus + "?",
            "Confirm Status Update",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = issueDAO.updateIssueStatus(selected.getIssueId(), newStatus);
            if (success) {
                JOptionPane.showMessageDialog(this, "Issue #" + selected.getIssueId() + " status updated to " + newStatus + ".", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadIssues();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status. Check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewSelectedIssueDetails() {
        Issue selected = getSelectedIssue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an issue to view details.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog detailsDialog = new JDialog(this, "Admin View: Issue #" + selected.getIssueId() + " - " + selected.getTitle(), true);
        detailsDialog.setSize(540, 450);
        detailsDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JPanel metaPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        metaPanel.setOpaque(false);
        metaPanel.add(new JLabel("Category: " + selected.getCategory()));
        metaPanel.add(new JLabel("Status: " + selected.getStatus()));
        metaPanel.add(new JLabel("Location: " + selected.getLocation()));
        metaPanel.add(new JLabel("Crowdsourced Upvotes: " + selected.getUpvotes()));
        metaPanel.add(new JLabel("Reported By: " + selected.getCreatorName()));
        metaPanel.add(new JLabel("Date: " + selected.getCreatedAt()));

        JTextArea descArea = new JTextArea(selected.getDescription());
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createTitledBorder("Full Problem Description"));

        JButton closeBtn = UIUtils.createSecondaryButton("Close", new Dimension(90, 32));
        closeBtn.addActionListener(e -> detailsDialog.dispose());

        panel.add(metaPanel, BorderLayout.NORTH);
        panel.add(descScroll, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);

        detailsDialog.setContentPane(panel);
        detailsDialog.setVisible(true);
    }

    private void handleLogout(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out of the Admin portal?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
