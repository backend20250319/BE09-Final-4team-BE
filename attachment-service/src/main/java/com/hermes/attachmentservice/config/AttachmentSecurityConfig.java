package com.hermes.attachmentservice.config;

import com.hermes.auth.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 활성화
public class AttachmentSecurityConfig extends BaseSecurityConfig {
    
    @Override
    protected void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    ) {
        authz
            // 내부 API - 인증된 사용자만
            .requestMatchers("/internal/**").authenticated()
            // 공개 API - 인증된 사용자만  
            .requestMatchers("/api/**").authenticated();
    }
}