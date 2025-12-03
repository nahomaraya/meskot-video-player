package com.neu.finalproject.meskot.controller;

import com.neu.finalproject.meskot.service.JdbcAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Alternative authentication controller using direct JDBC
 * Endpoints: /api/jdbc-auth/*
 */
@RestController
@RequestMapping("/api/jdbc-auth")
@CrossOrigin(origins = "*")
public class JdbcAuthController {
    
    @Autowired
    private JdbcAuthenticationService jdbcAuthService;
    
    /**
     * Login using JDBC authentication
     * POST /api/jdbc-auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Username and password required")
            );
        }
        
        Map<String, Object> userInfo = jdbcAuthService.authenticate(username, password);
        
        if (userInfo != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", userInfo);
            response.put("authMethod", "JDBC");
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("error", "Invalid username or password")
            );
        }
    }
    
    /**
     * Register using JDBC authentication
     * POST /api/jdbc-auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String email = userData.get("email");
        String password = userData.get("password");
        
        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Username, email, and password required")
            );
        }
        
        // Check if username exists
        if (jdbcAuthService.usernameExists(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("error", "Username already exists")
            );
        }
        
        Long userId = jdbcAuthService.register(username, email, password);
        
        if (userId != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("userId", userId);
            response.put("username", username);
            response.put("authMethod", "JDBC");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Registration failed")
            );
        }
    }
    
    /**
     * Get user by ID
     * GET /api/jdbc-auth/user/{id}
     */
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Map<String, Object> userInfo = jdbcAuthService.getUserById(id);
        
        if (userInfo != null) {
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "User not found")
            );
        }
    }
    
    /**
     * Promote user to admin
     * POST /api/jdbc-auth/admin/{id}
     */
    @PostMapping("/admin/{id}")
    public ResponseEntity<?> makeAdmin(@PathVariable Long id) {
        boolean success = jdbcAuthService.makeAdmin(id);
        
        if (success) {
            return ResponseEntity.ok(
                Map.of("message", "User promoted to admin", "userId", id)
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", "User not found")
            );
        }
    }
    
    /**
     * Test JDBC connection
     * GET /api/jdbc-auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        try {
            jdbcAuthService.testConnection();
            return ResponseEntity.ok(
                Map.of("message", "JDBC connection test successful", "status", "connected")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "JDBC connection test failed", "details", e.getMessage())
            );
        }
    }
}
