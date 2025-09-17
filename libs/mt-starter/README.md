# Mt-Starter

Hermes 시스템을 위한 Spring Boot 멀티테넌시 자동 구성 스타터입니다.

## 개요

Mt-Starter는 스키마별 테넌트(schema-per-tenant) 패턴을 사용하여 PostgreSQL 기반의 멀티테넌시 기능을 자동으로 구성합니다. JWT 기반 테넌트 식별, RabbitMQ를 통한 이벤트 기반 스키마 관리, Flyway를 사용한 자동 마이그레이션을 제공합니다.

## 주요 기능

- **스키마별 테넌트 아키텍처**: 각 테넌트마다 독립된 PostgreSQL 스키마
- **JWT 기반 테넌트 라우팅**: JWT 토큰에서 테넌트 정보 자동 추출
- **자동 스키마 생성/삭제**: RabbitMQ 이벤트를 통한 스키마 생명주기 관리
- **Flyway 통합**: 테넌트별 데이터베이스 마이그레이션 자동화
- **동적 DataSource 라우팅**: 요청 컨텍스트 기반 자동 데이터베이스 라우팅
- **Spring Boot 자동 구성**: 최소한의 설정으로 즉시 사용 가능

## 설치

`build.gradle`에 의존성을 추가하세요:

```gradle
dependencies {
    implementation project(':libs:mt-starter')
}
```

## 기본 사용법

### 1. 설정 활성화

`application.yml`에 다음 설정을 추가하세요:

```yaml
hermes:
  multitenancy:
    enabled: true
    schema:
      auto-create: true
    rabbitmq:
      enabled: true
```

### 2. 자동 구성

의존성을 추가하면 다음 컴포넌트들이 자동으로 구성됩니다:

- **TenantContextFilter**: 테넌트 라우팅 및 컨텍스트 관리
- **TenantRoutingDataSource**: 동적 DataSource 라우팅
- **FlywayTenantInitializer**: 자동 스키마 마이그레이션
- **TenantEventListener**: RabbitMQ 이벤트 처리

### 3. 엔티티 및 리포지토리

표준 JPA 엔티티와 Spring Data 리포지토리를 사용하면 테넌트 라우팅이 자동으로 적용됩니다:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 필드들...
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

## 설정 옵션

### 멀티테넌시 설정

```yaml
hermes:
  multitenancy:
    enabled: true                    # 멀티테넌시 활성화 여부
    default-tenant-id: "default"     # 기본 테넌트 ID
    default-schema-name: "public"    # 기본 스키마명
    
    # JWT 설정
    jwt:
      tenant-claim-name: "tenantId"  # JWT 테넌트 클레임명
      use-email-domain: true         # 이메일 도메인을 테넌트 ID로 사용
      
    # 스키마 관리
    schema:
      auto-create: true              # 스키마 자동 생성
      validate-on-startup: true      # 시작시 스키마 검증
      schema-prefix: "tenant_"       # 스키마명 접두사
      allow-drop: false              # 스키마 삭제 허용
      
    # 데이터소스 설정
    data-source:
      max-pool-size: 10              # 최대 연결 풀 크기
      min-idle-size: 2               # 최소 유휴 연결 수
      connection-timeout-ms: 30000   # 연결 타임아웃
      
    # 캐시 설정
    cache:
      enabled: true                  # 캐시 활성화
      ttl-minutes: 60               # 캐시 만료 시간
      max-size: 1000                # 최대 캐시 크기
```

### RabbitMQ 설정

```yaml
hermes:
  multitenancy:
    rabbitmq:
      enabled: true
      # 기본적으로 "tenant.events.{service-name}" 큐 생성
```

## 테넌트 라이프사이클 이벤트

시스템은 다음 RabbitMQ 이벤트를 처리합니다:

- **TENANT_CREATED**: 모든 서비스에서 스키마 생성
- **TENANT_DELETED**: 모든 서비스에서 스키마 삭제
- **TENANT_UPDATED**: 테넌트 메타데이터 업데이트

## 스키마 명명 규칙

- **패턴**: `tenant_{tenantId}`
- **예시**: `tenant_1`, `tenant_company_a`

## 고급 사용법

### 커스텀 테넌트 이벤트 리스너

```java
@Component
public class CustomTenantEventListener extends AbstractTenantEventListener {
    
    @Override
    protected void handleTenantCreated(Long tenantId) {
        // 커스텀 테넌트 생성 로직
        super.handleTenantCreated(tenantId);
    }
    
    @Override
    protected void handleTenantDeleted(Long tenantId) {
        // 커스텀 테넌트 삭제 로직
        super.handleTenantDeleted(tenantId);
    }
}
```

### 테넌트 컨텍스트 직접 사용

```java
@Service
public class MyService {

    public void doSomething() {
        Long tenantId = TenantContext.getCurrentTenantId();
        String schemaName = TenantUtils.getSchemaName(tenantId);
        // 비즈니스 로직...
    }
}
```

### @NonTenant 어노테이션 사용

특정 API에서 테넌트 격리를 적용하지 않으려면 `@NonTenant` 어노테이션을 사용하세요. 이 어노테이션이 적용된 컨트롤러 메소드나 클래스는 데이터베이스 기본 설정(public 스키마)을 사용합니다.

#### 메소드 레벨 적용

```java
@RestController
@RequestMapping("/api")
public class PublicController {

    @NonTenant
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/tenant-data")  // 테넌트별 데이터 접근
    public ResponseEntity<List<Data>> getTenantData() {
        // 현재 테넌트의 스키마에서 데이터 조회
        return ResponseEntity.ok(dataService.findAll());
    }
}
```

#### 클래스 레벨 적용

```java
@NonTenant
@RestController
@RequestMapping("/system")
public class SystemController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        // 모든 메소드가 public 스키마 사용
        return ResponseEntity.ok(metricsService.getSystemMetrics());
    }
}
```

#### 우선순위

어노테이션 적용 우선순위는 다음과 같습니다:
1. **메소드 레벨** `@NonTenant` (최우선)
2. **클래스 레벨** `@NonTenant`
3. **기본 동작**: 테넌트별 스키마 라우팅

#### 일반적인 사용 사례

- **시스템 헬스체크**: `/health`, `/metrics`
- **공개 API**: 로그인, 회원가입, 공지사항
- **관리자 전용 API**: 전체 시스템 모니터링
- **Cross-tenant 데이터**: 공통 설정, 메타데이터

## 의존성

이 스타터는 다음 의존성들을 포함합니다:

- Spring Boot Data JPA
- Spring Boot AMQP (RabbitMQ)
- Flyway Core
- PostgreSQL Flyway
- Auth-Starter (JWT 지원)
- Events 라이브러리

## 제한사항

- PostgreSQL만 지원
- 스키마별 테넌트 패턴만 지원
- JWT 기반 인증 필수

## 트러블슈팅

### 일반적인 문제들

1. **스키마 생성 실패**
   - PostgreSQL 권한 확인
   - RabbitMQ 연결 상태 확인

2. **테넌트 라우팅 실패**
   - JWT 토큰의 tenantId 클레임 확인
   - TenantContextFilter 설정 확인

3. **Flyway 마이그레이션 실패**
   - `db/migration` 디렉토리 구조 확인
   - SQL 스크립트 문법 검증
