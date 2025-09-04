package com.hermes.approvalservice.dto.response;

import com.hermes.approvalservice.client.dto.UserProfile;
import com.hermes.approvalservice.enums.ActivityType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentActivityResponse {
    
    private Long id;
    private ActivityType activityType;
    private UserProfile user;
    private String description;
    private String reason;
    private LocalDateTime createdAt;
}