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
    }
}
