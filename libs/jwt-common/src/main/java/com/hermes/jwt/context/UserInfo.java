package com.hermes.jwt.context;

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
    private String role;
    private String tenantId;
    
    /**
     * 현재 사용자가 관리자인지 확인
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    /**
     * 현재 사용자가 매니저인지 확인
     */
    public boolean isManager() {
        return "MANAGER".equals(role);
    }
    
    /**
     * 현재 사용자가 직원인지 확인
     */
    public boolean isEmployee() {
        return "EMPLOYEE".equals(role);
    }
    
    /**
     * 특정 권한을 가지고 있는지 확인
     */
    public boolean hasPermission(String requiredRole) {
        if (role == null || requiredRole == null) {
            return false;
        }
        
        // ADMIN은 모든 권한을 가짐
        if (isAdmin()) {
            return true;
        }
        
        // MANAGER는 MANAGER와 EMPLOYEE 권한을 가짐
        if (isManager()) {
            return "MANAGER".equals(requiredRole) || "EMPLOYEE".equals(requiredRole);
        }
        
        // EMPLOYEE는 EMPLOYEE 권한만 가짐
        return isEmployee() && "EMPLOYEE".equals(requiredRole);
    }
    
    /**
     * 권한 레벨을 숫자로 반환 (높을수록 권한이 높음)
     */
    public int getRoleLevel() {
        switch (role != null ? role : "") {
            case "ADMIN":
                return 3;
            case "MANAGER":
                return 2;
            case "EMPLOYEE":
                return 1;
            default:
                return 0;
        }
    }
}