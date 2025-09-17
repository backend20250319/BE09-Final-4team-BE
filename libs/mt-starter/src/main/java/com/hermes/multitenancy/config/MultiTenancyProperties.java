package com.hermes.multitenancy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 멀티테넌시 설정 속성
 */
@Data
@ConfigurationProperties(prefix = "hermes.multitenancy")
public class MultiTenancyProperties {

    /**
     * 멀티테넌시 기능 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 기본 테넌트 ID
     */
    private String defaultTenantId = "default";


    /**
     * 엔티티 스캔 패키지 목록
     */
    private List<String> entityPackages = new ArrayList<>();

    /**
     * 리포지토리 스캔 패키지 목록
     */
    private List<String> repositoryPackages = new ArrayList<>();

    /**
     * 테넌트 캐시 설정
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 데이터베이스 연결 풀 설정
     */
    private DataSourceConfig dataSource = new DataSourceConfig();

    /**
     * JWT 설정
     */
    private JwtConfig jwt = new JwtConfig();

    /**
     * 스키마 관리 설정
     */
    private SchemaConfig schema = new SchemaConfig();

    /**
     * 보안 설정
     */
    private SecurityConfig security = new SecurityConfig();

    @Data
    public static class CacheConfig {
        /**
         * 캐시 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 캐시 만료 시간 (분)
         */
        private long ttlMinutes = 60;

        /**
         * 최대 캐시 크기
         */
        private int maxSize = 1000;
    }

    @Data
    public static class DataSourceConfig {
        /**
         * 최대 연결 풀 크기
         */
        private int maxPoolSize = 10;

        /**
         * 최소 유휴 연결 수
         */
        private int minIdleSize = 2;

        /**
         * 연결 타임아웃 (밀리초)
         */
        private long connectionTimeoutMs = 30000;

        /**
         * 유휴 타임아웃 (밀리초)
         */
        private long idleTimeoutMs = 600000;

        /**
         * 최대 수명 (밀리초)
         */
        private long maxLifetimeMs = 1800000;
    }

    @Data
    public static class JwtConfig {
        /**
         * JWT에서 테넌트 정보 추출 시 사용할 클레임명
         */
        private String tenantClaimName = "tenantId";

        /**
         * 대안 클레임명들
         */
        private List<String> alternativeClaimNames = List.of("tenant", "org", "organization");

        /**
         * 이메일 도메인을 테넌트 ID로 사용할지 여부
         */
        private boolean useEmailDomain = true;
    }

    @Data
    public static class SchemaConfig {
        /**
         * 스키마 자동 생성 여부
         */
        private boolean autoCreate = true;

        /**
         * 애플리케이션 시작 시 스키마 검증 여부
         */
        private boolean validateOnStartup = true;

        /**
         * 스키마명 접두사
         */
        private String schemaPrefix = "tenant_";

        /**
         * 스키마 삭제 허용 여부
         */
        private boolean allowDrop = false;
    }

    /**
     * 엔티티 패키지 추가
     */
    public void addEntityPackage(String packageName) {
        if (!entityPackages.contains(packageName)) {
            entityPackages.add(packageName);
        }
    }

    /**
     * 리포지토리 패키지 추가
     */
    public void addRepositoryPackage(String packageName) {
        if (!repositoryPackages.contains(packageName)) {
            repositoryPackages.add(packageName);
        }
    }

    @Data
    public static class SecurityConfig {
        /**
         * Tenant 정보가 없을 때의 처리 전략
         */
        private FallbackStrategy strategy = FallbackStrategy.FAIL_FAST;

        /**
         * 인터셉터에서 제외할 경로 목록
         */
        private List<String> excludePaths = List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico"
        );

        /**
         * 로깅 활성화 여부
         */
        private boolean enableSecurityLogging = true;
    }

    /**
     * Tenant fallback 전략
     */
    public enum FallbackStrategy {
        /**
         * Tenant 정보가 없으면 즉시 예외 발생 (권장)
         */
        FAIL_FAST,

        /**
         * 기본 테넌트로 허용하고 로그 남김
         */
        LOG_AND_ALLOW,

        /**
         * 기본 테넌트로 조용히 허용 (비권장)
         */
        ALLOW_DEFAULT
    }
}
