package com.hermes.userservice.exception;

// JWT 토큰 검증 실패 시 발생하는 예외

public class JwtValidationException extends SecurityException {
    
    public JwtValidationException(String reason) {
        super("JWT_VALIDATION_FAILED", 
              String.format("JWT 검증 실패: %s", reason));
    }
    
    public JwtValidationException(String message, Throwable cause) {
        super("JWT_VALIDATION_FAILED", message, cause);
    }
}
