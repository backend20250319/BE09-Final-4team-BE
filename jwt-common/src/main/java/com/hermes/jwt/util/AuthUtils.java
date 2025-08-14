package com.hermes.jwt.util;

import org.springframework.util.StringUtils;

/**
 * 인증 관련 유틸리티 클래스
 */
public class AuthUtils {
    
    /**
     * 사용자 권한이 ADMIN인지 확인
     */
    public static boolean isAdmin(String role) {
        return "ADMIN".equals(role);
    }
    
    /**
     * 사용자 권한이 MANAGER인지 확인
     */
    public static boolean isManager(String role) {
        return "MANAGER".equals(role);
    }
    
    /**
     * 사용자 권한이 EMPLOYEE인지 확인
     */
    public static boolean isEmployee(String role) {
        return "EMPLOYEE".equals(role);
    }
    
    /**
     * 사용자가 필요한 권한을 가지고 있는지 확인
     */
    public static boolean hasPermission(String userRole, String requiredRole) {
        if (!StringUtils.hasText(userRole) || !StringUtils.hasText(requiredRole)) {
            return false;
        }
        
        // ADMIN은 모든 권한을 가짐
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        
        // MANAGER는 MANAGER와 EMPLOYEE 권한을 가짐
        if ("MANAGER".equals(userRole)) {
            return "MANAGER".equals(requiredRole) || "EMPLOYEE".equals(requiredRole);
        }
        
        // EMPLOYEE는 EMPLOYEE 권한만 가짐
        return "EMPLOYEE".equals(userRole) && "EMPLOYEE".equals(requiredRole);
    }
    
    /**
     * 사용자가 ADMIN 또는 MANAGER 권한을 가지고 있는지 확인
     */
    public static boolean isAdminOrManager(String role) {
        return isAdmin(role) || isManager(role);
    }
    
    /**
     * 권한 레벨을 숫자로 변환 (높을수록 권한이 높음)
     */
    public static int getRoleLevel(String role) {
        switch (role) {
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
    
    /**
     * 사용자 권한이 필요한 권한 레벨 이상인지 확인
     */
    public static boolean hasRoleLevel(String userRole, String requiredRole) {
        return getRoleLevel(userRole) >= getRoleLevel(requiredRole);
    }
}
