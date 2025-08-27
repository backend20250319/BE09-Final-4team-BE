package com.hermes.newscrawlerservice.config;

import com.hermes.auth.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * News Crawler Service의 Spring Security 설정
 * 모든 API는 인증된 사용자만 접근 가능합니다.
 */
@Configuration
public class NewsSecurityConfig extends BaseSecurityConfig {
    
    @Override
    protected void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    ) {
        // news-crawler-service는 특별한 공개 API가 없으므로 기본 설정만 사용
        // 모든 /api/news/** 요청은 인증 필요
    }
}