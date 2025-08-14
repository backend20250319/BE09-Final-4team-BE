package com.hermes.userservice.exception;

// 보안 관련 예외의 기본 클래스

public class SecurityException extends RuntimeException {
    
    private final String errorCode;
    
    public SecurityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SecurityException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
