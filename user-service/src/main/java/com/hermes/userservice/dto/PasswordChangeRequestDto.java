package com.hermes.userservice.dto;

import com.hermes.userservice.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 변경 요청 DTO")
public class PasswordChangeRequestDto {
    
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @Schema(description = "현재 비밀번호", example = "currentPassword123")
    private String currentPassword;
    
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @ValidPassword
    @Schema(description = "새 비밀번호", example = "NewPassword123!")
    private String newPassword;
}
