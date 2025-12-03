package com.neu.finalproject.meskot.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neu.finalproject.meskot.model.UploadHistory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Service for Admin operations
 * Connects to backend admin endpoints
 */
public class AdminApiService {

    private final String baseUrl = "http://localhost:8080/api";
    private final ObjectMapper objectMapper;

    public AdminApiService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // =========================================================================
    // UPLOAD HISTORY - ADMIN ENDPOINTS
    // =========================================================================

    /**
     * Get all upload history (admin only)
     * Endpoint: GET /api/history/uploads/all?limit=X
     */
    public List<UploadHistory> getAllUploadHistory(int limit) throws Exception {
        String endpoint = baseUrl + "/history/uploads/all?limit=" + limit;
        System.out.println("Admin: Fetching all upload history from: " + endpoint);

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);

        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return objectMapper.readValue(response.toString(),
                        new TypeReference<List<UploadHistory>>() {});
            }
        } else if (responseCode == 403) {
            throw new Exception("Access Denied: Admin privileges required");
        } else {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                throw new Exception("HTTP " + responseCode + ": " + error.toString());
            }
        }
    }

    /**
     * Get upload statistics (admin only)
     * Endpoint: GET /api/history/uploads/stats
     */
    public Map<String, Object> getUploadStats() throws Exception {
        String endpoint = baseUrl + "/history/uploads/stats";
        System.out.println("Admin: Fetching upload stats from: " + endpoint);

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                // For now, return a stub map since backend returns "Stats endpoint stub"
                Map<String, Object> stats = new HashMap<>();
                stats.put("message", response.toString());
                return stats;
            }
        } else if (responseCode == 403) {
            throw new Exception("Access Denied: Admin privileges required");
        } else {
            throw new Exception("HTTP " + responseCode);
        }
    }

    // =========================================================================
    // MOVIE MANAGEMENT - ADMIN OPERATIONS
    // =========================================================================

    /**
     * Delete a movie (admin operation)
     * Endpoint: DELETE /api/movies/{id}
     */
    public boolean deleteMovie(Long movieId) throws Exception {
        String endpoint = baseUrl + "/movies/" + movieId;
        System.out.println("Admin: Deleting movie at: " + endpoint);

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            System.out.println("Movie " + movieId + " deleted successfully");
            return true;
        } else if (responseCode == 404) {
            throw new Exception("Movie not found");
        } else if (responseCode == 403) {
            throw new Exception("Access Denied: Admin privileges required");
        } else {
            throw new Exception("Failed to delete movie. HTTP " + responseCode);
        }
    }

    /**
     * Update movie status (admin operation)
     * Endpoint: PATCH /api/movies/{id}/status?status=X
     */
    public boolean updateMovieStatus(Long movieId, String status) throws Exception {
        String endpoint = baseUrl + "/movies/" + movieId + "/status?status=" + status;
        System.out.println("Admin: Updating movie status at: " + endpoint);

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            System.out.println("Movie " + movieId + " status updated to: " + status);
            return true;
        } else if (responseCode == 404) {
            throw new Exception("Movie not found");
        } else if (responseCode == 403) {
            throw new Exception("Access Denied: Admin privileges required");
        } else {
            throw new Exception("Failed to update movie status. HTTP " + responseCode);
        }
    }

    // =========================================================================
    // DASHBOARD STATISTICS
    // =========================================================================

    /**
     * Get dashboard statistics (computed from available endpoints)
     */
    public Map<String, Object> getDashboardStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get upload history to compute stats
            List<UploadHistory> uploads = getAllUploadHistory(1000);
            
            stats.put("totalUploads", uploads.size());
            
            // Count by status
            long completed = uploads.stream()
                    .filter(u -> "COMPLETED".equals(u.getStatus()))
                    .count();
            long failed = uploads.stream()
                    .filter(u -> "FAILED".equals(u.getStatus()))
                    .count();
            long pending = uploads.stream()
                    .filter(u -> "PENDING".equals(u.getStatus()) || "PROCESSING".equals(u.getStatus()))
                    .count();
            
            stats.put("completedUploads", completed);
            stats.put("failedUploads", failed);
            stats.put("pendingUploads", pending);
            
        } catch (Exception e) {
            System.err.println("Error getting dashboard stats: " + e.getMessage());
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    // =========================================================================
    // USER MANAGEMENT (Using existing auth endpoints)
    // =========================================================================

    /**
     * Note: User management endpoints are not yet implemented in the backend.
     * These methods are placeholders for future implementation.
     * For now, users can be managed directly through the database.
     */

    public List<Map<String, Object>> getAllUsers() throws Exception {
        // This endpoint doesn't exist yet in backend
        // Would need to add: GET /api/admin/users
        throw new Exception("User management endpoint not yet implemented in backend");
    }

    public boolean deleteUser(Long userId) throws Exception {
        // This endpoint doesn't exist yet in backend
        // Would need to add: DELETE /api/admin/users/{id}
        throw new Exception("User management endpoint not yet implemented in backend");
    }

    public boolean updateUserRole(Long userId, String role, boolean isAdmin) throws Exception {
        // This endpoint doesn't exist yet in backend
        // Would need to add: PATCH /api/admin/users/{id}/role
        throw new Exception("User management endpoint not yet implemented in backend");
    }
}
