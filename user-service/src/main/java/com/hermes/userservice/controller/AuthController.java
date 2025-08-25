package com.hermes.userservice.controller;

import com.hermes.auth.JwtTokenProvider;
import com.hermes.auth.context.UserInfo;
import com.hermes.auth.context.AuthContext;
import com.hermes.userservice.dto.LoginRequest;
import com.hermes.userservice.dto.LoginResponse;
import com.hermes.userservice.dto.RegisterRequest;
import com.hermes.userservice.dto.RegisterResponse;
import com.hermes.userservice.entity.User;
import com.hermes.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("회원가입 요청: {}", request.getEmail());

        try {
            User user = userService.registerUser(request);
            RegisterResponse response = RegisterResponse.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .message("회원가입이 성공적으로 완료되었습니다.")
                    .build();

            log.info("회원가입 성공: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(RegisterResponse.builder()
                    .message("회원가입에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: {}", request.getEmail());

        try {
            LoginResponse response = userService.loginUser(request);
            log.info("로그인 성공: {}", request.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse.builder()
                    .message("로그인에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message("토큰이 제공되지 않았습니다.")
                    .build());
        }

        try {
            UserInfo userInfo = jwtTokenProvider.getUserInfoFromToken(token);
            if (userInfo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse.builder()
                        .message("유효하지 않은 토큰입니다.")
                        .build());
            }

            String email = userInfo.getEmail();
            Long userId = userInfo.getUserId();
            String userRole = userInfo.getRole().toString();
            String newAccessToken = jwtTokenProvider.createToken(email, userId, userRole);

            LoginResponse response = LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .message("토큰이 갱신되었습니다.")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse.builder()
                    .message("토큰 갱신에 실패했습니다: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            userService.logoutUser(token);
            log.info("로그아웃 성공");
        }
        return ResponseEntity.ok("로그아웃되었습니다.");
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser() {
        UserInfo currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(currentUser);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}