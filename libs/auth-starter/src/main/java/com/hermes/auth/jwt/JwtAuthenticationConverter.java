package com.hermes.auth.jwt;

import com.hermes.auth.enums.Role;
import com.hermes.auth.principal.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * JWT 토큰을 Spring Security의 Authentication 객체로 변환하는 컨버터
 * JWT의 클레임에서 사용자 정보를 추출하여 UserPrincipal을 생성합니다.
 */
@Slf4j
@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        try {
            // JWT 클레임에서 사용자 정보 추출
            String email = jwt.getSubject();
            Object userIdObj = jwt.getClaim("userId");
            Object roleObj = jwt.getClaim("role");
            Object tenantIdObj = jwt.getClaim("tenantId");
            
            // userId 파싱
            Long userId = null;
            if (userIdObj != null) {
                try {
                    userId = Long.parseLong(userIdObj.toString());
                } catch (NumberFormatException e) {
                    log.warn("Invalid userId format in JWT token: {}", userIdObj);
                    throw new IllegalArgumentException("Invalid userId format in JWT token: " + userIdObj);
                }
            }
            
            // Role 파싱
            Role role = Role.fromString(roleObj != null ? roleObj.toString() : null, Role.USER);
            
            // TenantId 추출
            String tenantId = tenantIdObj != null ? tenantIdObj.toString() : null;
            
            // UserPrincipal 생성
            UserPrincipal principal = new UserPrincipal(userId, email, role, tenantId);
            
            // GrantedAuthority 생성
            Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role.name())
            );
            
            log.debug("JWT converted to authentication: userId={}, email={}, role={}, tenantId={}", 
                     userId, email, role, tenantId);

            return new AbstractAuthenticationToken(authorities) {
                @Override
                public Object getCredentials() {
                    return null;
                }
                
                @Override
                public Object getPrincipal() {
                    return principal;
                }
                
                @Override
                public boolean isAuthenticated() {
                    return true;
                }
            };
            
        } catch (Exception e) {
            log.error("Failed to convert JWT to authentication: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to convert JWT to authentication", e);
        }
    }
}