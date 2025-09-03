package com.hermes.auth.test;

import com.hermes.auth.enums.Role;
import com.hermes.auth.principal.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @WithMockJwtUser 어노테이션을 위한 SecurityContext 팩토리
 */
public class WithMockJwtUserSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser annotation) {
        // UserPrincipal 생성
        UserPrincipal principal = UserPrincipal.builder()
                .id(annotation.userId())
                .role(Role.fromString(annotation.role(), Role.USER))
                .tenantId(annotation.tenantId())
                .build();
        
        // JWT 토큰 생성
        Map<String, Object> claims = Map.of(
                "id", annotation.userId(),
                "role", annotation.role(),
                "tenantId", annotation.tenantId()
        );
        
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .header("typ", "JWT")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        
        // 권한 설정
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + annotation.role())
        );
        
        // JwtAuthenticationToken 생성
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, authorities, String.valueOf(annotation.userId())
        );
        authentication.setDetails(principal);
        
        // SecurityContext 생성 및 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        
        return context;
    }
}