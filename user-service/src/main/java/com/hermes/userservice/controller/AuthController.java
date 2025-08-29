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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequestDto loginDto) {
        TokenResponse tokenResponse = authService.login(loginDto);
        return ResponseEntity.ok(ApiResponse.success("로그인이 성공했습니다.", tokenResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authHeader) {

        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더 형식입니다.");
        }
        String accessToken = authHeader.substring(7); // "Bearer " 제거
        authService.logout(user.getUserId(), accessToken);
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 성공적으로 처리되었습니다.", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestBody RefreshRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("토큰이 성공적으로 갱신되었습니다.", tokenResponse));
    }
}