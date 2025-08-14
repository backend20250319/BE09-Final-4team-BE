package com.hermes.gatewayserver.constant;

public class ErrorCode {

    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String DUPLICATE_EMAIL = "DUPLICATE_EMAIL";
    public static final String INSUFFICIENT_PERMISSION = "INSUFFICIENT_PERMISSION";

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BIND_ERROR = "BIND_ERROR";
    public static final String ILLEGAL_ARGUMENT = "ILLEGAL_ARGUMENT";

    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";

    public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    
    private ErrorCode() {
    }
}
