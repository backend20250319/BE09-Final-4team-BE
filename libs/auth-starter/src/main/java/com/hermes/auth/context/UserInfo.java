package com.hermes.auth.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 현재 인증된 사용자의 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {
    
    private Long userId;
    private String email;
    private Role role;
    private String tenantId;
    
    /**
     * 현재 사용자가 관리자인지 확인
     */
    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }
    
    /**
     * 현재 사용자가 일반 사용자인지 확인
     */
    public boolean isUser() {
        return role != null && role.isUser();
    }
    
    /**
     * 특정 권한을 가지고 있는지 확인
     */
    public boolean hasPermission(Role requiredRole) {
        return role != null && role.hasPermission(requiredRole);
    }
    
    /**
     * 문자열 권한과 비교 (하위 호환성을 위해 유지)
     */
    public boolean hasPermission(String requiredRoleString) {
        Role requiredRole = Role.fromString(requiredRoleString);
        return hasPermission(requiredRole);
    }
    
    
    /**
     * 권한을 문자열로 반환 (JWT 토큰 등에서 사용)
     */
    public String getRoleString() {
        return role != null ? role.name() : null;
    }
}