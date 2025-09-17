package com.hermes.multitenancy.exception;

/**
 * 테넌트 정보를 찾을 수 없을 때 발생하는 예외
 */
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException() {
        super("Tenant information not found");
    }

    public TenantNotFoundException(String message) {
        super(message);
    }

    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TenantNotFoundException(Throwable cause) {
        super(cause);
    }
}