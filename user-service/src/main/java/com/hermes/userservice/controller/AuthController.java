package com.hermes.userservice.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.userservice.dto.LoginRequestDto;
import com.hermes.userservice.dto.RefreshRequestDto;
import com.hermes.userservice.dto.TokenResponseDto;
import com.hermes.auth.dto.RefreshRequest;
import com.hermes.auth.dto.TokenResponse;
import com.hermes.auth.principal.UserPrincipal;
import com.hermes.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증 API", description = "사용자 로그인, 로그아웃, 토큰 갱신 기능 제공")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "이메일과 비밀번호를 사용하여 사용자 인증을 수행하고 JWT 토큰을 발급합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공", 
                     content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)")
    })
    public ResponseEntity<ApiResult<TokenResponseDto>> login(
            @Parameter(description = "로그인 정보", required = true) 
            @Valid @RequestBody LoginRequestDto loginDto) {
        log.info("로그인 요청: {}", loginDto.getEmail());
        TokenResponse tokenResponse = authService.login(loginDto);
        
        // TokenResponse를 TokenResponseDto로 변환
        TokenResponseDto responseDto = TokenResponseDto.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();
                
        return ResponseEntity.ok(ApiResult.success("로그인이 성공했습니다.", responseDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "사용자 로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리하고 JWT 토큰을 무효화합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "204", description = "이미 로그아웃된 상태"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResult<Void>> logout(@AuthenticationPrincipal UserPrincipal user) {
        if (user == null) {
            return ResponseEntity.status(204).body(ApiResult.success("이미 로그아웃된 상태입니다.", null));
        }
        log.info("로그아웃 요청: userId={}", user.getUserId());
        authService.logout(user.getUserId());
        return ResponseEntity.ok(ApiResult.success("로그아웃이 성공적으로 처리되었습니다.", null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", 
                     content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "토큰 갱신 실패 (만료된 또는 잘못된 리프레시 토큰)")
    })
    public ResponseEntity<ApiResult<TokenResponseDto>> refresh(
            @Parameter(description = "토큰 갱신 요청 정보", required = true) 
            @Valid @RequestBody RefreshRequestDto refreshRequestDto) {
        log.info("토큰 갱신 요청");
        
        // RefreshRequestDto를 RefreshRequest로 변환
        RefreshRequest refreshRequest = RefreshRequest.builder()
                .refreshToken(refreshRequestDto.getRefreshToken())
                .email(refreshRequestDto.getEmail())
                .build();
        
        TokenResponse tokenResponse = authService.refreshToken(refreshRequest);
        
        // TokenResponse를 TokenResponseDto로 변환
        TokenResponseDto responseDto = TokenResponseDto.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .build();
                
        return ResponseEntity.ok(ApiResult.success("토큰이 성공적으로 갱신되었습니다.", responseDto));
    }
}