package com.hermes.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtPayload {
    private final String userId;
    private final String email;
    private final String role;

    public JwtPayload(String email) {
        this.userId = null;
        this.email = email;
        this.role = null;
    }
}
