package com.hermes.communicationservice.config;

import com.hermes.auth.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Communication Service의 API용 Spring Security 설정
 * 
 * 주요 특징:
 * - BaseSecurityConfig 상속으로 표준 JWT 인증 적용
 * - WebSocket 경로는 WebSocketSecurityConfig에서 별도 처리
 * - API 경로는 표준 보안 정책 적용
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize 활성화
public class CommunicationSecurityConfig extends BaseSecurityConfig {

    @Override
    protected void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    ) {
        // WebSocket 경로 허용 (HTTP 핸드셰이크 단계)
        authz.requestMatchers("/ws/**", "/ws-pure/**").permitAll();
        // WebSocket 문서 API 허용
        authz.requestMatchers("/api/websocket-docs/**").permitAll();
    }

    @Override
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults()) // CORS 허용 추가
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // 기본 공개 경로
                auth.requestMatchers("/actuator/health", "/actuator/info").permitAll();
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
                
                // 서비스별 커스텀 경로
                configureAuthorization(auth);
                
                // 기본: 나머지는 인증 필요
                auth.anyRequest().authenticated();
            })
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }
}