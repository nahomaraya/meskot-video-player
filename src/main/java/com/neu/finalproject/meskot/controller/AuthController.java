package com.neu.finalproject.meskot.controller;

import com.neu.finalproject.meskot.model.User;
import com.neu.finalproject.meskot.security.SessionManager;
import com.neu.finalproject.meskot.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final SessionManager sessionManager;

    /** Login endpoint */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userService.login(request.getUsername(), request.getPassword());
        if (userOpt.isPresent()) {
            sessionManager.startSession(userOpt.get());
            return ResponseEntity.ok(new AuthResponse("Login successful", userOpt.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    /** Registration endpoint */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        Optional<User> existing = userService.findByUsername(req.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        User saved = userService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse("Registered successfully", saved));
    }

    /** Logout endpoint */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        sessionManager.endSession();
        return ResponseEntity.ok("Logged out");
    }

    @Data
    @AllArgsConstructor
    static class AuthResponse {
        private String message;
        private User user;
    }
    @Data
    static class AuthRequest {
        private String username;
        private String password;
    }
    @Data
    static class RegisterRequest {
        private String username;
        private String email;
        private String password;
    }
}
