package com.hermes.orgservice.exception;

public class OrganizationNotFoundException extends RuntimeException {
    
    public OrganizationNotFoundException(String message) {
        super(message);
    }
    
    public OrganizationNotFoundException(Long organizationId) {
        super("Organization not found with ID: " + organizationId);
    }
}
