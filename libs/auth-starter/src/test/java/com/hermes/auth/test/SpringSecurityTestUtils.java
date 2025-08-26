package com.hermes.auth.test;

import com.hermes.auth.enums.Role;
import com.hermes.auth.principal.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Spring Security 테스트 환경에서 인증 정보를 설정하는 유틸리티 클래스
 * 
 * 기존 AuthContext 기반에서 Spring Security 기반으로 마이그레이션
 */
public class SpringSecurityTestUtils {
    
    /**
     * 테스트용 사용자 정보를 SecurityContext에 설정합니다.
     * 
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param role 사용자 역할 (ADMIN, USER)
     * @param tenantId 테넌트 ID
     */
    public static void setAuthenticatedUser(Long userId, String email, String role, String tenantId) {
        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId)
                .email(email)
                .role(Role.fromString(role, Role.USER))
                .tenantId(tenantId)
                .build();
        
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, authorities
        );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
    
    /**
     * 테스트용 사용자 정보를 설정합니다. (기본 테넌트 ID 사용)
     * 
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param role 사용자 역할 (ADMIN, USER)
     */
    public static void setAuthenticatedUser(Long userId, String email, String role) {
        setAuthenticatedUser(userId, email, role, "test-tenant");
    }
    
    /**
     * JWT 토큰을 사용한 인증 정보를 설정합니다.
     * 
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param role 사용자 역할
     * @param tenantId 테넌트 ID
     */
    public static void setJwtAuthenticatedUser(Long userId, String email, String role, String tenantId) {
        Map<String, Object> claims = Map.of(
                "sub", email,
                "userId", userId,
                "role", role,
                "tenantId", tenantId
        );
        
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .header("typ", "JWT")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        
        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId)
                .email(email)
                .role(Role.fromString(role, Role.USER))
                .tenantId(tenantId)
                .build();
        
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
        
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, authorities, email
        );
        authentication.setDetails(principal);
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
    
    /**
     * 관리자 사용자로 설정합니다.
     * 
     * @param userId 사용자 ID
     */
    public static void setAdminUser(Long userId) {
        setAuthenticatedUser(userId, "admin@test.com", "ADMIN");
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
     * @param userId 사용자 ID
     */
    public static void setUserUser(Long userId) {
        setAuthenticatedUser(userId, "user@test.com", "USER");
    }
    
    /**
     * 일반 사용자로 설정합니다. (기본 ID: 2L)
     */
    public static void setUserUser() {
        setUserUser(2L);
    }
    
    /**
     * 인증되지 않은 상태로 설정합니다. (SecurityContext 비움)
     */
    public static void setUnauthenticated() {
        SecurityContextHolder.clearContext();
    }
    
    /**
     * 테스트 완료 후 SecurityContext를 정리합니다.
     * 각 테스트 메서드의 @AfterEach에서 호출하는 것을 권장합니다.
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
    
    /**
     * 현재 설정된 테스트 사용자의 ID를 반환합니다.
     * 
     * @return 현재 사용자 ID, 인증되지 않은 경우 null
     */
    public static Long getCurrentTestUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUserId();
        }
        
        return null;
    }
    
    /**
     * 현재 설정된 테스트 사용자가 관리자인지 확인합니다.
     * 
     * @return 관리자인 경우 true, 그렇지 않으면 false
     */
    public static boolean isCurrentTestUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
    
    /**
     * 현재 설정된 테스트 사용자의 UserPrincipal을 반환합니다.
     * 
     * @return UserPrincipal 객체, 인증되지 않은 경우 null
     */
    public static UserPrincipal getCurrentTestUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        // UsernamePasswordAuthenticationToken의 경우 principal에 저장됨
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }
        
        // JwtAuthenticationToken의 경우 details에 저장됨
        if (auth instanceof JwtAuthenticationToken) {
            Object details = auth.getDetails();
            if (details instanceof UserPrincipal) {
                return (UserPrincipal) details;
            }
        }
        
        return null;
    }
}