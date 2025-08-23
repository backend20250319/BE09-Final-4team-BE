package com.hermes.auth.config;

import com.hermes.auth.JwtProperties;
import com.hermes.auth.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 인증 관련 자동 구성
 * JWT 토큰 처리 및 인증 컨텍스트 관련 빈을 자동으로 등록합니다.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass({JwtTokenProvider.class, JwtProperties.class})
@EnableConfigurationProperties(JwtProperties.class)
@ComponentScan(basePackages = "com.hermes.auth")
public class AuthAutoConfiguration {

}