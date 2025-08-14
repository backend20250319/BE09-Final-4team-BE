package com.hermes.userservice.exception;

// 권한이 부족한 경우 발생하는 예외
public class InsufficientPermissionException extends BusinessException {
    
    public InsufficientPermissionException(String message) {
        super(message, "INSUFFICIENT_PERMISSION");
    }
    
    public InsufficientPermissionException(String message, Throwable cause) {
        super(message, "INSUFFICIENT_PERMISSION", cause);
    }
}
