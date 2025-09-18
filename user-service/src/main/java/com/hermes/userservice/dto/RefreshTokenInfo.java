package com.hermes.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenInfo {
    private final Long userId;
    private final String tenantId;
}