package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.util.DatabaseConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Alternative authentication service using direct JDBC instead of JPA
 * This can be used alongside or instead of the JPA-based UserService
 */
@Service
public class JdbcAuthenticationService {
    
    /**
     * Authenticate user using direct JDBC connection
     * @param username Username
     * @param password Plain text password
     * @return User information map if successful, null otherwise
     */
    public Map<String, Object> authenticate(String username, String password) {
        String query = "SELECT id, username, email, password_hash, is_admin, created_at FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                
                // TODO: Implement proper password verification with BCrypt
                // For now, this is a placeholder - you should use:
                // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                // if (encoder.matches(password, storedHash))
                
                System.out.println("✅ JDBC Auth: User found - " + username);
                
                // Return user information
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", rs.getLong("id"));
                userInfo.put("username", rs.getString("username"));
                userInfo.put("email", rs.getString("email"));
                userInfo.put("isAdmin", rs.getInt("is_admin") == 1);
                userInfo.put("createdAt", rs.getTimestamp("created_at"));
                
                return userInfo;
            } else {
                System.out.println("❌ JDBC Auth: User not found - " + username);
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ JDBC Auth error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Register a new user using direct JDBC
     * @param username Username
     * @param email Email
     * @param password Plain text password
     * @return User ID if successful, null otherwise
     */
    public Long register(String username, String email, String password) {
        // TODO: Hash password with BCrypt before storing
        // BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // String passwordHash = encoder.encode(password);
        String passwordHash = password; // TEMPORARY - hash this properly!
        
        String query = "INSERT INTO users (username, email, password_hash, is_admin, created_at) " +
                       "VALUES (?, ?, ?, 0, NOW()) RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, passwordHash);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Long userId = rs.getLong("id");
                System.out.println("✅ JDBC Registration: User created - " + username + " (ID: " + userId + ")");
                return userId;
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("❌ JDBC Registration error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if username already exists
     * @param username Username to check
     * @return true if exists, false otherwise
     */
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking username: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return User information map
     */
    public Map<String, Object> getUserById(Long userId) {
        String query = "SELECT id, username, email, is_admin, created_at FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", rs.getLong("id"));
                userInfo.put("username", rs.getString("username"));
                userInfo.put("email", rs.getString("email"));
                userInfo.put("isAdmin", rs.getInt("is_admin") == 1);
                userInfo.put("createdAt", rs.getTimestamp("created_at"));
                return userInfo;
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting user: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Update user to admin
     * @param userId User ID
     * @return true if successful
     */
    public boolean makeAdmin(Long userId) {
        String query = "UPDATE users SET is_admin = 1 WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setLong(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ User " + userId + " promoted to admin");
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test the JDBC connection
     */
    public void testConnection() {
        DatabaseConnection.testConnection();
        DatabaseConnection.checkDatabaseInfo();
    }
}
