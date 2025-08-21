# Multi-tenancy Starter (mt-starter) - 완전 자동화 버전 🚀

Spring Boot 마이크로서비스에서 **의존성 하나만 추가하면 끝!** 
완전히 자동화된 멀티테넌시 시스템입니다.

## ✨ 핵심 장점

- 🔥 **Zero Configuration**: 코드 작성 없이 의존성 + 설정파일만
- 🎯 **Auto Schema Management**: 테넌트 생성/삭제 시 자동 스키마 관리  
- 📦 **Out-of-the-box**: 별도 구현 없이 바로 사용 가능
- 🔧 **Customizable**: 필요시 세부 커스터마이징 가능

## 🚀 2단계로 끝나는 설정

### 1단계: 의존성 추가

```gradle
dependencies {
    implementation project(':libs:mt-starter')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.postgresql:postgresql'
}
```

### 2단계: 설정 파일 작성

```yaml
# application.yml
spring:
  application:
    name: my-service  # 🔥 이것만으로 Queue 이름이 자동 결정!
    
  datasource:
    url: jdbc:postgresql://localhost:5432/my_service_db
    username: my_service_user
    password: my_service_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

hermes:
  multitenancy:
    enabled: true
    schema:
      auto-create: true
    rabbitmq:
      enabled: true
```

## 🎉 완료!

**그게 전부입니다!** 이제 서비스가 자동으로:

✅ `tenant.events.my-service` Queue 생성  
✅ RabbitMQ 바인딩 설정  
✅ 테넌트 생성/삭제 이벤트 수신  
✅ 자동 스키마 생성/삭제  
✅ Dead Letter Queue 설정  
✅ 에러 처리 & 재시도  

## 🏗️ 자동으로 생성되는 것들

### RabbitMQ 구성요소
- **Queue**: `tenant.events.{서비스명}`
- **Dead Letter Queue**: `tenant.events.dlq.{서비스명}`  
- **Exchange 바인딩**: `tenant.*` 패턴으로 자동 바인딩
- **Event Listener**: 기본 스키마 관리 로직 포함

### 이벤트 처리
- **테넌트 생성** → 자동 스키마 생성 (`tenant_company1`)
- **테넌트 삭제** → 자동 스키마 삭제
- **테넌트 업데이트/상태변경** → 로그 출력 (필요시 커스터마이징)

## 🎯 사용 예시

서비스 시작 시 로그:

```
[my-service] Auto-created tenant event queue: tenant.events.my-service
[my-service] Auto-created dead letter queue: tenant.events.dlq.my-service
[my-service] Default Tenant Event Listener initialized
[my-service] Tenant Event Auto Configuration completed
```

테넌트 이벤트 수신 시:

```
[my-service] Tenant Event Received: Type=TENANT_CREATED, TenantId=company1, SchemaName=tenant_company1
[my-service] 새 테넌트 스키마 생성 시작: schemaName=tenant_company1
[my-service] 새 테넌트 스키마 생성 완료: schemaName=tenant_company1
```

## 🔧 커스터마이징 (선택사항)

### 방법 1: 전체 커스터마이징

기본 리스너를 대체하고 싶다면:

```java
@Component("tenantEventListener")  // 🔥 이 이름이 중요!
public class MyCustomTenantEventListener extends AbstractTenantEventListener {
    
    public MyCustomTenantEventListener(RabbitMQProperties properties, SchemaUtils schemaUtils) {
        super(properties);
    }

    @Override
    protected String getServiceName() {
        return "my-service";
    }

    @RabbitListener(queues = "tenant.events.my-service")
    @Override
    public void handleTenantEvent(TenantEvent event) {
        // 커스텀 로직
        super.handleTenantEvent(event);
    }
    
    // 필요한 메서드만 오버라이드...
}
```

### 방법 2: 설정만 커스터마이징

```yaml
hermes:
  multitenancy:
    rabbitmq:
      tenant-exchange: my.custom.exchange
      max-retry-count: 5
      retry-delay: 10000
```

## 📋 지원되는 서비스 예시

### News Service
```yaml
spring:
  application:
    name: news-service
```
→ 자동 생성: `tenant.events.news-service`

### Order Service  
```yaml
spring:
  application:
    name: order-service
```
→ 자동 생성: `tenant.events.order-service`

### Payment Service
```yaml
spring:
  application:
    name: payment-service  
```
→ 자동 생성: `tenant.events.payment-service`

## 🎊 비교: 이전 vs 지금

### ❌ 이전 (수동 설정)
1. `RabbitMQConfig.java` 작성 (50줄)
2. `ServiceTenantEventListener.java` 작성 (100줄)  
3. Queue/Binding 설정
4. 각 서비스마다 반복...

### ✅ 지금 (완전 자동화)
1. 의존성 추가
2. `application.yml` 설정
3. **끝!**

## 🚨 주의사항

1. **서비스명 중복 금지**: `spring.application.name`이 고유해야 함
2. **RabbitMQ 필수**: RabbitMQ 서버가 실행 중이어야 함  
3. **데이터베이스 권한**: 스키마 생성/삭제 권한 필요

## 🐛 트러블슈팅

### Q: Queue가 생성되지 않아요
```yaml
# spring.application.name이 설정되어 있는지 확인
spring:
  application:
    name: your-service-name  # 필수!
```

### Q: 커스텀 리스너가 작동하지 않아요
```java
@Component("tenantEventListener")  // 정확한 Bean 이름 필요
public class MyListener extends AbstractTenantEventListener {
    // ...
}
```

### Q: 스키마 생성이 실패해요
```yaml
# 데이터베이스 사용자에게 스키마 생성 권한 부여 필요
GRANT CREATE ON DATABASE my_service_db TO my_service_user;
```

## 📈 로드맵

- [ ] Redis 캐싱 자동 설정
- [ ] 메트릭 & 모니터링 자동 설정  
- [ ] Kafka 지원
- [ ] 스키마 마이그레이션 자동화

---

**🎉 이제 정말 의존성 하나면 멀티테넌시 완성!**
