package com.hermes.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMemberRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "임시 비밀번호는 필수입니다.")
    private String tempPassword;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "입사일은 필수입니다.")
    private LocalDate joinDate;

    private Boolean isAdmin = false;

    private Long employmentTypeId;
    private Long rankId;
    private Long positionId;
    private Long jobId;

    @Size(max = 50, message = "직무는 50자 이하여야 합니다.")
    private String role;

    @Size(max = 500, message = "자기소개는 500자 이하여야 합니다.")
    private String selfIntroduction;

    private String profileImage;

    private List<Long> organizationIds;

}