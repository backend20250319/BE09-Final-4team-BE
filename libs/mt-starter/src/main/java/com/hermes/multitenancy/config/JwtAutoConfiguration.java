package com.hermes.multitenancy.config;

import com.hermes.jwt.JwtProperties;
import com.hermes.jwt.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * JWT 자동 구성
 * JwtTokenProvider 빈을 자동으로 등록합니다.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass({JwtTokenProvider.class, JwtProperties.class})
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    /**
     * JwtTokenProvider 빈 자동 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        log.info("Auto-configuring JwtTokenProvider with properties: {}", jwtProperties.getClass().getSimpleName());
        return new JwtTokenProvider(jwtProperties);
    }

    /**
     * JWT 자동 구성 정보 로깅
     */
    @Bean
    public Object logJwtAutoConfiguration(JwtProperties jwtProperties) {
        log.info("JWT Auto Configuration completed:");
        log.info("  - Expiration Time: {}ms", jwtProperties.getExpirationTime());
        log.info("  - Refresh Expiration: {}ms", jwtProperties.getRefreshExpiration());
        log.info("  - JwtTokenProvider: Auto-registered");
        return new Object();
    }
}
