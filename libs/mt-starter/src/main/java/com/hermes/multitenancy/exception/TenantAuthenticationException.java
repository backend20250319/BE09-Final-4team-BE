package com.hermes.multitenancy.exception;

/**
 * 테넌트 인증 실패 시 발생하는 예외
 */
public class TenantAuthenticationException extends RuntimeException {

    public TenantAuthenticationException() {
        super("Tenant authentication failed");
    }

    public TenantAuthenticationException(String message) {
        super(message);
    }

    public TenantAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TenantAuthenticationException(Throwable cause) {
        super(cause);
    }
}