package com.hermes.gatewayserver.filter;

import com.hermes.auth.JwtTokenProvider;
import com.hermes.auth.context.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;
    private final FilterProperties filterProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("=== JWT Filter Debug ===");
        log.debug("Request Path: {}", path);
        log.debug("Whitelist: {}", filterProperties.getWhitelist());
        log.debug("Blacklist: {}", filterProperties.getBlacklist());
        log.debug("=========================");

        // 화이트리스트 체크
        if (isWhitelisted(path)) {
            log.debug("Path {} is whitelisted, skipping JWT validation", path);
            return chain.filter(exchange);
        }

        // 블랙리스트 체크 - 로그아웃은 JWT 검증 후 처리
        if (isBlacklisted(path)) {
            log.debug("Path {} is blacklisted, proceeding with JWT validation", path);
            // 블랙리스트 경로도 JWT 검증은 수행하되, 특별 처리
        }

        // JWT 토큰 검증
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("No JWT token found in request to {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
            if (userInfo == null) {
                log.warn("Invalid JWT token for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            log.debug("JWT validation successful for user: {} (ID: {})", userInfo.getEmail(), userInfo.getUserId());
            
            // 블랙리스트 경로는 JWT 검증만 하고 원본 요청 전달
            if (isBlacklisted(path)) {
                log.debug("Blacklisted path {} - JWT validated, proceeding", path);
            }
            
            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("JWT validation failed for path: {}, error: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isWhitelisted(String path) {
        List<String> whitelist = filterProperties.getWhitelist();
        if (whitelist == null) {
            log.warn("화이트리스트가 null입니다!");
            return false;
        }
        return whitelist.stream().anyMatch(path::startsWith);
    }

    private boolean isBlacklisted(String path) {
        List<String> blacklist = filterProperties.getBlacklist();
        if (blacklist == null) {
            return false;
        }
        return blacklist.stream().anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    public int getOrder() {
        return -100; // 높은 우선순위
    }
}