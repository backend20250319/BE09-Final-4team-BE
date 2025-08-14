package com.hermes.gatewayserver.filter;

import com.hermes.gatewayserver.dto.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter implements GlobalFilter, Ordered {

    private final WebClient.Builder webClientBuilder;
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
            log.info(" [Gateway] 화이트리스트 경로 → JWT 검증 후 헤더 주입");

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
        log.info(" [Gateway] JWT 검증 요청 시작 → /token/validate");
        log.info(" [Gateway] 토큰 길이: {} 문자", token.length());
        log.info(" [Gateway] 토큰 시작 부분: {}", token.substring(0, Math.min(50, token.length())) + "...");

        return webClientBuilder.build()
                .post()
                .uri("http://user-service/api/token/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.isError(),
                        res -> {
                            log.error(" [Gateway] JWT 검증 실패: {} - {}", res.statusCode(), res.statusCode().value());
                            return res.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error(" [Gateway] 에러 응답 본문: {}", body);
                                        return Mono.error(new RuntimeException("JWT 검증 실패: " + res.statusCode() + " - " + body));
                                    })
                                    .defaultIfEmpty("응답 본문 없음")
                                    .flatMap(body -> Mono.error(new RuntimeException("JWT 검증 실패: " + res.statusCode() + " - " + body)));
                        })
                .bodyToMono(ApiResponse.class)
                .flatMap(apiResponse -> {
                    log.info(" [Gateway] ApiResponse 받음: success={}, message={}", apiResponse.isSuccess(), apiResponse.getMessage());
                    log.info(" [Gateway] ApiResponse 데이터 타입: {}", apiResponse.getData() != null ? apiResponse.getData().getClass().getSimpleName() : "null");

                    if (!apiResponse.isSuccess()) {
                        log.error(" [Gateway] ApiResponse 실패: {}", apiResponse.getMessage());
                        return Mono.error(new RuntimeException("JWT 검증 실패: " + apiResponse.getMessage()));
                    }

                    if (apiResponse.getData() == null) {
                        log.error(" [Gateway] ApiResponse 데이터가 null입니다");
                        return Mono.error(new RuntimeException("JWT 검증 실패: 응답 데이터가 null입니다"));
                    }

                    try {
                        TokenValidationResponse response;
                        
                        if (apiResponse.getData() instanceof TokenValidationResponse) {
                            response = (TokenValidationResponse) apiResponse.getData();
                        } else if (apiResponse.getData() instanceof java.util.Map) {
                            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) apiResponse.getData();
                            String email = (String) dataMap.get("email");
                            String userId = String.valueOf(dataMap.get("userId"));
                            response = new TokenValidationResponse(email, userId);
                        } else {
                            throw new RuntimeException("지원하지 않는 데이터 타입: " + apiResponse.getData().getClass());
                        }
                        
                        log.info(" [Gateway] JWT 검증 성공 → userId={}, email={}", response.getUserId(), response.getEmail());

                        return checkBlacklist(token, request, exchange, chain, response);
                        
                    } catch (ClassCastException e) {
                        log.error(" [Gateway] 데이터 타입 변환 실패: {}", e.getMessage());
                        log.error(" [Gateway] 실제 데이터: {}", apiResponse.getData());
                        return Mono.error(new RuntimeException("JWT 검증 실패: 데이터 타입 변환 실패"));
                    }
                })
                .onErrorResume(e -> {
                    log.error(" [Gateway] JWT 검증 중 예외 발생: {}", e.getMessage(), e);

                    if (isWhiteListed(request.getURI().getPath())) {
                        log.info(" [Gateway] 화이트리스트 경로 → JWT 검증 실패해도 통과");
                        return chain.filter(exchange);
                    }

                    return unauthorized(exchange);
                });
    }

    private Mono<Void> checkBlacklist(String token, ServerHttpRequest request, 
                                     ServerWebExchange exchange, GatewayFilterChain chain, 
                                     TokenValidationResponse response) {
        log.info(" [Gateway] 블랙리스트 검증 요청 시작 → /token/check-blacklist");

        return webClientBuilder.build()
                .post()
                .uri("http://user-service/api/token/check-blacklist")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.isError(),
                        res -> {
                            log.error(" [Gateway] 블랙리스트 검증 실패: {} - {}", res.statusCode(), res.statusCode().value());
                            return res.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error(" [Gateway] 블랙리스트 검증 에러 응답 본문: {}", body);
                                        return Mono.error(new RuntimeException("블랙리스트 검증 실패: " + res.statusCode() + " - " + body));
                                    })
                                    .defaultIfEmpty("응답 본문 없음")
                                    .flatMap(body -> Mono.error(new RuntimeException("블랙리스트 검증 실패: " + res.statusCode() + " - " + body)));
                        })
                .bodyToMono(ApiResponse.class)
                .flatMap(apiResponse -> {
                    log.info(" [Gateway] 블랙리스트 검증 응답: success={}, message={}", apiResponse.isSuccess(), apiResponse.getMessage());

                    if (!apiResponse.isSuccess()) {
                        log.error(" [Gateway] 블랙리스트 검증 ApiResponse 실패: {}", apiResponse.getMessage());
                        return Mono.error(new RuntimeException("블랙리스트 검증 실패: " + apiResponse.getMessage()));
                    }

                    if (apiResponse.getData() == null) {
                        log.error(" [Gateway] 블랙리스트 검증 응답 데이터가 null입니다");
                        return Mono.error(new RuntimeException("블랙리스트 검증 실패: 응답 데이터가 null입니다"));
                    }

                    try {
                        java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) apiResponse.getData();
                        Boolean isBlacklisted = (Boolean) dataMap.get("isBlacklisted");
                        
                        if (isBlacklisted != null && isBlacklisted) {
                            log.warn(" [Gateway] 블랙리스트된 토큰 사용 시도: userId={}", response.getUserId());
                            return unauthorized(exchange);
                        }
                        
                        log.info(" [Gateway] 블랙리스트 검증 성공 → 토큰 유효: userId={}", response.getUserId());

                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Id", response.getUserId())
                                .header("X-User-Email", response.getEmail())
                                .build();

                        log.info(" [Gateway] 사용자 정보 헤더 주입 완료: X-User-Id={}, X-User-Email={}",
                                response.getUserId(), response.getEmail());

                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(modifiedRequest)
                                .build();

                        return chain.filter(modifiedExchange);
                        
                    } catch (ClassCastException e) {
                        log.error(" [Gateway] 블랙리스트 검증 데이터 타입 변환 실패: {}", e.getMessage());
                        log.error(" [Gateway] 실제 데이터: {}", apiResponse.getData());
                        return Mono.error(new RuntimeException("블랙리스트 검증 실패: 데이터 타입 변환 실패"));
                    }
                })
                .onErrorResume(e -> {
                    log.error(" [Gateway] 블랙리스트 검증 중 예외 발생: {}", e.getMessage(), e);

                    if (isWhiteListed(request.getURI().getPath())) {
                        log.info(" [Gateway] 화이트리스트 경로 → 블랙리스트 검증 실패해도 통과");
                        return chain.filter(exchange);
                    }

                    return unauthorized(exchange);
                });
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenValidationResponse {
        private String email;
        private String userId;
    }
}