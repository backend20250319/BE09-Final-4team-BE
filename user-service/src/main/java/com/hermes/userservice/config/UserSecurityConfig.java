package com.hermes.userservice.config;

import com.hermes.auth.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class UserSecurityConfig extends BaseSecurityConfig {
    
    @Override
    protected void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers("/api/auth/**").permitAll();
        
        auth.requestMatchers("/api/users/count").hasRole("ADMIN");
        auth.requestMatchers("/api/users/ids").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.PATCH, "/api/users/*/profile-image").authenticated();
        auth.requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN");
        auth.requestMatchers("/api/users/sync-organizations").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.POST, "/api/users/*/sync-organization").hasRole("ADMIN");
        
        auth.requestMatchers(HttpMethod.POST, "/api/v1/titles/ranks").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.PUT, "/api/v1/titles/ranks/**").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.DELETE, "/api/v1/titles/ranks/**").hasRole("ADMIN");
        
        auth.requestMatchers(HttpMethod.POST, "/api/v1/titles/positions").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.PUT, "/api/v1/titles/positions/**").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.DELETE, "/api/v1/titles/positions/**").hasRole("ADMIN");
        
        auth.requestMatchers(HttpMethod.POST, "/api/v1/titles/jobs").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.PUT, "/api/v1/titles/jobs/**").hasRole("ADMIN");
        auth.requestMatchers(HttpMethod.DELETE, "/api/v1/titles/jobs/**").hasRole("ADMIN");
        
        auth.requestMatchers(HttpMethod.GET, "/api/v1/titles/**").authenticated();
        auth.requestMatchers(HttpMethod.GET, "/api/users/**").authenticated();
    }
}