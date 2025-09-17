# Tenant Service

Hermes 멀티테넌시 시스템의 테넌트 관리 서비스입니다.

## 주요 기능

### 테넌트 관리
- 테넌트 생성/수정/삭제/조회
- 테넌트 상태 관리 (ACTIVE/INACTIVE/SUSPENDED)
- 페이징된 테넌트 목록 조회
- 테넌트 검색 및 필터링

### 스키마 관리
- 테넌트별 데이터베이스 스키마 생성
- 스키마 초기화 및 구조 검증
- 스키마 백업 생성
- 스키마 삭제 및 정리

## API 엔드포인트

### 테넌트 관리 API
- `POST /api/v1/tenants` - 테넌트 생성
- `GET /api/v1/tenants` - 테넌트 목록 조회 (페이징)
- `GET /api/v1/tenants/{tenantId}` - 특정 테넌트 조회
- `PUT /api/v1/tenants/{tenantId}` - 테넌트 정보 수정
- `DELETE /api/v1/tenants/{tenantId}` - 테넌트 삭제
- `PATCH /api/v1/tenants/{tenantId}/status` - 테넌트 상태 변경

### 스키마 관리 API
- `POST /api/v1/schemas/{tenantId}` - 스키마 생성
- `GET /api/v1/schemas/{tenantId}` - 스키마 정보 조회
- `DELETE /api/v1/schemas/{tenantId}` - 스키마 삭제
- `POST /api/v1/schemas/{tenantId}/initialize` - 스키마 초기화
- `POST /api/v1/schemas/{tenantId}/backup` - 스키마 백업
- `POST /api/v1/schemas/{tenantId}/validate` - 스키마 검증

## 사용법

### 1. 테넌트 생성

```bash
curl -X POST http://localhost:8083/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "company1",
    "name": "Sample Company",
    "description": "샘플 회사 테넌트",
    "adminEmail": "admin@company.com",
    "createInitialSchema": true
  }'
```

### 2. 테넌트 조회

```bash
curl -X GET http://localhost:8083/api/v1/tenants/company1
```

### 3. 테넌트 목록 조회

```bash
curl -X GET "http://localhost:8083/api/v1/tenants?page=0&size=10&sort=createdAt,desc"
```

### 4. 스키마 정보 조회

```bash
curl -X GET http://localhost:8083/api/v1/schemas/company1
```

## 설정

### 기본 설정

```yaml
# application.yml
server:
  port: 8083

spring:
  application:
    name: tenant-service
  datasource:
    url: jdbc:postgresql://localhost:5432/hermes_db
    username: hermes_user
    password: hermes_password

hermes:
  multitenancy:
    enabled: true
    schema:
      auto-create: true
```

### 환경 변수

- `DB_USERNAME`: 데이터베이스 사용자명
- `DB_PASSWORD`: 데이터베이스 비밀번호
- `EUREKA_SERVER_URL`: Eureka 서버 URL

## 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │───▶│ Tenant Service  │───▶│   PostgreSQL    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   mt-starter    │
                       │ (Multi-tenancy) │
                       └─────────────────┘
```

### 주요 컴포넌트

1. **Controller Layer**
   - `TenantController`: 테넌트 CRUD API
   - `SchemaController`: 스키마 관리 API

2. **Service Layer**
   - `TenantManagementService`: 테넌트 비즈니스 로직
   - `SchemaManagementService`: 스키마 관리 로직

3. **Repository Layer**
   - `TenantManagementRepository`: 테넌트 데이터 접근

## 의존성

- **mt-starter**: 멀티테넌시 자동 설정
- **Spring Boot**: 3.5.x
- **Spring Data JPA**: 데이터 접근
- **PostgreSQL**: 메인 데이터베이스
- **Eureka Client**: 서비스 디스커버리
- **OpenAPI**: API 문서화

## 개발 환경 설정

### 1. 데이터베이스 준비

```sql
CREATE DATABASE hermes_db;
CREATE USER hermes_user WITH PASSWORD 'hermes_password';
GRANT ALL PRIVILEGES ON DATABASE hermes_db TO hermes_user;
```

### 2. 애플리케이션 시작

```bash
./gradlew :tenant-service:bootRun
```

### 3. API 문서 확인

브라우저에서 `http://localhost:8083/swagger-ui.html` 접속

## 테스트

```bash
# 단위 테스트
./gradlew :tenant-service:test

# 통합 테스트
./gradlew :tenant-service:integrationTest
```

## 로깅

애플리케이션은 다음과 같은 로그를 생성합니다:

- 테넌트 생성/수정/삭제 작업 로그
- 스키마 생성/삭제 작업 로그
- 멀티테넌시 컨텍스트 전환 로그
- 데이터베이스 쿼리 로그 (개발 환경)

## 모니터링

Actuator 엔드포인트를 통해 서비스 상태를 모니터링할 수 있습니다:

- `/actuator/health`: 헬스 체크
- `/actuator/metrics`: 메트릭
- `/actuator/info`: 애플리케이션 정보

## 주의사항

1. **스키마 삭제**: 스키마 삭제는 복구할 수 없으므로 신중하게 수행하세요
2. **동시성**: 동일한 테넌트에 대한 동시 스키마 작업은 피하세요
3. **백업**: 중요한 테넌트의 경우 정기적으로 백업을 생성하세요
4. **권한**: 관리자 권한이 있는 사용자만 테넌트를 생성/삭제할 수 있어야 합니다
