package com.hermes.multitenancy.config;

import com.hermes.multitenancy.interceptor.TenantContextInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 멀티테넌시 Web MVC 설정
 * TenantContextInterceptor 등록 및 경로 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hermes.multitenancy.enabled", havingValue = "true", matchIfMissing = true)
public class MultiTenancyWebConfig implements WebMvcConfigurer {

    private final TenantContextInterceptor tenantContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantContextInterceptor)
                .addPathPatterns("/**");
    }
}