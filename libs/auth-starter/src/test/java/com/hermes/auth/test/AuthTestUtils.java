package com.hermes.auth.test;

import com.hermes.auth.context.AuthContext;
import com.hermes.auth.context.UserInfo;
import com.hermes.auth.context.Role;

/**
 * 테스트 환경에서 AuthContext를 쉽게 설정할 수 있는 유틸리티 클래스
 */
public class AuthTestUtils {
    
    /**
     * 테스트용 사용자 정보를 설정합니다.
     * 
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param role 사용자 역할 (ADMIN, USER)
     */
    public static void setCurrentUser(Long userId, String email, String role) {
        Role userRole = Role.fromString(role, Role.USER);
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .email(email)
                .role(userRole)
                .tenantId("test-tenant")
                .build();
        
        AuthContext.setCurrentUser(userInfo);
    }
    
    /**
     * 테스트용 사용자 정보를 설정합니다. (테넌트 ID 포함)
     * 
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param role 사용자 역할
     * @param tenantId 테넌트 ID
     */
    public static void setCurrentUser(Long userId, String email, String role, String tenantId) {
        Role userRole = Role.fromString(role, Role.USER);
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .email(email)
                .role(userRole)
                .tenantId(tenantId)
                .build();
        
        AuthContext.setCurrentUser(userInfo);
    }
    
    /**
     * 테스트용 사용자 정보를 설정합니다.
     * 
     * @param userInfo 사용자 정보 객체
     */
    public static void setCurrentUser(UserInfo userInfo) {
        AuthContext.setCurrentUser(userInfo);
    }
    
    /**
     * 관리자 사용자로 설정합니다.
     * 
     * @param userId 사용자 ID (기본값: 1L)
     */
    public static void setAdminUser(Long userId) {
        setCurrentUser(userId, "admin@test.com", "ADMIN");
    }
    
    /**
     * 관리자 사용자로 설정합니다. (기본 ID: 1L)
     */
    public static void setAdminUser() {
        setAdminUser(1L);
    }
    
    /**
     * 일반 사용자로 설정합니다.
     * 
     * @param userId 사용자 ID (기본값: 2L)
     */
    public static void setUserUser(Long userId) {
        setCurrentUser(userId, "user@test.com", "USER");
    }
    
    /**
     * 일반 사용자로 설정합니다. (기본 ID: 2L)
     */
    public static void setUserUser() {
        setUserUser(2L);
    }
    
    /**
     * 인증되지 않은 상태로 설정합니다. (AuthContext 비움)
     */
    public static void setUnauthenticated() {
        AuthContext.clear();
    }
    
    /**
     * 테스트 완료 후 AuthContext를 정리합니다.
     * 각 테스트 메서드의 @AfterEach에서 호출하는 것을 권장합니다.
     */
    public static void clearAuthContext() {
        AuthContext.clear();
    }
    
    /**
     * 현재 설정된 테스트 사용자의 ID를 반환합니다.
     * 
     * @return 현재 사용자 ID, 인증되지 않은 경우 null
     */
    public static Long getCurrentTestUserId() {
        return AuthContext.getCurrentUserId();
    }
    
    /**
     * 현재 설정된 테스트 사용자가 관리자인지 확인합니다.
     * 
     * @return 관리자인 경우 true, 그렇지 않으면 false
     */
    public static boolean isCurrentTestUserAdmin() {
        return AuthContext.isCurrentUserAdmin();
    }
}