package com.hermes.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "토큰 갱신 요청 DTO", example = "{\"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"email\": \"kim@example.com\"}")
public class RefreshRequestDto {
    
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    @Schema(description = "리프레시 토큰", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Schema(description = "사용자 이메일", required = true, example = "kim@example.com")
    private String email;
}
