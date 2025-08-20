package com.hermes.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResult {
    private boolean isValid;
    private String message;
    private Map<String, Object> claims;
}
