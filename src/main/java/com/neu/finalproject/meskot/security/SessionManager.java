package com.neu.finalproject.meskot.security;

import com.neu.finalproject.meskot.model.User;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionManager {
    private static final String USER_SESSION_ATTR = "CURRENT_USER";
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60; // 30 minutes

    private final HttpSession httpSession;

    public SessionManager(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public void startSession(User user) {
        httpSession.setAttribute(USER_SESSION_ATTR, user);
        httpSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
    }

    public void endSession() {
        httpSession.invalidate();
    }

    public boolean isLoggedIn() {
        return httpSession.getAttribute(USER_SESSION_ATTR) != null;
    }

    public User getCurrentUser() {
        return (User) httpSession.getAttribute(USER_SESSION_ATTR);
    }

    public boolean isAdmin() {
        User user = getCurrentUser();
        boolean isAdmin = user.getIsAdmin() == 1;
        return user != null && isAdmin;
    }

    public void refreshSession() {
        httpSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
    }
}
