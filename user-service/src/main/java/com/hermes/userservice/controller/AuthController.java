package com.hermes.userservice.controller;

import com.hermes.jwt.dto.ApiResponse;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.jwt.dto.TokenResponse;
import com.hermes.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

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

        userService.logout(Long.valueOf(userId), accessToken);

        Map<String, String> result = new HashMap<>();
        result.put("userId", userId);
        result.put("email", email != null ? email : "unknown");
        result.put("message", "로그아웃이 성공적으로 처리되었습니다. Access Token이 블랙리스트에 추가되었습니다.");
        
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 성공적으로 처리되었습니다.", result));
    }
}
