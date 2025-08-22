package com.hermes.approvalservice.exception;

public class NotFoundException extends BusinessException {
    
    public NotFoundException(String message) {
        super(message);
    }
}