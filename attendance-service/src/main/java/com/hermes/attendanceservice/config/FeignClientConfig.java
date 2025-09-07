package com.hermes.attendanceservice.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Configuration
public class FeignClientConfig {
    
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return requestTemplate -> {
            try {
                // 현재 요청의 Authorization 헤더를 그대로 전달
                String authHeader = getCurrentRequestAuthHeader();
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                    log.debug("Feign Client에 Authorization 헤더 추가: {}", 
                             authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
                } else {
                    log.warn("현재 요청에서 Authorization 헤더를 찾을 수 없음");
                }
            } catch (Exception e) {
                log.error("Feign Client Authorization 헤더 추가 실패: {}", e.getMessage(), e);
            }
        };
    }
    
    private String getCurrentRequestAuthHeader() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("Authorization");
            }
        } catch (Exception e) {
            log.warn("Authorization 헤더 추출 실패: {}", e.getMessage());
        }
        return null;
    }
} 