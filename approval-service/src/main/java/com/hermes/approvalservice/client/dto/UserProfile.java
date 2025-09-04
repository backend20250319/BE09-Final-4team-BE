package com.hermes.approvalservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profileImageUrl;

}