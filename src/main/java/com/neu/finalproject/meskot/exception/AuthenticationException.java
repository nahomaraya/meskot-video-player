package com.neu.finalproject.meskot.exception;

public class AuthenticationException extends RuntimeException {
    public enum ErrorType {
        NOT_LOGGED_IN, SESSION_EXPIRED, INSUFFICIENT_PERMISSIONS, USER_BANNED, INVALID_CREDENTIALS
    }

    private final ErrorType errorType;

    public AuthenticationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
