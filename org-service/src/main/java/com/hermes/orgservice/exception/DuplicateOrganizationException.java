package com.hermes.orgservice.exception;

public class DuplicateOrganizationException extends RuntimeException {
    
    public DuplicateOrganizationException(String message) {
        super(message);
    }
}
