package com.hermes.multitenancy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Flyway 멀티테넌시 설정
 */
@Data
@ConfigurationProperties(prefix = "hermes.multitenancy.flyway")
public class FlywayProperties {
    
    /**
     * Flyway 멀티테넌시 활성화 여부
     */
    private boolean enabled = true;
    
    /**
     * 테넌트 스키마용 migration 위치
     */
    private List<String> locations = List.of("classpath:db/migration/tenant");
    
    /**
     * 기본 스키마용 migration 위치 (공통 테이블용)
     */
    private List<String> defaultLocations = List.of("classpath:db/migration");
    
    /**
     * migration 테이블명
     */
    private String table = "flyway_schema_history";
    
    /**
     * 베이스라인 버전
     */
    private String baselineVersion = "1";
    
    /**
     * 베이스라인 설명
     */
    private String baselineDescription = "Initial tenant schema";
    
    /**
     * 베이스라인 자동 생성 여부
     */
    private boolean baselineOnMigrate = true;
    
    /**
     * 스키마 검증 활성화 여부
     */
    private boolean validateOnMigrate = true;
    
    /**
     * 빈 스키마에서 migration 허용 여부
     */
    private boolean cleanOnValidationError = false;
    
    /**
     * migration 실행 시 트랜잭션 사용 여부
     */
    private boolean executeInTransaction = true;
}
