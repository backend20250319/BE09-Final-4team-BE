package com.hermes.auth.context;

import lombok.extern.slf4j.Slf4j;

/**
 * ThreadLocal 기반 인증 컨텍스트
 * 현재 스레드의 사용자 정보를 저장하고 관리합니다.
 */
@Slf4j
public class AuthContext {
    
    private static final ThreadLocal<UserInfo> USER_CONTEXT = new ThreadLocal<>();
    
    /**
     * 현재 스레드에 사용자 정보를 설정합니다.
     * 
     * @param userInfo 설정할 사용자 정보
     */
    public static void setCurrentUser(UserInfo userInfo) {
        if (userInfo != null) {
            log.debug("Setting current user: userId={}, email={}, role={}", 
                     userInfo.getUserId(), userInfo.getEmail(), userInfo.getRole());
        }
        USER_CONTEXT.set(userInfo);
    }
    
    /**
     * 현재 스레드의 사용자 정보를 반환합니다.
     * 
     * @return 현재 사용자 정보, 인증되지 않은 경우 null
     */
    public static UserInfo getCurrentUser() {
        return USER_CONTEXT.get();
    }
    
    /**
     * 현재 스레드의 사용자 ID를 반환합니다.
     *
     * @return 현재 사용자 ID, 인증되지 않은 경우 null
     */
    public static Long getCurrentUserId() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getUserId() : null;
    }
    
    /**
     * 현재 스레드의 사용자 이메일을 반환합니다.
     * 
     * @return 현재 사용자 이메일, 인증되지 않은 경우 null
     */
    public static String getCurrentUserEmail() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getEmail() : null;
    }
    
    /**
     * 현재 스레드의 사용자 역할을 반환합니다.
     * 
     * @return 현재 사용자 역할, 인증되지 않은 경우 null
     */
    public static Role getCurrentUserRole() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getRole() : null;
    }
    
    /**
     * 현재 스레드의 테넌트 ID를 반환합니다.
     * 
     * @return 현재 테넌트 ID, 인증되지 않은 경우 null
     */
    public static String getCurrentTenantId() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null ? userInfo.getTenantId() : null;
    }
    
    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     * 
     * @return 인증된 경우 true, 그렇지 않으면 false
     */
    public static boolean isAuthenticated() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null && userInfo.getUserId() != null;
    }
    
    /**
     * 현재 사용자가 관리자인지 확인합니다.
     * 
     * @return 관리자인 경우 true, 그렇지 않으면 false
     */
    public static boolean isCurrentUserAdmin() {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null && userInfo.isAdmin();
    }
    
    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인합니다.
     * 
     * @param requiredRole 필요한 권한
     * @return 권한이 있는 경우 true, 그렇지 않으면 false
     */
    public static boolean hasPermission(String requiredRole) {
        UserInfo userInfo = getCurrentUser();
        return userInfo != null && userInfo.hasPermission(requiredRole);
    }
    
    /**
     * 현재 스레드의 사용자 정보를 제거합니다.
     * 메모리 누수를 방지하기 위해 요청 처리 완료 후 반드시 호출해야 합니다.
     */
    public static void clear() {
        UserInfo userInfo = USER_CONTEXT.get();
        if (userInfo != null) {
            log.debug("Clearing current user: userId={}, email={}", 
                     userInfo.getUserId(), userInfo.getEmail());
        }
        USER_CONTEXT.remove();
    }
    
    /**
     * 인증이 필요한 작업을 수행합니다.
     * 
     * @throws AuthenticationRequiredException 인증되지 않은 경우
     */
    public static void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new AuthenticationRequiredException("인증이 필요합니다.");
        }
    }
    
    /**
     * 관리자 권한이 필요한 작업을 수행합니다.
     * 
     * @throws AuthenticationRequiredException 인증되지 않은 경우
     * @throws InsufficientPermissionException 관리자 권한이 없는 경우
     */
    public static void requireAdmin() {
        requireAuthentication();
        if (!isCurrentUserAdmin()) {
            throw new InsufficientPermissionException("관리자 권한이 필요합니다.");
        }
    }
    
    /**
     * 특정 권한이 필요한 작업을 수행합니다.
     * 
     * @param requiredRole 필요한 권한
     * @throws AuthenticationRequiredException 인증되지 않은 경우
     * @throws InsufficientPermissionException 필요한 권한이 없는 경우
     */
    public static void requirePermission(String requiredRole) {
        requireAuthentication();
        if (!hasPermission(requiredRole)) {
            throw new InsufficientPermissionException("필요한 권한이 없습니다: " + requiredRole);
        }
    }
    
    /**
     * 인증 예외 클래스
     */
    public static class AuthenticationRequiredException extends RuntimeException {
        public AuthenticationRequiredException(String message) {
            super(message);
        }
    }
    
    /**
     * 권한 부족 예외 클래스
     */
    public static class InsufficientPermissionException extends RuntimeException {
        public InsufficientPermissionException(String message) {
            super(message);
        }
    }
}