package com.hermes.userservice.dto;

import com.hermes.userservice.entity.*;
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
    private EmploymentType employmentType;
    private Rank rank;
    private Position position;
    private Job job;
    private String role;
    private Long workPolicyId;
    private String profileImageUrl;
    private String selfIntroduction;
}
