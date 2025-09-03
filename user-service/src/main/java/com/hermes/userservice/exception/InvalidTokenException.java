package com.hermes.userservice.exception;

// JWT 토큰 검증 실패 시 발생하는 예외

public class InvalidTokenException extends SecurityException {
    
    public InvalidTokenException(String reason) {
        super("INVALID_TOKEN",
              String.format("유효하지 않은 토큰: %s", reason));
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super("INVALID_TOKEN", message, cause);
    }
}
