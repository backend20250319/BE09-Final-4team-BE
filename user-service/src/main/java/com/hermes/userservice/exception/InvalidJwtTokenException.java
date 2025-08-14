package com.hermes.userservice.exception;

public class InvalidJwtTokenException extends RuntimeException {
    
    public InvalidJwtTokenException(String message) {
        super(message);
    }
    
    public InvalidJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
