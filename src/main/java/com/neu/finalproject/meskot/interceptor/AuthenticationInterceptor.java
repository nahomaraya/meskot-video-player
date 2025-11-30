package com.neu.finalproject.meskot.interceptor;

import com.neu.finalproject.meskot.exception.AuthenticationException;
import com.neu.finalproject.meskot.security.SessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.annotation.Annotation;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final SessionManager sessionManager;
    public static final String REQ_ATTR_CURRENT_USER = "currentUser";
    public AuthenticationInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod method = (HandlerMethod) handler;
        boolean requiresAuth = isAnnotationPresent(method, RequiresAuth.class);
        boolean requiresUpload = isAnnotationPresent(method, RequiresUploadPermission.class);
        boolean requiresAdmin = isAnnotationPresent(method, RequiresAdmin.class);
        if (requiresAuth || requiresUpload || requiresAdmin) {
            if (!sessionManager.isLoggedIn()) {
                throw new AuthenticationException(AuthenticationException.ErrorType.NOT_LOGGED_IN, "User must be logged in.");
            }
            if (sessionExpired(request)) {
                sessionManager.endSession();
                throw new AuthenticationException(AuthenticationException.ErrorType.SESSION_EXPIRED, "Session expired.");
            }
            request.setAttribute(REQ_ATTR_CURRENT_USER, sessionManager.getCurrentUser());
        }
        if (requiresAdmin) {
            if (!sessionManager.isAdmin()) {
                throw new AuthenticationException(AuthenticationException.ErrorType.INSUFFICIENT_PERMISSIONS, "Admin privileges required.");
            }
        } else if (requiresUpload) {
            // Add more permission logic here if needed later.
        }
        sessionManager.refreshSession();
        return true;
    }
    private boolean sessionExpired(HttpServletRequest request) {
        return false; // Stub - HttpSession itself handles real timeout
    }
    private boolean isAnnotationPresent(HandlerMethod method, Class<? extends Annotation> annotationClass) {
        if (method.hasMethodAnnotation(annotationClass)) {
            return true;
        }
        return method.getBeanType().isAnnotationPresent(annotationClass);
    }
}
