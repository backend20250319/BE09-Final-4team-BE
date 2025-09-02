package com.hermes.userservice.dto;

import com.hermes.userservice.entity.EmploymentType;
import com.hermes.userservice.entity.Job;
import com.hermes.userservice.entity.Position;
import com.hermes.userservice.entity.Rank;
import com.hermes.userservice.dto.workpolicy.WorkPolicyResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 응답 DTO", example = "{\"id\": 1, \"name\": \"김철수\", \"email\": \"kim@example.com\"}")
public class UserResponseDto {
    
    @Schema(description = "사용자 고유 ID", example = "1")
    private Long id;
    
    @Schema(description = "사용자 이름", example = "김철수")
    private String name;
    
    @Schema(description = "사용자 이메일", example = "kim@example.com")
    private String email;
    
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
    private EmploymentType employmentType;
    
    @Schema(description = "직급")
    private Rank rank;
    
    @Schema(description = "직책")
    private Position position;
    
    @Schema(description = "직무")
    private Job job;
    
    @Schema(description = "역할", example = "DEVELOPER")
    private String role;
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;
    
    @Schema(description = "자기소개", example = "안녕하세요! 개발자 김철수입니다.")
    private String selfIntroduction;
    
    @Schema(description = "근무 정책 ID", example = "1")
    private Long workPolicyId;
    
    @Schema(description = "마지막 로그인 시간", example = "2024-01-15T09:00:00")
    private LocalDateTime lastLoginAt;
    
    @Schema(description = "생성 시간", example = "2024-01-15T09:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정 시간", example = "2024-01-15T09:00:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "근무 정책 정보")
    private WorkPolicyResponseDto workPolicy;
    
    @Schema(description = "소속 조직 정보 목록")
    private List<UserOrganizationDto> organizations;
}
