package com.hermes.orgservice.exception;

public class EmployeeAssignmentNotFoundException extends RuntimeException {
    
    public EmployeeAssignmentNotFoundException(String message) {
        super(message);
    }
    
    public EmployeeAssignmentNotFoundException(Long assignmentId) {
        super("Employee assignment not found with ID: " + assignmentId);
    }
}
