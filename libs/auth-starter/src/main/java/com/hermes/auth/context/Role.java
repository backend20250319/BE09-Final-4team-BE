package com.hermes.auth.context;

import lombok.Getter;

/**
 * 사용자 권한을 나타내는 열거형
 * Hermes 시스템에서 사용하는 권한 체계를 정의합니다.
 */
@Getter
public enum Role {
    
    /**
     * 일반 사용자 권한
     * 기본적인 기능에 대한 접근 권한을 가집니다.
     */
    USER("일반사용자"),
    
    /**
     * 관리자 권한
     * 대부분의 관리 기능에 대한 접근 권한을 가집니다.
     */
    ADMIN("관리자");

    /**
     * -- GETTER --
     *  권한 설명을 반환합니다.
     *
     * @return 권한 설명
     */
    private final String description;
    
    Role(String description) {
        this.description = description;
    }

    /**
     * 현재 권한이 요구되는 권한을 만족하는지 확인합니다.
     * 계층적 권한 구조를 사용하여 높은 권한은 낮은 권한을 포함합니다.
     * 
     * @param requiredRole 요구되는 권한
     * @return 권한을 만족하는 경우 true
     */
    public boolean hasPermission(Role requiredRole) {
        if (requiredRole == null) {
            return false;
        }
        
        // ordinal() 기반 계층 구조: 높은 ordinal 값이 낮은 ordinal 값의 권한을 포함
        return this.ordinal() >= requiredRole.ordinal();
    }
    
    
    /**
     * 관리자 권한 이상인지 확인합니다.
     * ADMIN 이상의 모든 권한(향후 SUPERADMIN 포함)에 대해 true를 반환합니다.
     * 
     * @return 관리자 권한 이상인 경우 true
     */
    public boolean isAdmin() {
        return this.ordinal() >= ADMIN.ordinal();
    }
    
    /**
     * 정확히 일반 사용자 권한인지 확인합니다.
     * 
     * @return 정확히 USER 권한인 경우만 true
     */
    public boolean isUser() {
        return this == USER;
    }
    
    /**
     * 문자열로부터 Role을 생성합니다.
     * 
     * @param roleString 권한 문자열
     * @return Role 객체, 유효하지 않은 경우 null
     */
    public static Role fromString(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return valueOf(roleString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 문자열로부터 Role을 생성합니다. (기본값 포함)
     * 
     * @param roleString 권한 문자열
     * @param defaultRole 유효하지 않은 경우 사용할 기본값
     * @return Role 객체
     */
    public static Role fromString(String roleString, Role defaultRole) {
        Role role = fromString(roleString);
        return role != null ? role : defaultRole;
    }
}