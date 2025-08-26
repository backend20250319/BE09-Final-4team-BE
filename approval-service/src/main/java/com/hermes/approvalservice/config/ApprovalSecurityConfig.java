package com.hermes.approvalservice.config;

import com.hermes.auth.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Approval Service의 Spring Security 설정
 * 템플릿 관리는 관리자만, 문서 관리는 인증된 사용자로 설정합니다.
 */
@Configuration
public class ApprovalSecurityConfig extends BaseSecurityConfig {
    
    @Override
    protected void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    ) {
        authz.requestMatchers("/api/approval/templates/**").hasRole("ADMIN");
        authz.requestMatchers("/api/approval/documents/**").authenticated();
    }
}