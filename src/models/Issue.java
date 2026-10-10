package models;

import java.sql.Timestamp;

/**
 * Model representing a campus issue reported by a student.
 */
public class Issue {
    private int issueId;
    private String title;
    private String category; // Hostel, Mess, IT, Sanitation, Infrastructure
    private String location;
    private String description;
    private String status; // PENDING, IN_PROGRESS, RESOLVED
    private int upvotes;
    private int createdBy;
    private String creatorName; // Populated from users table join for display
    private Timestamp createdAt;

    public Issue() {
        this.status = "PENDING";
        this.upvotes = 1;
    }

    public Issue(String title, String category, String location, String description, int createdBy) {
        this.title = title;
        this.category = category;
        this.location = location;
        this.description = description;
        this.createdBy = createdBy;
        this.status = "PENDING";
        this.upvotes = 1;
    }

    public Issue(int issueId, String title, String category, String location, String description,
                 String status, int upvotes, int createdBy, String creatorName, Timestamp createdAt) {
        this.issueId = issueId;
        this.title = title;
        this.category = category;
        this.location = location;
        this.description = description;
        this.status = status;
        this.upvotes = upvotes;
        this.createdBy = createdBy;
        this.creatorName = creatorName;
        this.createdAt = createdAt;
    }

    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "issueId=" + issueId +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                ", upvotes=" + upvotes +
                ", createdBy=" + createdBy +
                ", creatorName='" + creatorName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
