# auth-starter

Hermes 프로젝트의 JWT 인증 및 Spring Security 자동 구성을 제공하는 Spring Boot Starter 라이브러리입니다.

## 개요

auth-starter는 JWT 토큰 기반 인증을 위한 Spring Security 설정을 자동으로 구성해주는 라이브러리입니다. 모든 마이크로서비스에서 일관된 인증 및 권한 관리를 제공합니다.

## 기능

- **JWT 인증**: JJWT 라이브러리를 사용한 JWT 토큰 인증
- **Spring Security 통합**: Spring Security OAuth2 Resource Server 기반 토큰 검증
- **UserPrincipal**: JWT 토큰 정보를 Spring Security UserDetails로 변환
- **자동 구성**: 최소한의 설정으로 JWT 인증 시스템 활성화
- **멀티 테넌트 지원**: JWT 토큰에서 테넌트 정보 추출
- **역할 기반 접근 제어**: ADMIN/USER 역할 기반 권한 관리
- **테스트 지원**: 단위 테스트용 Mock JWT 사용자 지원

## 사용법

### 1. 의존성 추가

```gradle
dependencies {
    implementation project(':libs:auth-starter')
}
```

### 2. 설정 파일

`application.yml`에 JWT 설정을 추가합니다:

```yaml
hermes:
  jwt:
    secret: your-jwt-secret-key-base64
    expiration: 86400  # 24시간
    refresh-expiration: 604800  # 7일
```

### 3. Security 설정

각 서비스에서 `BaseSecurityConfig`를 상속받아 서비스별 권한을 설정합니다:

```java
@Configuration
@EnableWebSecurity
public class MyServiceSecurityConfig extends BaseSecurityConfig {
    
    @Override
    protected void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    ) {
        authz
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }
}
```

### 4. 컨트롤러에서 사용

```java
@RestController
public class MyController {

    @GetMapping("/api/user/profile")
    public ApiResult<UserProfile> getProfile(
        @AuthenticationPrincipal UserPrincipal user
    ) {
        Long userId = user.getUserId();
        String email = user.getEmail();
        boolean isAdmin = user.isAdmin();
        String tenantId = user.getTenantId();
        
        // 비즈니스 로직...
        return ApiResult.success(profile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/users")
    public ApiResult<List<User>> getAllUsers() {
        // 관리자 전용 엔드포인트
    }
}
```

## 주요 컴포넌트

### UserPrincipal

JWT 토큰에서 추출한 사용자 정보를 담는 Spring Security UserDetails 구현체:

```java
@Data
public class UserPrincipal implements UserDetails {
    private Long userId;
    private String email;
    private Role role;
    private String tenantId;
    
    // 편의 메서드
    public boolean isAdmin();
    public boolean isUser();
    public boolean hasPermission(Role requiredRole);
}
```

### BaseSecurityConfig

모든 서비스가 공통으로 사용하는 Spring Security 기본 설정:

- JWT 토큰 검증 설정
- OAuth2 Resource Server 구성
- 공통 예외 처리 (401, 403 응답)
- 기본 허용 경로 (Actuator, Swagger)

### JwtAuthenticationConverter

JWT Claims를 UserPrincipal로 변환하는 컨버터:

- `sub`: 사용자 ID
- `email`: 사용자 이메일
- `role`: 사용자 역할 (ADMIN/USER)
- `tenantId`: 테넌트 ID

### Role Enum

사용자 권한을 정의하는 열거형:

```java
public enum Role {
    ADMIN, USER;
    
    public boolean isAdmin();
    public boolean isUser();
    public boolean hasPermission(Role role);
}
```

## 테스트 지원

테스트 시 Mock JWT 사용자를 생성할 수 있는 유틸리티를 제공합니다:

### @WithMockJwtUser 어노테이션

```java
@WithMockJwtUser(userId = 1L, email = "user@example.com", role = "ADMIN", tenantId = "tenant1")
@Test
void testAdminEndpoint() {
    // 관리자 권한으로 테스트
}
```

### SpringSecurityTestUtils

```java
@Test
void testUserEndpoint() {
    SpringSecurityTestUtils.setUserUser(1L);
    // 일반 사용자로 테스트
    SpringSecurityTestUtils.clearSecurityContext();
}
```

## 자동 구성

auth-starter는 다음 컴포넌트들을 자동으로 구성합니다:

- `UserPrincipal`: JWT 사용자 정보 클래스
- `JwtAuthenticationConverter`: JWT → UserPrincipal 변환기
- `BaseSecurityConfig`: 기본 Security 설정
- `JwtDecoder`: JWT 토큰 디코더
- `JwtProperties`: JWT 설정 프로퍼티

## 의존성

- Spring Boot Starter Security
- Spring Boot Starter OAuth2 Resource Server
- JJWT (JSON Web Token library)
- api-common (공통 API 응답 클래스)

## 참고사항

- JWT 비밀키는 Base64로 인코딩된 값을 사용해야 합니다
- 모든 서비스는 동일한 JWT 비밀키를 공유해야 합니다
- 토큰 만료시간은 서비스별로 다르게 설정할 수 있습니다
- 멀티테넌트 환경에서는 JWT 토큰에 `tenantId` 클레임이 포함되어야 합니다