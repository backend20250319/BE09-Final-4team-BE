package com.hermes.userservice.dto;

import com.hermes.userservice.dto.title.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;

@Getter
@Setter
public class UserUpdateDto {
    private String name;
    @Email
    private String email;
    private String password;
    private String phone;
    private String address;
    private LocalDate joinDate;
    private Boolean isAdmin;
    private Boolean needsPasswordReset;
    private EmploymentTypeDto employmentType;
    private RankDto rank;
    private PositionDto position;
    private JobDto job;
    private String role;
    private Long workPolicyId;
    private String profileImageUrl;
    private String selfIntroduction;
}
