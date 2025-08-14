package com.hermes.userservice.exception;

import com.hermes.jwt.exception.BusinessException;

// 사용자를 찾을 수 없을 때 발생하는 예외

public class UserNotFoundException extends BusinessException {
    
    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, "USER_NOT_FOUND", cause);
    }
}
