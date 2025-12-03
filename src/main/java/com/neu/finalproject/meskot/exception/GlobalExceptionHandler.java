package com.neu.finalproject.meskot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthException(AuthenticationException ex) {
        AuthenticationException.ErrorType type = ex.getErrorType();
        HttpStatus status;
        switch (type) {
            case NOT_LOGGED_IN:
            case SESSION_EXPIRED:
            case INVALID_CREDENTIALS:
                status = HttpStatus.UNAUTHORIZED; // 401
                break;
            case INSUFFICIENT_PERMISSIONS:
            case USER_BANNED:
                status = HttpStatus.FORBIDDEN; // 403
                break;
            default:
                status = HttpStatus.UNAUTHORIZED;
        }
        return ResponseEntity.status(status)
                .body(new ErrorResponse(type.name(), ex.getMessage()));
    }

    static class ErrorResponse {
        public String errorType;
        public String message;
        public ErrorResponse(String errorType, String message) {
            this.errorType = errorType;
            this.message = message;
        }
    }
}
