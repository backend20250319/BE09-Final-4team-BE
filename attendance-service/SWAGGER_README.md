# Attendance Service API Documentation

## 📚 Swagger API 문서

Attendance Service의 모든 API는 Swagger OpenAPI 3를 통해 문서화되어 있습니다.

### 🚀 접속 방법

1. **로컬 개발 환경**

   ```
   http://localhost:8088/swagger-ui.html
   ```

2. **API 문서 JSON**
   ```
   http://localhost:8088/api-docs
   ```

### 📋 API 카테고리

#### 1. **Attendance (출근/퇴근 관리)**

- **출근 체크인**: `POST /api/attendance/check-in`
- **퇴근 체크아웃**: `POST /api/attendance/check-out`
- **출근 상태 기록**: `POST /api/attendance/attendance-status`
- **근무 상태 기록**: `POST /api/attendance/work-status`

#### 2. **Leave (휴가 신청 관리)**

- **휴가 신청 생성**: `POST /api/leaves`
- **휴가 신청 수정**: `PUT /api/leaves/{requestId}`
- **휴가 신청 조회**: `GET /api/leaves/{requestId}`

#### 3. **Work Policy (근무 정책 관리)**

- **근무 정책 생성**: `POST /api/workpolicy`
- **전체 근무 정책 목록 조회**: `GET /api/workpolicy`
- **근무 정책 조회**: `GET /api/workpolicy/{workPolicyId}`

#### 4. **Annual Leave (연차 정책 관리)**

- **연차 정책 생성**: `POST /api/annual-leaves/work-policies/{workPolicyId}`
- **연차 정책 조회**: `GET /api/annual-leaves/{id}`
- **근무 정책별 연차 정책 목록 조회**: `GET /api/annual-leaves/work-policies/{workPolicyId}`
- **연차 정책 수정**: `PUT /api/annual-leaves/{id}`
- **연차 정책 삭제**: `DELETE /api/annual-leaves/{id}`
- **총 연차 일수 계산**: `GET /api/annual-leaves/work-policies/{workPolicyId}/total-leave-days`
- **총 휴일 일수 계산**: `GET /api/annual-leaves/work-policies/{workPolicyId}/total-holiday-days`

#### 5. **Work Schedule (근무 스케줄 관리)**

- **사용자 근무 정책 조회**: `GET /api/work-schedule/users/{userId}/work-policy`
- **스케줄 생성**: `POST /api/work-schedule/schedules`
- **고정 스케줄 생성**: `POST /api/work-schedule/users/{userId}/fixed-schedules`

#### 6. **Work Monitor (근무 모니터링)**

- **오늘 근무 모니터링 조회**: `GET /api/work-monitor/today`
- **특정 날짜 근무 모니터링 조회**: `GET /api/work-monitor/{date}`
- **근무 모니터링 데이터 갱신**: `POST /api/work-monitor/update/{date}`
- **오늘 근무 모니터링 데이터 갱신**: `POST /api/work-monitor/update/today`

### 🔐 인증 및 권한

#### 인증 방식

- JWT 토큰 기반 인증
- Authorization 헤더에 Bearer 토큰 포함

#### 권한 레벨

- **USER**: 일반 사용자 (본인 데이터만 접근 가능)
- **ADMIN**: 관리자 (모든 데이터 접근 가능)

### 📝 API 사용 예시

#### 1. 출근 체크인

```bash
curl -X POST "http://localhost:8088/api/attendance/check-in" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 1,
    "checkIn": "2024-01-15T09:00:00"
  }'
```

#### 2. 휴가 신청 생성

```bash
curl -X POST "http://localhost:8088/api/leaves" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "employeeId": 1,
    "leaveType": "ANNUAL",
    "startDate": "2024-01-20",
    "endDate": "2024-01-22",
    "reason": "개인 휴가"
  }'
```

#### 3. 근무 정책 생성

```bash
curl -X POST "http://localhost:8088/api/workpolicy" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "일반 근무 정책",
    "type": "FIXED",
    "workCycle": "WEEKLY",
    "startDayOfWeek": "MONDAY",
    "workDays": 5,
    "weeklyWorkingDays": 5,
    "startTime": "09:00",
    "workHours": 8,
    "workMinutes": 0,
    "annualLeaves": [
      {
        "name": "신입 연차",
        "minYears": 0,
        "maxYears": 1,
        "leaveDays": 15,
        "holidayDays": 0
      }
    ]
  }'
```

### 🛠️ 개발 환경 설정

#### 1. 의존성 추가

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

#### 2. 설정 파일 (application.yml)

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  packages-to-scan: com.hermes.attendanceservice.controller
```

#### 3. OpenAPI 설정 클래스

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI attendanceServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Attendance Service API")
                        .description("근태 관리 서비스 API 문서")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8088").description("Local Development Server")
                ));
    }
}
```

### 📊 응답 형식

모든 API는 일관된 응답 형식을 사용합니다:

```json
{
  "success": true,
  "message": "성공 메시지",
  "data": {
    // 응답 데이터
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### ❌ 에러 처리

에러 발생 시 다음과 같은 형식으로 응답됩니다:

```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 🔍 API 테스트

Swagger UI에서 제공하는 기능:

- **Try it out**: API를 직접 테스트할 수 있는 기능
- **Parameters**: 요청 파라미터 입력
- **Responses**: 응답 예시 및 스키마 확인
- **Schemas**: 데이터 모델 정의 확인

### 📞 지원

API 사용 중 문제가 발생하면 다음을 확인해주세요:

1. JWT 토큰이 유효한지 확인
2. 요청 파라미터가 올바른지 확인
3. 권한이 충분한지 확인
4. 서버 로그를 확인하여 상세한 에러 메시지 확인
