package com.hermes.communicationservice.notification.exception;

public class NotificationAccessDeniedException extends RuntimeException {

    public NotificationAccessDeniedException(String message) {
        super(message);
    }

    public NotificationAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}