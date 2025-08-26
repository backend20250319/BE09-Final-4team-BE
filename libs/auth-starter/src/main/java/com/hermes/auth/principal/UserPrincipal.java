package com.hermes.auth.principal;

import com.hermes.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails를 구현하는 사용자 주체(Principal) 클래스
 * JWT 토큰에서 추출한 사용자 정보를 Spring Security에서 사용할 수 있도록 래핑합니다.
 */
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserPrincipal implements UserDetails {
    
    @EqualsAndHashCode.Include
    private Long userId;
    
    @EqualsAndHashCode.Include
    private String email;
    
    private Role role;
    private String tenantId;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getPassword() {
        // JWT 기반 인증에서는 패스워드가 필요 없음
        return null;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // 편의 메서드들
    
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
     * 권한을 문자열로 반환
     */
    public String getRoleString() {
        return role != null ? role.name() : null;
    }
}