# User Service Postman 테스트 가이드

## 📋 개요
이 문서는 `user-service`의 모든 API 엔드포인트를 Postman으로 테스트하는 방법을 설명합니다.

**Base URL**: `http://localhost:8081`

---

## 🔐 인증 API

### 1. 로그인
```http
POST http://localhost:8081/api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@hermes.com",
  "password": "admin123"
}
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "로그인이 성공했습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 2. 로그아웃
```http
POST http://localhost:8081/api/v1/auth/logout
Authorization: Bearer {accessToken}
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "로그아웃이 성공적으로 처리되었습니다.",
  "data": {
    "userId": "1",
    "email": "admin@hermes.com",
    "message": "로그아웃이 성공적으로 처리되었습니다. 모든 토큰이 삭제되었습니다."
  }
}
```

### 3. 토큰 갱신
```http
POST http://localhost:8081/api/v1/auth/refresh
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "refreshToken": "{refreshToken}"
}
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "토큰이 성공적으로 갱신되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

## 👥 사용자 관리 API

### 1. 사용자 생성 (Create)
```http
POST http://localhost:8081/api/v1/users
Content-Type: application/json

{
  "name": "홍길동",
  "email": "hong@example.com",
  "password": "password123",
  "phone": "010-1234-5678",
  "address": "서울시 강남구"
}
```

**예상 응답 (201 Created):**
```json
{
  "success": true,
  "message": "사용자 생성 성공",
  "data": {
    "id": 18,
    "name": "홍길동",
    "email": "hong@example.com",
    "phone": "010-1234-5678",
    "address": "서울시 강남구",
    "joinDate": "2025-08-26",
    "isAdmin": false,
    "needsPasswordReset": false,
    "employmentType": null,
    "rank": null,
    "position": null,
    "job": null,
    "role": null,
    "profileImageUrl": null,
    "selfIntroduction": null,
    "workPolicyId": null,
    "lastLoginAt": null,
    "createdAt": "2025-08-26T15:30:00",
    "updatedAt": "2025-08-26T15:30:00"
  }
}
```

### 2. 전체 사용자 목록 조회 (Read All)
```http
GET http://localhost:8081/api/v1/users
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "사용자 목록 조회 성공",
  "data": [
    {
      "id": 1,
      "name": "관리자",
      "email": "admin@hermes.com",
      "phone": "010-0000-0000",
      "address": "서울시",
      "joinDate": "2025-01-01",
      "isAdmin": true,
      "needsPasswordReset": false,
      "employmentType": null,
      "rank": null,
      "position": null,
      "job": null,
      "role": null,
      "profileImageUrl": null,
      "selfIntroduction": null,
      "workPolicyId": null,
      "lastLoginAt": "2025-08-26T15:30:00",
      "createdAt": "2025-01-01T00:00:00",
      "updatedAt": "2025-08-26T15:30:00"
    }
  ]
}
```

### 3. 개별 사용자 조회 (Read One)
```http
GET http://localhost:8081/api/v1/users/1
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "사용자 조회 성공",
  "data": {
    "user": {
      "id": 1,
      "name": "관리자",
      "email": "admin@hermes.com",
      "phone": "010-0000-0000",
      "address": "서울시",
      "joinDate": "2025-01-01",
      "isAdmin": true,
      "needsPasswordReset": false,
      "employmentType": null,
      "rank": null,
      "position": null,
      "job": null,
      "role": null,
      "profileImageUrl": null,
      "selfIntroduction": null,
      "workPolicyId": 8480,
      "lastLoginAt": "2025-08-26T15:30:00",
      "createdAt": "2025-01-01T00:00:00",
      "updatedAt": "2025-08-26T15:30:00"
    },
    "workPolicy": {
      "id": 8480,
      "name": "기본 근무 정책",
      "description": "기본적인 근무 정책입니다."
    }
  }
}
```

