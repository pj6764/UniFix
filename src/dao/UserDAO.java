package dao;

import config.DatabaseConnection;
import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for User entities.
 * Handles authentication, registration, and user lookups via JDBC PreparedStatements.
 */
public class UserDAO {

    /**
     * Authenticates a user given email and password.
     *
     * @param email User email
     * @param password Plaintext password
     * @return User object if credentials are valid, or null otherwise.
     */
    public User authenticate(String email, String password) {
        String sql = "SELECT user_id, name, email, role FROM users WHERE email = ? AND password = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("[UserDAO ERROR] Unable to get database connection for authentication.");
            return null;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] Authentication failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if an email is already registered in the system.
     *
     * @param email Email to verify
     * @return true if email exists, false otherwise.
     */
    public boolean isEmailTaken(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] Failed to check email uniqueness: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registers a new user into the database.
     *
     * @param user User model containing name, email, and role
     * @param plainPassword Password string
     * @return true if successfully inserted, false otherwise.
     */
    public boolean registerUser(User user, String plainPassword) {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("[UserDAO ERROR] Unable to get database connection for registration.");
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getName().trim());
            stmt.setString(2, user.getEmail().trim().toLowerCase());
            stmt.setString(3, plainPassword);
            stmt.setString(4, user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO ERROR] User registration failed: " + e.getMessage());
        }
        return false;
    }
}
