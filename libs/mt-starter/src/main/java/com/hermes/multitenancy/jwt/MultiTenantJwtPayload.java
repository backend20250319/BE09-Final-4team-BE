package com.hermes.multitenancy.jwt;

import com.hermes.jwt.JwtPayload;
import lombok.Getter;

/**
 * 멀티테넌트를 지원하는 확장된 JWT 페이로드
 */
@Getter
public class MultiTenantJwtPayload extends JwtPayload {
    
    private final String tenantId;

    public MultiTenantJwtPayload(String userId, String email, String role, String tenantId) {
        super(userId, email, role);
        this.tenantId = tenantId;
    }

    public MultiTenantJwtPayload(String email, String tenantId) {
        super(email);
        this.tenantId = tenantId;
    }

    /**
     * 기존 JwtPayload에서 MultiTenantJwtPayload로 변환
     * 테넌트 ID는 별도로 추출해야 함
     */
    public static MultiTenantJwtPayload from(JwtPayload jwtPayload, String tenantId) {
        return new MultiTenantJwtPayload(
            jwtPayload.getUserId(),
            jwtPayload.getEmail(),
            jwtPayload.getRole(),
            tenantId
        );
    }
}
