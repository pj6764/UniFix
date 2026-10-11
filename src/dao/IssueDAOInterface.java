package dao;

import models.Issue;
import java.util.List;

/**
 * Contract interface defining operations for Issue data access.
 * Implemented by IssueDAO to demonstrate OOP interface abstraction.
 */
public interface IssueDAOInterface {

    /**
     * Inserts a new campus issue and registers initial creator upvote.
     */
    boolean createIssue(Issue issue);

    /**
     * Checks if a user has already upvoted an issue.
     */
    boolean hasUserUpvoted(int issueId, int userId);

    /**
     * Atomically upvotes an issue within a transaction.
     */
    boolean upvoteIssue(int issueId, int userId);

    /**
     * Updates resolution status of an issue.
     */
    boolean updateIssueStatus(int issueId, String newStatus);

    /**
     * Deletes an issue by ID (cascades related upvotes).
     */
    boolean deleteIssue(int issueId);

    /**
     * Retrieves all issues sorted by urgency or submission date.
     */
    List<Issue> getAllIssues(boolean sortByUpvotes);

    /**
     * Retrieves issues with category and status filters.
     */
    List<Issue> getFilteredIssues(String categoryFilter, String statusFilter, boolean sortByUpvotes);
}
