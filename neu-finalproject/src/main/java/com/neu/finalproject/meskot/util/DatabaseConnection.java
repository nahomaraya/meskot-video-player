package com.neu.finalproject.meskot.util;

import java.sql.*;

/**
 * Direct JDBC connection utility for Supabase PostgreSQL
 * This provides an alternative authentication method alongside Spring Boot JPA
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:5432/postgres";
    private static final String USERNAME = "postgres.sykcyulhobvhsrssxldd";
    private static final String PASSWORD = "YqTRixflrMN9HeM1";
    
    static {
        try {

            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found: " + e.getMessage());
            System.err.println("Add PostgreSQL JDBC driver to your project libraries");
        }
    }
    
    /**
     * Get a direct JDBC connection to Supabase PostgreSQL
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        System.out.println("Attempting PostgreSQL database connection...");
        Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        System.out.println("PostgreSQL database connection established");
        return conn;
    }
    
    /**
     * Test the database connection
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("PostgreSQL connection test: PASSED");
            

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT version()");
            if (rs.next()) {
                System.out.println("PostgreSQL Version: " + rs.getString(1));
            }
            
        } catch (SQLException e) {
            System.err.println("PostgreSQL connection test: FAILED - " + e.getMessage());
            System.err.println("Check if PostgreSQL is running on port 5432");
            System.err.println("Verify database exists");
            System.err.println("Check username/password credentials");
        }
    }
    
    /**
     * Display detailed database information
     */
    public static void checkDatabaseInfo() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database Information:");
            System.out.println("   Database: " + meta.getDatabaseProductName());
            System.out.println("   Version: " + meta.getDatabaseProductVersion());
            System.out.println("   URL: " + meta.getURL());
            System.out.println("   User: " + meta.getUserName());
            
        } catch (SQLException e) {
            System.err.println("Error getting database info: " + e.getMessage());
        }
    }
    
    /**
     * Authenticate user using direct JDBC (alternative to JPA)
     * @param username Username
     * @param password Plain text password
     * @return true if authentication successful
     */
    public static boolean authenticateUser(String username, String password) {
        String query = "SELECT id, username, password_hash FROM users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                // TODO: Use proper password hashing (BCrypt/Argon2)
                // For now, this is a placeholder
                System.out.println("User found: " + username);
                return storedHash != null; // Replace with actual password verification
            } else {
                System.out.println("User not found: " + username);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user information by username
     * @param username Username to lookup
     * @return ResultSet with user data (remember to close it!)
     */
    public static ResultSet getUserByUsername(String username) throws SQLException {
        Connection conn = getConnection();
        String query = "SELECT id, username, email, is_admin, created_at FROM users WHERE username = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        return pstmt.executeQuery();
    }
    
    /**
     * Register a new user using direct JDBC
     * @param username Username
     * @param email Email
     * @param passwordHash Hashed password
     * @return true if registration successful
     */
    public static boolean registerUser(String username, String email, String passwordHash) {
        String query = "INSERT INTO users (username, email, password_hash, is_admin, created_at) VALUES (?, ?, ?, 0, NOW())";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ User registered: " + username);
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            return false;
        }
    }
}
