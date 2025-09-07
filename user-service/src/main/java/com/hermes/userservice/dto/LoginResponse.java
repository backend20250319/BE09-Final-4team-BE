package com.hermes.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "로그인 응답 DTO")
public class LoginResponse {
    
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
    private Long expiresIn;
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    
    @Schema(description = "사용자 역할", example = "USER")
    private String role;
    
    @Schema(description = "비밀번호 재설정 필요 여부", example = "true")
    private Boolean needsPasswordReset;
}
