# Hermes Multi-tenancy Starter

Hermes MSA 프로젝트를 위한 멀티테넌시 자동 설정 라이브러리입니다.

## 주요 기능

- **Schema-per-tenant 방식**: 각 테넌트마다 별도의 데이터베이스 스키마 사용
- **자동 DataSource 라우팅**: 테넌트별 자동 데이터 소스 전환
- **JWT 기반 테넌트 식별**: JWT 토큰에서 테넌트 정보 자동 추출
- **ThreadLocal 컨텍스트**: 요청별 테넌트 컨텍스트 자동 관리
- **Spring Boot 자동 설정**: 의존성 추가만으로 멀티테넌시 기능 활성화

## 사용법

### 1. 의존성 추가

각 서비스의 `build.gradle`에 다음 의존성을 추가합니다:

```gradle
dependencies {
    implementation project(':libs:mt-starter')
}
```

### 2. 설정 추가

`application.yml`에 다음 설정을 추가합니다:

```yaml
# 기본 데이터베이스 설정
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hermes_db
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver

# 멀티테넌시 설정
hermes:
  multitenancy:
    enabled: true
    schema:
      auto-create: true
```

### 3. 엔티티 설정

기존 엔티티 클래스는 그대로 사용 가능합니다. 라이브러리가 자동으로 테넌트별 스키마를 관리합니다.

```java
@Entity
@Table(name = "users")
public class User {
    // 기존 코드 그대로 사용
}
```

### 4. JWT에 테넌트 정보 포함

JWT 토큰에 테넌트 정보를 포함해야 합니다:

```java
// JWT 생성 시 테넌트 정보 포함
Claims claims = Jwts.claims().setSubject(email);
claims.put("tenantId", "company1"); // 테넌트 ID 추가
```

## 테넌트 식별 방법

라이브러리는 다음 순서로 테넌트를 식별합니다:

1. **HTTP 헤더**: `X-Tenant-ID` 헤더
2. **JWT 토큰**: `tenantId` 클레임
3. **하위 도메인**: `tenant1.example.com`에서 `tenant1` 추출
4. **이메일 도메인**: `user@company.com`에서 `company` 추출

## API 사용 예시

### 테넌트 컨텍스트 접근

```java
@Service
public class UserService {
    
    public void someMethod() {
        // 현재 테넌트 ID 조회
        String tenantId = TenantContext.getCurrentTenantId();
        
        // 현재 스키마명 조회
        String schemaName = TenantContext.getCurrentSchemaName();
        
        // 테넌트 정보 조회
        TenantInfo tenant = TenantContext.getTenant();
    }
}
```

### 특정 테넌트로 작업 실행

```java
@Service
public class SomeService {
    
    public void processForTenant(String tenantId) {
        TenantInfo tenantInfo = TenantInfo.of(tenantId, "tenant_" + tenantId);
        
        TenantContext.executeWithTenant(tenantInfo, () -> {
            // 이 블록 내에서는 지정된 테넌트 컨텍스트로 실행됨
            return userRepository.findAll(); // 해당 테넌트의 데이터만 조회
        });
    }
}
```

## 테넌트 관리

### 새 테넌트 생성

```java
@Service
@RequiredArgsConstructor
public class TenantManagementService {
    
    private final TenantService tenantService;
    private final SchemaUtils schemaUtils;
    
    public void createNewTenant(String tenantId, String name, String adminEmail) {
        // 테넌트 등록
        Tenant tenant = tenantService.createTenant(tenantId, name, adminEmail);
        
        // 스키마 생성
        schemaUtils.createSchema(tenant.getSchemaName());
        
        // 스키마 초기화
        tenantService.initializeTenantSchema(tenantId);
    }
}
```

## 설정 옵션

`application.yml`에서 사용 가능한 설정 옵션들:

```yaml
hermes:
  multitenancy:
    enabled: true                    # 멀티테넌시 기능 활성화
    default-tenant-id: default       # 기본 테넌트 ID
    default-schema-name: public      # 기본 스키마명
    
    cache:
      enabled: true                  # 테넌트 정보 캐시
      ttl-minutes: 60               # 캐시 만료 시간
      max-size: 1000                # 최대 캐시 크기
    
    data-source:
      max-pool-size: 10             # 최대 연결 풀 크기
      min-idle-size: 2              # 최소 유휴 연결
      connection-timeout-ms: 30000  # 연결 타임아웃
    
    jwt:
      tenant-claim-name: tenantId   # JWT 테넌트 클레임명
      use-email-domain: true        # 이메일 도메인 사용
    
    schema:
      auto-create: true             # 스키마 자동 생성
      validate-on-startup: true     # 시작 시 스키마 검증
      schema-prefix: tenant_        # 스키마 접두사
      allow-drop: false             # 스키마 삭제 허용
```

## 데이터베이스 구조

### 메타데이터 스키마 (public)
```sql
-- 테넌트 정보는 public 스키마에 저장
CREATE TABLE public.tenants (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    schema_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    admin_email VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### 테넌트별 스키마
```sql
-- 각 테넌트마다 별도 스키마 생성
CREATE SCHEMA tenant_company1;
CREATE SCHEMA tenant_company2;

-- 각 스키마에는 동일한 테이블 구조
CREATE TABLE tenant_company1.users (...);
CREATE TABLE tenant_company1.orders (...);
```

## 주의사항

1. **데이터베이스 권한**: 애플리케이션 사용자는 스키마 생성 권한이 필요합니다.
2. **연결 풀**: 테넌트가 많을 경우 연결 풀 설정을 적절히 조정하세요.
3. **캐시 관리**: 운영 환경에서는 Redis 등 외부 캐시 사용을 권장합니다.
4. **스키마 마이그레이션**: 새 버전 배포 시 모든 테넌트 스키마 업데이트가 필요합니다.

## 트러블슈팅

### 일반적인 문제들

1. **스키마 권한 오류**
   ```
   ERROR: permission denied for schema tenant_xxx
   ```
   해결: 데이터베이스 사용자에게 스키마 생성/사용 권한 부여

2. **테넌트를 찾을 수 없음**
   ```
   No tenant found for token
   ```
   해결: JWT에 올바른 테넌트 정보가 포함되어 있는지 확인

3. **연결 풀 부족**
   ```
   Connection pool exhausted
   ```
   해결: `max-pool-size` 설정 증가

## 라이센스

이 프로젝트는 Hermes 프로젝트의 일부입니다.
