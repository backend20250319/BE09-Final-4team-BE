package com.hermes.userservice.controller;

import com.hermes.jwt.JwtTokenProvider;
import com.hermes.jwt.JwtPayload;
import com.hermes.jwt.dto.TokenValidationResponse;
import com.hermes.userservice.dto.ApiResponse;
import com.hermes.userservice.jwt.dto.RefreshRequest;
import com.hermes.userservice.jwt.dto.TokenRequest;
import com.hermes.userservice.jwt.dto.TokenResponse;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import com.hermes.userservice.service.TokenBlacklistService;

@Slf4j
@RestController
@RequestMapping("api/token")
@RequiredArgsConstructor
@Transactional
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<TokenResponse>> generateToken(@RequestBody TokenRequest request) {
        String accessToken = jwtTokenProvider.createToken(request.getEmail(), request.getUserId(), "USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(request.getEmail());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000);
        
        log.info("토큰 생성: 현재시간={}, 만료시간={}, userId={}", now, expiration, request.getUserId());
        
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(request.getUserId())
                        .token(refreshToken)
                        .expiration(expiration)
                        .build()
        );

        return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 생성되었습니다.", new TokenResponse(accessToken, refreshToken)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody RefreshRequest request) {
    
        log.info("�� [Token Controller] 토큰 갱신 요청 받음");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
        }

        String token = authHeader.substring(7); // "Bearer " 제거
        log.info(" [Token Controller] JWT 토큰: {}", token.substring(0, Math.min(20, token.length())) + "...");
    
        if (!jwtTokenProvider.isValidToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        Long userId = Long.valueOf(jwtTokenProvider.getClaimFromToken(token, "userId"));
        log.info(" [Token Controller] JWT에서 userId 추출: {}", userId);

        RefreshToken saved = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("RefreshToken not found"));
    
        if (!saved.getToken().equals(request.getRefreshToken())) {
            throw new RuntimeException("유효하지 않은 RefreshToken입니다.");
        }

        if (tokenBlacklistService.isRefreshTokenBlacklisted(request.getRefreshToken())) {
            log.warn(" [Token Controller] 블랙리스트된 Refresh Token 사용 시도: userId={}", userId);
            throw new RuntimeException("로그아웃된 Refresh Token입니다.");
        }

        Instant now = Instant.now();
        Instant expiration = saved.getExpiration().atZone(ZoneId.systemDefault()).toInstant();
        
        if (expiration.isBefore(now)) {
            log.warn("RefreshToken 만료: 현재시간={}, 만료시간={}, userId={}", now, expiration, userId);
            throw new RuntimeException("만료된 RefreshToken입니다.");
        }

        String newAccessToken = jwtTokenProvider.createToken(request.getEmail(), userId, "USER");
    
        log.info(" [Token Controller] 토큰 갱신 성공: userId={}", userId);
        return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 갱신되었습니다.", new TokenResponse(newAccessToken, saved.getToken())));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        log.info(" [User Service] JWT 검증 요청 받음: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
        }

        String token = authHeader.substring(7);
        log.info(" [User Service] JWT 토큰: {}", token.substring(0, Math.min(20, token.length())) + "...");

        if (!jwtTokenProvider.isValidToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        if (tokenBlacklistService.isBlacklisted(token)) {
            log.warn(" [User Service] 블랙리스트된 토큰 사용 시도");
            throw new RuntimeException("로그아웃된 토큰입니다.");
        }

        JwtPayload payload = jwtTokenProvider.getPayloadFromToken(token);
        log.info(" [User Service] JWT 페이로드 파싱 성공: email={}, userId={}", payload.getEmail(), payload.getUserId());

        if (payload.getEmail() == null || payload.getUserId() == null) {
            throw new IllegalArgumentException("클레임 정보가 누락되었습니다.");
        }

        TokenValidationResponse response = new TokenValidationResponse(payload.getEmail(), payload.getUserId());
        log.info(" [User Service] JWT 검증 성공: {}", response);
        return ResponseEntity.ok(ApiResponse.success("토큰 검증이 성공했습니다.", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(@RequestHeader("X-User-Id") String userId,
                                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
                                                             @RequestBody(required = false) Map<String, String> requestBody) {
        log.info(" [Token Controller] /logout 요청 - userId: {}", userId);

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            log.info(" [Token Controller] Access Token 추출 완료 - userId: {}", userId);
        } else {
            log.warn("⚠ [Token Controller] Authorization 헤더가 없거나 형식이 잘못됨 - userId: {}", userId);
        }

        String refreshToken = null;
        if (requestBody != null && requestBody.containsKey("refreshToken")) {
            refreshToken = requestBody.get("refreshToken");
            log.info(" [Token Controller] Refresh Token 추출 완료 - userId: {}", userId);
        } else {
            log.warn(" [Token Controller] Request Body에 Refresh Token이 없음 - userId: {}", userId);
        }

        try {
            refreshTokenRepository.deleteById(Long.valueOf(userId));
            log.info(" [Token Controller] RefreshToken 삭제 완료 - userId: {}", userId);

            if (accessToken != null && !accessToken.isEmpty()) {
                tokenBlacklistService.blacklistToken(accessToken, jwtTokenProvider.getExpirationTime());
                log.info(" [Token Controller] Access Token 블랙리스트 추가 완료 - userId: {}", userId);
            }

            if (refreshToken != null && !refreshToken.isEmpty()) {
                tokenBlacklistService.blacklistRefreshToken(refreshToken, jwtTokenProvider.getRefreshExpiration());
                log.info(" [Token Controller] Refresh Token 블랙리스트 추가 완료 - userId: {}", userId);
            }

            tokenBlacklistService.recordUserLogout(Long.valueOf(userId), System.currentTimeMillis());
            log.info(" [Token Controller] 사용자 로그아웃 시간 기록 완료 - userId: {}", userId);
            
        } catch (Exception e) {
            log.error(" [Token Controller] 로그아웃 처리 중 오류 발생 - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }

        Map<String, String> result = new HashMap<>();
        result.put("userId", userId);
        result.put("message", "로그아웃이 성공적으로 처리되었습니다. Access Token과 Refresh Token이 블랙리스트에 추가되었습니다.");

        return ResponseEntity.ok(ApiResponse.success("로그아웃이 성공적으로 처리되었습니다.", result));
    }

    @GetMapping("/parse-email")
    public ResponseEntity<ApiResponse<String>> parseEmail(@RequestParam String token) {
        String email = jwtTokenProvider.getClaimFromToken(token, "sub");
        return ResponseEntity.ok(ApiResponse.success("이메일 파싱이 성공했습니다.", email));
    }

    @PostMapping("/check-blacklist")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkBlacklist(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        log.info(" [Token Controller] 블랙리스트 검증 요청 받음");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
        }

        String token = authHeader.substring(7); // "Bearer " 제거
        log.info(" [Token Controller] JWT 토큰: {}", token.substring(0, Math.min(20, token.length())) + "...");

        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isBlacklisted", isBlacklisted);
        result.put("message", isBlacklisted ? "토큰이 블랙리스트에 있습니다." : "토큰이 유효합니다.");
        
        log.info(" [Token Controller] 블랙리스트 검증 완료: isBlacklisted={}", isBlacklisted);
        return ResponseEntity.ok(ApiResponse.success("블랙리스트 검증이 완료되었습니다.", result));
    }
}