### 4. 사용자 정보 수정 (Update)
```http
PATCH http://localhost:8081/api/v1/users/1
Content-Type: application/json

{
  "name": "수정된 이름",
  "phone": "010-9876-5432",
  "address": "서울시 서초구"
}
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "사용자 정보 업데이트 성공",
  "data": {
    "id": 1,
    "name": "수정된 이름",
    "email": "admin@hermes.com",
    "phone": "010-9876-5432",
    "address": "서울시 서초구",
    "joinDate": "2025-01-01",
    "isAdmin": true,
    "needsPasswordReset": false,
    "employmentType": null,
    "rank": null,
    "position": null,
    "job": null,
    "role": null,
    "profileImageUrl": null,
    "selfIntroduction": null,
    "workPolicyId": 8480,
    "lastLoginAt": "2025-08-26T15:30:00",
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-08-26T15:35:00"
  }
}
```

### 5. 사용자 삭제 (Delete)
```http
DELETE http://localhost:8081/api/v1/users/1
```

**예상 응답 (200 OK):**
```json
{
  "success": true,
  "message": "사용자 삭제 성공",
  "data": null
}
```

---

## 🧪 테스트 시나리오

### 시나리오 1: 기본 CRUD 테스트
1. **사용자 생성** → POST `/api/v1/users`
2. **사용자 목록 조회** → GET `/api/v1/users`
3. **개별 사용자 조회** → GET `/api/v1/users/{id}`
4. **사용자 정보 수정** → PATCH `/api/v1/users/{id}`
5. **사용자 삭제** → DELETE `/api/v1/users/{id}`

### 시나리오 2: 인증 테스트
1. **로그인** → POST `/api/v1/auth/login`
2. **토큰 갱신** → POST `/api/v1/auth/refresh`
3. **로그아웃** → POST `/api/v1/auth/logout`

### 시나리오 3: 에러 케이스 테스트
1. **존재하지 않는 사용자 조회** → GET `/api/v1/users/999`
2. **중복 이메일로 사용자 생성** → POST `/api/v1/users`
3. **잘못된 비밀번호로 로그인** → POST `/api/v1/auth/login`

---

## ⚠️ 주의사항

### 1. 데이터베이스 준비
- PostgreSQL 데이터베이스가 실행 중이어야 합니다
- `users` 테이블이 생성되어 있어야 합니다
- 필요한 경우 기본 데이터를 삽입하세요

### 2. 서비스 실행
- `user-service`가 8081 포트에서 실행 중이어야 합니다
- Eureka Server가 실행 중이어야 합니다 (서비스 디스커버리)

### 3. 비밀번호 해싱
- 새로 생성되는 사용자의 비밀번호는 자동으로 BCrypt로 해싱됩니다
- 기존 사용자의 비밀번호가 평문으로 저장되어 있다면 BCrypt로 업데이트하세요

### 4. 외부 서비스 의존성
- `workpolicy-service`가 실행되지 않으면 사용자 조회 시 workPolicy 정보가 null로 반환됩니다
- 이는 정상적인 동작입니다 (graceful degradation)

---

## 🔧 문제 해결

### 로그인 실패 시
1. 데이터베이스에서 비밀번호가 BCrypt로 해싱되어 있는지 확인
2. 다음 SQL로 비밀번호 업데이트:
```sql
UPDATE users 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa' 
WHERE email = 'admin@hermes.com';
```

### 서비스 연결 실패 시
1. Eureka Server가 실행 중인지 확인
2. `user-service` 로그에서 연결 오류 확인
3. 네트워크 설정 및 방화벽 확인

---

## 📝 Postman Collection

Postman에서 다음 환경 변수를 설정하세요:

| 변수명 | 값 | 설명 |
|--------|-----|------|
| `baseUrl` | `http://localhost:8081` | 기본 URL |
| `accessToken` | (로그인 후 자동 설정) | JWT 액세스 토큰 |
| `refreshToken` | (로그인 후 자동 설정) | JWT 리프레시 토큰 |
| `userId` | (사용자 생성 후 자동 설정) | 사용자 ID |

이 가이드를 따라하면 `user-service`의 모든 기능을 테스트할 수 있습니다!
