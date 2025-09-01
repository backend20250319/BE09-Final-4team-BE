package com.hermes.communicationservice.notification.exception;

public class InvalidNotificationRequestException extends RuntimeException {

    public InvalidNotificationRequestException(String message) {
        super(message);
    }

    public InvalidNotificationRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}