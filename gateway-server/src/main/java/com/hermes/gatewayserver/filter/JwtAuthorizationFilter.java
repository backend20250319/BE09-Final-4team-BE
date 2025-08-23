package com.hermes.gatewayserver.filter;

import com.hermes.jwt.JwtTokenProvider;
import com.hermes.jwt.context.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
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
        String path = request.getURI().getPath();

        log.info(" [Gateway] 요청 경로: {}", path);

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info(" [Gateway] Authorization 헤더: {}", authHeader);

        log.info(" [Gateway] 화이트리스트 확인 중: path={}", path);
        log.info(" [Gateway] 현재 화이트리스트: {}", filterProperties.getWhitelist());

        if (isWhiteListed(path)) {
            log.info(" [Gateway] 화이트리스트 경로 → JWT 검증 후 통과");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info(" [Gateway] JWT 토큰: {}", token.substring(0, Math.min(20, token.length())) + "...");

                return performJwtValidation(token, request, exchange, chain);
            } else {
                log.info(" [Gateway] 화이트리스트 경로 → Authorization 헤더 없음, 그냥 통과");
                return chain.filter(exchange);
            }
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(" [Gateway] Authorization 헤더가 없거나 형식이 잘못됨: {}", authHeader);
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);
        log.info(" [Gateway] JWT 토큰: {}", token.substring(0, Math.min(20, token.length())) + "...");

        return performJwtValidation(token, request, exchange, chain);
    }

    private Mono<Void> performJwtValidation(String token, ServerHttpRequest request,
                                            ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info(" [Gateway] JWT 검증 시작 (로컬 검증)");
        log.info(" [Gateway] 토큰 길이: {} 문자", token.length());
        log.info(" [Gateway] 토큰 시작 부분: {}", token.substring(0, Math.min(50, token.length())) + "...");

        try {
            // JWT 토큰에서 사용자 정보 추출 (유효성 검증 포함)
            UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
            
            if (userInfo.getEmail() == null || userInfo.getUserId() == null) {
                log.warn(" [Gateway] JWT에서 필수 사용자 정보가 누락됨");
                return unauthorized(exchange);
            }

            log.info(" [Gateway] JWT 검증 성공 → userId={}, email={}", userInfo.getUserId(), userInfo.getEmail());

            // JWT 검증 성공 - 토큰 그대로 전달 (헤더 주입 제거)
            return chain.filter(exchange);

        } catch (Exception e) {
            log.error(" [Gateway] JWT 검증 중 예외 발생: {}", e.getMessage(), e);

            if (isWhiteListed(request.getURI().getPath())) {
                log.info(" [Gateway] 화이트리스트 경로 → JWT 검증 실패해도 통과");
                return chain.filter(exchange);
            }

            return unauthorized(exchange);
        }
    }

    private boolean isWhiteListed(String path) {
        List<String> whitelist = filterProperties.getWhitelist();
        log.info(" [Gateway] isWhiteListed 호출: path={}, whitelist={}", path, whitelist);

        if (whitelist == null) {
            log.warn(" [Gateway] 화이트리스트가 null입니다!");
            return false;
        }

        boolean isWhitelisted = whitelist.stream().anyMatch(path::startsWith);
        log.info(" [Gateway] 화이트리스트 매칭 결과: {} → {}", path, isWhitelisted);

        return isWhitelisted;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}