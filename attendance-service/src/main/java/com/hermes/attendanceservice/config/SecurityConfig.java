package com.hermes.attendanceservice.config;

import com.hermes.auth.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Attendance Service의 보안 설정
 * BaseSecurityConfig를 상속받아 attendance-service 특화 권한 설정만 추가
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    @Override
    protected void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        // BaseSecurityConfig에서 이미 /v3/api-docs/**와 /swagger-ui/**를 허용하므로
        // attendance-service API들은 인증된 사용자만 접근 가능
        auth.requestMatchers("/api/**").authenticated();       // attendance-service API들
    }
}
