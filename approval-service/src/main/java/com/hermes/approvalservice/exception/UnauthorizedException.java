package com.hermes.approvalservice.exception;

public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
}