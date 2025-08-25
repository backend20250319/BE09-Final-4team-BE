package com.hermes.userservice.exception;

import com.hermes.auth.exception.BusinessException;

// 잘못된 인증 정보 예외

public class InvalidCredentialsException extends BusinessException {
    
    public InvalidCredentialsException(String message) {
        super(message, "INVALID_CREDENTIALS");
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, "INVALID_CREDENTIALS", cause);
    }
}
