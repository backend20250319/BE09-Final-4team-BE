# Organization Service

조직 관리 서비스입니다. 조직 구조와 직원 배정을 관리합니다.

## 주요 기능

### 조직 관리
- 조직 생성, 조회, 수정, 삭제
- 조직 계층 구조 관리 (상위/하위 조직)
- 조직 검색

### 직원 배정 관리
- 직원을 조직에 배정
- 메인 부서 설정
- 조직장 지정
- 배정 이력 관리

## API 엔드포인트

### 조직 API
- `POST /api/organizations` - 조직 생성
- `GET /api/organizations/{organizationId}` - 조직 조회
- `GET /api/organizations` - 전체 조직 목록 조회
- `GET /api/organizations/root` - 최상위 조직 목록 조회
- `GET /api/organizations/hierarchy` - 조직 계층 구조 조회
- `PUT /api/organizations/{organizationId}` - 조직 수정
- `DELETE /api/organizations/{organizationId}` - 조직 삭제
- `GET /api/organizations/search?keyword={keyword}` - 조직 검색

### 직원 배정 API
- `POST /api/assignments` - 직원 배정 생성
- `GET /api/assignments/{assignmentId}` - 배정 조회
- `GET /api/assignments/employee/{employeeId}` - 직원의 모든 배정 조회
- `GET /api/assignments/employee/{employeeId}/primary` - 직원의 메인 부서 조회
- `GET /api/assignments/organization/{organizationId}` - 조직의 모든 배정 조회
- `GET /api/assignments/organization/{organizationId}/leaders` - 조직의 리더 조회
- `PUT /api/assignments/{assignmentId}` - 배정 수정
- `DELETE /api/assignments/{assignmentId}` - 배정 삭제

## 데이터베이스 스키마

### Organization (조직)
- `organization_id` (PK) - 조직 ID
- `name` - 조직명
- `parent_id` (FK) - 상위 조직 ID

### EmployeeAssignment (직원 배정)
- `assignment_id` (PK) - 배정 ID
- `employee_id` - 직원 ID (user-service의 User ID 참조)
- `organization_id` (FK) - 조직 ID
- `is_primary` - 메인 부서 여부
- `is_leader` - 조직장 여부
- `assigned_at` - 배정 일시

## 서비스 포트
- **포트**: 8089
- **서비스명**: org-service

## API 테스트

### 조직 생성 예시
```bash
# 최상위 조직 생성
curl -X POST http://localhost:8089/api/organizations \
  -H "Content-Type: application/json" \
  -d '{"name": "개발본부"}'

# 하위 조직 생성
curl -X POST http://localhost:8089/api/organizations \
  -H "Content-Type: application/json" \
  -d '{"name": "프론트엔드팀", "parentId": 1}'
```

### 조직 계층 구조 조회
```bash
curl -X GET http://localhost:8089/api/organizations/hierarchy
```

### 조직 검색
```bash
curl -X GET "http://localhost:8089/api/organizations/search?keyword=개발"
```

## 의존성
- Spring Boot 3.5.4
- Spring Cloud 2025.0.0
- Spring Data JPA
- MySQL
- Eureka Client
- Config Client
