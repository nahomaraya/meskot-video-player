package com.neu.finalproject.meskot.ui;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDatabaseService {
    // Supabase connection with pooler format
    private static final String DB_URL = "";
    private static final String USER = "";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    static {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Connecting to Supabase...");
            System.out.println("URL: " + DB_URL);
            System.out.println("User: " + USER);

            // Try connection with proper credentials
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("Connected to Supabase successfully!");
        }
        return connection;
    }

    public static List<Map<String, Object>> executeQuery(String query, Object... params) throws SQLException {
        System.out.println("\nExecuting query: " + query);

        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            System.out.println("Query returned " + columnCount + " columns");

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

            System.out.println("Retrieved " + results.size() + " rows");

        } catch (SQLException e) {
            System.err.println("Query failed: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            throw e;
        }
        return results;
    }

    public static int executeUpdate(String query, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();
        }
    }

    // Test method to verify connection and table structure
    public static void testDatabase() {
        try (Connection conn = getConnection()) {
            System.out.println("\n=== Database Test ===");

            // 1. List all tables
            System.out.println("\n1. Listing all tables:");
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, "public", "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("   - " + tableName);
            }

            // 2. Check movies table specifically
            System.out.println("\n2. Checking movies table structure:");
            ResultSet columns = meta.getColumns(null, "public", "movies", "%");
            boolean hasMoviesTable = false;
            while (columns.next()) {
                hasMoviesTable = true;
                System.out.println("   - " + columns.getString("COLUMN_NAME") +
                        " : " + columns.getString("TYPE_NAME"));
            }

            if (!hasMoviesTable) {
                System.err.println("   Movies table not found!");
                System.err.println("   Looking for any table with 'movie' in name:");
                tables = meta.getTables(null, "public", "%movie%", new String[]{"TABLE"});
                while (tables.next()) {
                    System.out.println("   Found: " + tables.getString("TABLE_NAME"));
                }
            }

            // 3. Try to query movies
            System.out.println("\n3. Testing movie query:");
            try {
                List<Map<String, Object>> movies = executeQuery("SELECT * FROM movies LIMIT 5");
                if (!movies.isEmpty()) {
                    System.out.println("   First movie: " + movies.get(0));
                } else {
                    System.out.println("   Movies table is empty");
                }
            } catch (SQLException e) {
                System.err.println("   Error querying movies: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("Database test failed: " + e.getMessage());
        }
    }
}