package com.hermes.gatewayserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway Server의 Spring WebFlux Security 설정
 * API Gateway 역할을 하므로 기본적인 보안 설정만 적용합니다.
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // CORS preflight 요청 허용 (WebSocket SockJS 지원)
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // Actuator 헬스체크는 공개
                .pathMatchers("/actuator/**").permitAll()
                // Swagger UI는 공개 (개발 환경)
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 나머지는 각 마이크로서비스에서 인증 처리
                .anyExchange().permitAll()
            )
            .build();
    }
}