package com.hermes.userservice.config;

import com.hermes.auth.config.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
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
    }
}