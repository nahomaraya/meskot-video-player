package com.neu.finalproject.meskot.ui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Handles all authentication-related API calls to the backend
 */
public class AuthApiService {

    private final String baseUrl = "http://localhost:8080/api/auth";
    private final ObjectMapper objectMapper;

    public AuthApiService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Login user with username and password
     * @return AuthResponse containing user information if successful
     * @throws Exception if login fails
     */
    public AuthResponse login(String username, String password) throws Exception {
        String endpoint = baseUrl + "/login";
        
        // Create request body
        String requestBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                username, password
        );

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200) {
            // Success - read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                JsonNode root = objectMapper.readTree(response.toString());
                AuthResponse authResponse = new AuthResponse();
                authResponse.setMessage(root.has("message") ? root.get("message").asText() : "Login successful");
                
                if (root.has("user")) {
                    JsonNode userNode = root.get("user");
                    UserInfo user = new UserInfo();
                    user.setId(userNode.has("id") ? userNode.get("id").asLong() : null);
                    user.setUsername(userNode.has("username") ? userNode.get("username").asText() : null);
                    user.setEmail(userNode.has("email") ? userNode.get("email").asText() : null);
                    authResponse.setUser(user);
                }
                
                return authResponse;
            }
        } else if (responseCode == 401) {
            throw new Exception("Invalid username or password");
        } else {
            // Read error response
            String errorMessage = "Login failed with HTTP " + responseCode;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                errorMessage = error.toString();
            } catch (Exception e) {
                // Ignore if can't read error stream
            }
            throw new Exception(errorMessage);
        }
    }

    /**
     * Register a new user
     * @return AuthResponse containing user information if successful
     * @throws Exception if registration fails
     */
    public AuthResponse register(String username, String email, String password) throws Exception {
        String endpoint = baseUrl + "/register";
        
        // Create request body
        String requestBody = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                username, email, password
        );

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        
        if (responseCode == 201 || responseCode == 200) {
            // Success - read response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                JsonNode root = objectMapper.readTree(response.toString());
                AuthResponse authResponse = new AuthResponse();
                authResponse.setMessage(root.has("message") ? root.get("message").asText() : "Registration successful");
                
                if (root.has("user")) {
                    JsonNode userNode = root.get("user");
                    UserInfo user = new UserInfo();
                    user.setId(userNode.has("id") ? userNode.get("id").asLong() : null);
                    user.setUsername(userNode.has("username") ? userNode.get("username").asText() : null);
                    user.setEmail(userNode.has("email") ? userNode.get("email").asText() : null);
                    authResponse.setUser(user);
                }
                
                return authResponse;
            }
        } else if (responseCode == 409) {
            throw new Exception("Username already exists");
        } else {
            // Read error response
            String errorMessage = "Registration failed with HTTP " + responseCode;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                errorMessage = error.toString();
            } catch (Exception e) {
                // Ignore if can't read error stream
            }
            throw new Exception(errorMessage);
        }
    }

    /**
     * Logout the current user
     * @throws Exception if logout fails
     */
    public void logout() throws Exception {
        String endpoint = baseUrl + "/logout";

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
            throw new Exception("Logout failed with HTTP " + responseCode);
        }
    }

    // =========================================================================
    // Response DTOs
    // =========================================================================

    public static class AuthResponse {
        private String message;
        private UserInfo user;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public UserInfo getUser() {
            return user;
        }

        public void setUser(UserInfo user) {
            this.user = user;
        }
    }

    public static class UserInfo {
        private Long id;
        private String username;
        private String email;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
}
