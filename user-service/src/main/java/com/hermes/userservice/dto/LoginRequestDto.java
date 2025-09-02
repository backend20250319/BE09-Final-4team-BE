package com.hermes.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청 DTO", example = "{\"email\": \"kim@example.com\", \"password\": \"password123\"}")
public class LoginRequestDto {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Schema(description = "사용자 이메일", required = true, example = "kim@example.com")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "비밀번호", required = true, example = "password123")
    private String password;
}
