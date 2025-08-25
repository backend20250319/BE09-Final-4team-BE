package com.hermes.userservice.config;

import com.hermes.auth.JwtTokenProvider;
import com.hermes.auth.JwtProperties;
import com.hermes.auth.filter.AuthContextFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        return new JwtTokenProvider(properties);
    }

    @Bean
    public AuthContextFilter authContextFilter(JwtTokenProvider jwtTokenProvider) {
        return new AuthContextFilter(jwtTokenProvider);
    }
}