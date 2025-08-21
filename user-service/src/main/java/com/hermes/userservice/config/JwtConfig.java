package com.hermes.userservice.config;

import com.hermes.jwt.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public TokenBlacklistService tokenBlacklistService() {
        return new TokenBlacklistService();
    }
}