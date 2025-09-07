package com.hermes.userservice.dto;

import com.hermes.userservice.dto.title.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "사용자 생성 요청 DTO", example = "{\"name\": \"김철수\", \"email\": \"kim@example.com\", \"password\": \"password123\"}")
public class UserCreateDto {
    
    @NotBlank
    @Schema(description = "사용자 이름", required = true, example = "김철수")
    private String name;
    
    @NotBlank @Email
    @Schema(description = "사용자 이메일", required = true, example = "kim@example.com")
    private String email;
    
    @NotBlank
    @Schema(description = "비밀번호", required = true, example = "password123")
    private String password;
    
    @Schema(description = "연락처", example = "010-1234-5678")
    private String phone;
    
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;
    
    @Schema(description = "입사일", example = "2024-01-15")
    private LocalDate joinDate;
    
    @Schema(description = "관리자 여부", example = "false")
    private Boolean isAdmin;
    
    @Schema(description = "비밀번호 재설정 필요 여부", example = "false")
    private Boolean needsPasswordReset;
    
    @Schema(description = "고용 형태")
    private EmploymentTypeDto employmentType;
    
    @Schema(description = "직급")
    private RankDto rank;
    
    @Schema(description = "직책")
    private PositionDto position;
    
    @Schema(description = "직무")
    private JobDto job;
    
    @Schema(description = "역할", example = "DEVELOPER")
    private String role;
    
    @Schema(description = "근무 정책 ID", example = "1")
    private Long workPolicyId;
}