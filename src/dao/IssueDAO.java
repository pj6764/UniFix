package dao;

import config.DatabaseConnection;
import models.Issue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Issue entities.
 * Handles issue reporting, retrieval, status updates, and transactional upvoting.
 */
public class IssueDAO {

    /**
     * Creates a new campus issue and automatically registers the creator's initial upvote in a transaction.
     *
     * @param issue Issue object to insert
     * @return true if successfully created, false otherwise.
     */
    public boolean createIssue(Issue issue) {
        String insertIssueSql = "INSERT INTO issues (title, category, location, description, status, upvotes, created_by) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String insertUpvoteSql = "INSERT INTO issue_upvotes (issue_id, user_id) VALUES (?, ?)";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("[IssueDAO ERROR] Database connection is null.");
            return false;
        }

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Begin transaction

            int generatedIssueId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(insertIssueSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, issue.getTitle().trim());
                stmt.setString(2, issue.getCategory().trim());
                stmt.setString(3, issue.getLocation().trim());
                stmt.setString(4, issue.getDescription().trim());
                stmt.setString(5, issue.getStatus() != null ? issue.getStatus() : "PENDING");
                stmt.setInt(6, 1); // Initial upvote count
                stmt.setInt(7, issue.getCreatedBy());

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedIssueId = rs.getInt(1);
                        issue.setIssueId(generatedIssueId);
                    }
                }
            }

            // Record creator's initial upvote in issue_upvotes table
            if (generatedIssueId > 0 && issue.getCreatedBy() > 0) {
                try (PreparedStatement upvoteStmt = conn.prepareStatement(insertUpvoteSql)) {
                    upvoteStmt.setInt(1, generatedIssueId);
                    upvoteStmt.setInt(2, issue.getCreatedBy());
                    upvoteStmt.executeUpdate();
                }
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.err.println("[IssueDAO ERROR] Failed to create issue: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rbEx) {
                System.err.println("[IssueDAO ERROR] Rollback failed: " + rbEx.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                System.err.println("[IssueDAO ERROR] Failed to restore autoCommit: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if a user has already upvoted an issue.
     *
     * @param issueId Target issue ID
     * @param userId Current user ID
     * @return true if user already voted, false otherwise.
     */
    public boolean hasUserUpvoted(int issueId, int userId) {
        String sql = "SELECT 1 FROM issue_upvotes WHERE issue_id = ? AND user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, issueId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[IssueDAO ERROR] Failed to check upvote status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Atomically upvotes an issue by adding a record to issue_upvotes and incrementing
     * the issue's upvote count in issues table within a transaction.
     *
     * @param issueId Target issue ID
     * @param userId Current user ID
     * @return true if upvoted successfully, false if already upvoted or on failure.
     */
    public boolean upvoteIssue(int issueId, int userId) {
        if (hasUserUpvoted(issueId, userId)) {
            return false; // Prevent duplicate upvote
        }

        String insertUpvoteSql = "INSERT INTO issue_upvotes (issue_id, user_id) VALUES (?, ?)";
        String incrementSql = "UPDATE issues SET upvotes = upvotes + 1 WHERE issue_id = ?";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return false;
        }

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement upvoteStmt = conn.prepareStatement(insertUpvoteSql)) {
                upvoteStmt.setInt(1, issueId);
                upvoteStmt.setInt(2, userId);
                upvoteStmt.executeUpdate();
            }

            try (PreparedStatement incStmt = conn.prepareStatement(incrementSql)) {
                incStmt.setInt(1, issueId);
                incStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.err.println("[IssueDAO ERROR] Failed to upvote issue: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rbEx) {
                System.err.println("[IssueDAO ERROR] Rollback failed: " + rbEx.getMessage());
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                System.err.println("[IssueDAO ERROR] Failed to restore autoCommit: " + e.getMessage());
            }
        }
    }

    /**
     * Updates status of an issue (PENDING, IN_PROGRESS, RESOLVED).
     *
     * @param issueId Target issue ID
     * @param newStatus New status value
     * @return true if updated, false otherwise.
     */
    public boolean updateIssueStatus(int issueId, String newStatus) {
        String sql = "UPDATE issues SET status = ? WHERE issue_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.toUpperCase());
            stmt.setInt(2, issueId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[IssueDAO ERROR] Failed to update issue status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all issues sorted by urgency (upvotes DESC) or recent submission (created_at DESC).
     *
     * @param sortByUpvotes If true, sort by upvotes DESC; otherwise sort by created_at DESC.
     * @return List of Issue models
     */
    public List<Issue> getAllIssues(boolean sortByUpvotes) {
        return getFilteredIssues(null, null, sortByUpvotes);
    }

    /**
     * Retrieves issues with optional category and status filters.
     *
     * @param categoryFilter Category to filter by (or null/All)
     * @param statusFilter Status to filter by (or null/All)
     * @param sortByUpvotes If true, sort by upvotes DESC; otherwise sort by created_at DESC.
     * @return List of matching Issue models
     */
    public List<Issue> getFilteredIssues(String categoryFilter, String statusFilter, boolean sortByUpvotes) {
        List<Issue> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT i.issue_id, i.title, i.category, i.location, i.description, i.status, " +
            "       i.upvotes, i.created_by, COALESCE(u.name, 'Anonymous') AS creator_name, i.created_at " +
            "FROM issues i " +
            "LEFT JOIN users u ON i.created_by = u.user_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (categoryFilter != null && !categoryFilter.trim().isEmpty() && !categoryFilter.equalsIgnoreCase("All")) {
            sql.append("AND i.category = ? ");
            params.add(categoryFilter.trim());
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("All")) {
            sql.append("AND i.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }

        if (sortByUpvotes) {
            sql.append("ORDER BY i.upvotes DESC, i.created_at DESC");
        } else {
            sql.append("ORDER BY i.created_at DESC");
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return list;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Issue issue = new Issue(
                        rs.getInt("issue_id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getInt("upvotes"),
                        rs.getInt("created_by"),
                        rs.getString("creator_name"),
                        rs.getTimestamp("created_at")
                    );
                    list.add(issue);
                }
            }
        } catch (SQLException e) {
            System.err.println("[IssueDAO ERROR] Failed to fetch issues: " + e.getMessage());
        }
        return list;
    }
}
