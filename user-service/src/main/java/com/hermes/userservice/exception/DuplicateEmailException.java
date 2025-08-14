package com.hermes.userservice.exception;

import com.hermes.jwt.exception.BusinessException;

public class DuplicateEmailException extends BusinessException {
    
    public DuplicateEmailException(String message) {
        super(message, "DUPLICATE_EMAIL");
    }
    
    public DuplicateEmailException(String message, Throwable cause) {
        super(message, "DUPLICATE_EMAIL", cause);
    }
}
