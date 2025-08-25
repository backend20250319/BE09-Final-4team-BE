package com.hermes.userservice.controller;

import com.hermes.auth.dto.ApiResponse;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.auth.dto.TokenResponse;
import com.hermes.auth.dto.RefreshRequest;
import com.hermes.userservice.service.UserService;
import com.hermes.jwt.JwtTokenProvider;
import com.hermes.jwt.context.UserInfo;
import com.hermes.jwt.context.AuthContext;
import com.hermes.userservice.entity.RefreshToken;
import com.hermes.userservice.repository.RefreshTokenRepository;
import com.hermes.userservice.repository.UserRepository;
import com.hermes.auth.service.TokenBlacklistService;
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
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        
        Long userId = AuthContext.getCurrentUserId();
        String email = AuthContext.getCurrentUserEmail();
        
        if (userId == null) {
            throw new IllegalArgumentException("사용자 인증 정보가 누락되었습니다.");
        }
        
        log.info(" [Auth Controller] /logout 요청 - userId: {}, email: {}", userId, email);

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7); // "Bearer " 제거
            log.info(" [Auth Controller] Access Token 추출 완료 - userId: {}", userId);
        } else {
            log.warn("⚠ [Auth Controller] Authorization 헤더가 없거나 형식이 잘못됨 - userId: {}", userId);
        }

        userService.logout(accessToken);

        Map<String, String> result = new HashMap<>();
        result.put("userId", userId.toString());
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

        UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
        
        if (userInfo.getUserId() == null || userInfo.getEmail() == null) {
            throw new RuntimeException("JWT에서 필수 사용자 정보를 추출할 수 없습니다.");
        }
        
        Long userId = userInfo.getUserId();
        String email = userInfo.getEmail();

        RefreshToken saved = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("RefreshToken not found"));

        if (!saved.getToken().equals(request.getRefreshToken())) {
            throw new RuntimeException("유효하지 않은 RefreshToken입니다.");
        }

        if (tokenBlacklistService.isRefreshTokenBlacklisted(request.getRefreshToken())) {
            throw new RuntimeException("로그아웃된 Refresh Token입니다.");
        }

        Instant now = Instant.now();
        Instant expiration = saved.getExpiration().atZone(ZoneId.systemDefault()).toInstant();

        if (expiration.isBefore(now)) {
            throw new RuntimeException("만료된 RefreshToken입니다.");
        }

        com.hermes.userservice.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        String userRole = user.getIsAdmin() ? "ADMIN" : "USER";
        String newAccessToken = jwtTokenProvider.createToken(email, userId, userRole);

        log.info(" [Auth Controller] 토큰 갱신 성공: userId={}", userId);
        return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 갱신되었습니다.",
                new TokenResponse(newAccessToken, saved.getToken())));
    }
}