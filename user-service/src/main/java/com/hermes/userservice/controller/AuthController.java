package com.hermes.userservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.dto.RefreshRequestDto;
import com.hermes.userservice.dto.LoginResponse;
import com.hermes.userservice.dto.LoginResult;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.userservice.service.AuthService;
import com.hermes.userservice.service.AuthCookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "사용자 로그인, 로그아웃, 토큰 갱신 기능 제공")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "이메일과 비밀번호를 사용하여 사용자 인증을 수행하고 JWT 토큰을 발급합니다. RefreshToken은 HttpOnly 쿠키로 설정됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
                     content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)")
    })
    public ResponseEntity<ApiResult<LoginResponse>> login(
            @Parameter(description = "로그인 정보", required = true) 
            @Valid @RequestBody LoginRequestDto loginDto) {
        log.info("로그인 요청: {}", loginDto.getEmail());
        LoginResult loginResult = authService.login(loginDto);
        
        // RefreshToken을 HttpOnly 쿠키로 설정
        ResponseCookie refreshTokenCookie = authCookieService.createRefreshTokenCookie(loginResult.getRefreshToken());
        
        // LoginResult를 LoginResponse로 변환 (AccessToken과 사용자 정보 포함)
        LoginResponse responseDto = LoginResponse.builder()
                .accessToken(loginResult.getAccessToken())
                .expiresIn(loginResult.getExpiresIn())
                .userId(loginResult.getUserId())
                .email(loginResult.getEmail())
                .name(loginResult.getName())
                .role(loginResult.getRole())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResult.success("로그인이 성공했습니다.", responseDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "사용자 로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리하고 JWT 토큰을 무효화합니다. RefreshToken 쿠키도 삭제됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "204", description = "이미 로그아웃된 상태"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResult<Void>> logout(@AuthenticationPrincipal UserPrincipal user) {
        // RefreshToken 쿠키 삭제
        ResponseCookie deleteRefreshTokenCookie = authCookieService.createRefreshTokenDeleteCookie();
        
        if (user == null) {
            return ResponseEntity.status(204)
                    .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString())
                    .body(ApiResult.success("이미 로그아웃된 상태입니다.", null));
        }
        
        log.info("로그아웃 요청: userId={}", user.getUserId());
        authService.logout(user.getUserId());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie.toString())
                .body(ApiResult.success("로그아웃이 성공적으로 처리되었습니다.", null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "HttpOnly 쿠키의 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다. 새로운 RefreshToken도 HttpOnly 쿠키로 설정됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", 
                     content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 RefreshToken 쿠키 없음"),
        @ApiResponse(responseCode = "401", description = "토큰 갱신 실패 (만료된 또는 잘못된 리프레시 토큰)")
    })
    public ResponseEntity<ApiResult<LoginResponse>> refresh(
            @Parameter(description = "토큰 갱신 요청 정보", required = true) 
            @Valid @RequestBody RefreshRequestDto refreshRequest,
            HttpServletRequest request) {
        log.info("토큰 갱신 요청");
        
        // 쿠키에서 RefreshToken 추출
        String refreshToken = authCookieService.getRefreshTokenFromRequest(request)
                .orElseThrow(() -> new IllegalArgumentException("RefreshToken 쿠키가 없습니다."));
        
        LoginResult loginResult = authService.refreshToken(refreshRequest.getEmail(), refreshToken);
        
        // 새로운 RefreshToken을 HttpOnly 쿠키로 설정
        ResponseCookie refreshTokenCookie = authCookieService.createRefreshTokenCookie(loginResult.getRefreshToken());
        
        // LoginResult를 LoginResponse로 변환 (AccessToken과 사용자 정보 포함)
        LoginResponse responseDto = LoginResponse.builder()
                .accessToken(loginResult.getAccessToken())
                .userId(loginResult.getUserId())
                .email(loginResult.getEmail())
                .name(loginResult.getName())
                .role(loginResult.getRole())
                .expiresIn(loginResult.getExpiresIn())
                .build();
                
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResult.success("토큰이 성공적으로 갱신되었습니다.", responseDto));
    }
}