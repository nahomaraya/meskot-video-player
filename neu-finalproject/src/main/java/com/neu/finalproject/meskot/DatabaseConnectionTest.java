package com.neu.finalproject.meskot;

import com.neu.finalproject.meskot.util.DatabaseConnection;

/**
 * Standalone test class for JDBC connection to Supabase
 * Run this to verify your direct database connection works
 */
public class DatabaseConnectionTest {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("🧪 JDBC Connection Test to Supabase PostgreSQL");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Test 1: Basic connection
        System.out.println("📍 Test 1: Basic Connection");
        DatabaseConnection.testConnection();
        System.out.println();
        
        // Test 2: Database info
        System.out.println("📍 Test 2: Database Information");
        DatabaseConnection.checkDatabaseInfo();
        System.out.println();
        
        // Test 3: User authentication (placeholder)
        System.out.println("📍 Test 3: User Authentication Test");
        boolean authResult = DatabaseConnection.authenticateUser("testuser", "testpass");
        System.out.println("Authentication result: " + (authResult ? "✅ SUCCESS" : "❌ FAILED"));
        System.out.println();
        
        System.out.println("=".repeat(60));
        System.out.println("✅ JDBC Connection Test Complete");
        System.out.println("=".repeat(60));
    }
}
