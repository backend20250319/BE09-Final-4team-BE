package com.hermes.auth.exception;

public class InsufficientPermissionException extends BusinessException {
    
    public InsufficientPermissionException(String message) {
        super(message, "INSUFFICIENT_PERMISSION");
    }
    
    public InsufficientPermissionException(String message, Throwable cause) {
        super(message, "INSUFFICIENT_PERMISSION", cause);
    }
}
