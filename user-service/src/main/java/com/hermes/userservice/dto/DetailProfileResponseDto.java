package com.hermes.userservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DetailProfileResponseDto {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;
    
    private String address;
    private LocalDate joinDate;
}