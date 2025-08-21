package com.hermes.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthInfo {
    private Long id;
    private String email;
    private String password;
    private String role;
    private Boolean isAdmin;
}