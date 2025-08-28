package com.hermes.auth.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Security 기반 인증 관련 자동 구성
 * JWT 인증, UserPrincipal, BaseSecurityConfig 관련 빈을 자동으로 등록합니다.
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.hermes.auth")
@ConfigurationPropertiesScan(basePackages = "com.hermes.auth")
public class AuthAutoConfiguration {

}