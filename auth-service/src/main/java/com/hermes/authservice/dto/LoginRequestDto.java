package com.hermes.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequestDto {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
