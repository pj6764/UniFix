package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton / Static utility to manage JDBC connection to MySQL database.
 * Supports configurable credentials via environment variables and system properties,
 * with resilient defaults for local development.
 */
public class DatabaseConnection {

    private static final String DEFAULT_HOST = "localhost:3306";
    private static final String DEFAULT_DB = "unifix_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "i@mpranav";

    private static Connection connection = null;

    // Private constructor to prevent direct instantiation
    private DatabaseConnection() {}

    /**
     * Resolves configuration property with priority:
     * 1. System property (e.g. -Ddb.password=...)
     * 2. Environment variable (e.g. UNIFIX_DB_PASSWORD)
     * 3. Fallback default
     */
    private static String getEnvOrProp(String sysProp, String envVar, String defaultValue) {
        String prop = System.getProperty(sysProp);
        if (prop != null && !prop.trim().isEmpty()) {
            return prop.trim();
        }
        String env = System.getenv(envVar);
        if (env != null && !env.trim().isEmpty()) {
            return env.trim();
        }
        return defaultValue;
    }

    /**
     * Retrieves or establishes a connection to the MySQL database.
     *
     * @return Connection instance, or null if connection fails.
     */
    public static synchronized Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed() && connection.isValid(2)) {
                return connection;
            }

            // Ensure Driver class is registered
            Class.forName("com.mysql.cj.jdbc.Driver");

            String host = getEnvOrProp("db.host", "UNIFIX_DB_HOST", DEFAULT_HOST);
            String database = getEnvOrProp("db.name", "UNIFIX_DB_NAME", DEFAULT_DB);
            String user = getEnvOrProp("db.user", "UNIFIX_DB_USER", DEFAULT_USER);
            String password = getEnvOrProp("db.password", "UNIFIX_DB_PASSWORD", DEFAULT_PASSWORD);

            String url = String.format(
                "jdbc:mysql://%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, database
            );

            connection = DriverManager.getConnection(url, user, password);
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection ERROR] MySQL JDBC Driver not found in classpath: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection ERROR] Failed to connect to MySQL database: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the database is reachable.
     */
    public static boolean isConnected() {
        Connection conn = getConnection();
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Safely closes the active connection if open.
     */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("[DatabaseConnection ERROR] Failed to close connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
