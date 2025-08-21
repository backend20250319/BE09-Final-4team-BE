package com.hermes.userservice.controller;

import com.hermes.jwt.dto.ApiResponse;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.jwt.dto.TokenResponse;
import com.hermes.jwt.dto.RefreshRequest;
import com.hermes.userservice.service.UserService;
import com.hermes.jwt.JwtTokenProvider;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserRepository;
import com.hermes.jwt.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequestDto loginDto) {
        log.info(" [Auth Controller] /login 요청 - email: {}", loginDto.getEmail());
        TokenResponse tokenResponse = userService.login(loginDto);
        return ResponseEntity.ok(ApiResponse.success("로그인이 성공했습니다.", tokenResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                                                   @RequestHeader(value = "X-User-Email", required = false) String email,
                                                                   @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        log.info(" [Auth Controller] /logout 요청 - userId: {}, email: {}", userId, email);

        if (userId == null) {
            throw new IllegalArgumentException("X-User-Id 헤더가 누락되었습니다. Gateway에서 JWT 검증이 실패했거나 헤더 주입이 되지 않았습니다.");
        }

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7); // "Bearer " 제거
            log.info(" [Auth Controller] Access Token 추출 완료 - userId: {}", userId);
        } else {
            log.warn("⚠ [Auth Controller] Authorization 헤더가 없거나 형식이 잘못됨 - userId: {}", userId);
        }

        // refreshToken도 함께 전달하여 완전한 로그아웃 처리
        userService.logout(Long.valueOf(userId), accessToken);

        Map<String, String> result = new HashMap<>();
        result.put("userId", userId);
        result.put("email", email != null ? email : "unknown");
        result.put("message", "로그아웃이 성공적으로 처리되었습니다. 모든 토큰이 삭제되었습니다.");

        return ResponseEntity.ok(ApiResponse.success("로그아웃이 성공적으로 처리되었습니다.", result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody RefreshRequest request) {

        log.info(" [Auth Controller] /refresh 요청");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
        }

        String token = authHeader.substring(7);

        if (!jwtTokenProvider.isValidToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        Long userId = Long.valueOf(jwtTokenProvider.getClaimFromToken(token, "userId"));
        String email = jwtTokenProvider.getEmailFromToken(token);

        RefreshToken saved = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("RefreshToken not found"));

        if (!saved.getToken().equals(request.getRefreshToken())) {
            throw new RuntimeException("유효하지 않은 RefreshToken입니다.");
        }

        if (tokenBlacklistService.isRefreshTokenBlacklisted(request.getRefreshToken())) {
            throw new RuntimeException("로그아웃된 Refresh Token입니다.");
        }

        // 만료 시간 확인
        Instant now = Instant.now();
        Instant expiration = saved.getExpiration().atZone(ZoneId.systemDefault()).toInstant();

        if (expiration.isBefore(now)) {
            throw new RuntimeException("만료된 RefreshToken입니다.");
        }

        // 사용자 정보를 조회하여 실제 권한을 확인
        com.hermes.userservice.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        String userRole = user.getIsAdmin() ? "ADMIN" : "USER";
        String newAccessToken = jwtTokenProvider.createToken(email, userId, userRole);

        log.info(" [Auth Controller] 토큰 갱신 성공: userId={}", userId);
        return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 갱신되었습니다.",
                new TokenResponse(newAccessToken, saved.getToken())));
    }
}