package com.hermes.userservice.controller;

import com.hermes.auth.dto.ApiResponse;
import com.hermes.auth.dto.RefreshRequest;
import com.hermes.auth.dto.TokenResponse;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequestDto loginDto) {
        log.info("로그인 요청: {}", loginDto.getEmail());
        TokenResponse tokenResponse = authService.login(loginDto);
        return ResponseEntity.ok(ApiResponse.success("로그인이 성공했습니다.", tokenResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(204).body(ApiResponse.success("이미 로그아웃된 상태입니다.", null));
        }
        log.info("로그아웃 요청: userId={}", user.getUserId());
        authService.logout(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 성공적으로 처리되었습니다.", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody RefreshRequest request) {
        log.info("토큰 갱신 요청: refreshToken={}", request.getRefreshToken());
        TokenResponse tokenResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 갱신되었습니다.", tokenResponse));
    }
}