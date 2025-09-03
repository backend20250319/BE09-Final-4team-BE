# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Hermes is a Spring Boot microservices system implementing a multi-tenant architecture with schema-per-tenant strategy. It provides enterprise workforce management capabilities including user management, organization structure, attendance tracking, document approval, and news crawling.

## Architecture

### Microservices Structure
- **config-server**: Spring Cloud Config Server for centralized configuration
- **discovery-server**: Eureka service discovery 
- **gateway-server**: Spring Cloud Gateway for API routing and authentication
- **user-service**: User authentication, authorization, and management
- **org-service**: Organization hierarchy and employee assignment management
- **attendance-service**: Employee attendance tracking and work hour management  
- **news-crawler-service**: News article crawling and management
- **tenant-service**: Multi-tenant management and schema operations
- **approval-service**: Document approval workflow and template management
- **companyinfo-service**: Company information and settings management
- **communication-service**: File transfer and communication utilities

### Shared Libraries
- **auth-starter**: Spring Boot Starter for JWT authentication and authorization with auto-configuration
- **mt-starter**: Multi-tenancy auto-configuration starter with RabbitMQ event-driven schema management
- **attachment-client-starter**: Attachment service integration and file handling utilities with auto-configuration
- **notification-starter**: RabbitMQ-based asynchronous notification system with auto-configuration
- **api-common**: Common API response classes and utilities
- **events**: Event models for inter-service communication

## Development Commands

### Build & Test
```bash
# Build entire project
./gradlew build '-Dfile.encoding=UTF-8'

# Build specific service
./gradlew :user-service:build '-Dfile.encoding=UTF-8'

# Run tests
./gradlew test

# Run tests for specific service  
./gradlew :user-service:test

# Clean build
./gradlew clean build '-Dfile.encoding=UTF-8'
```

**Startup Order**: discovery-server → config-server → gateway-server → other services

## Key Configuration

### External Dependencies
- **PostgreSQL**: Primary database for all services
- **RabbitMQ**: Message broker for tenant event distribution

### Service Discovery
All services register with Eureka for service discovery

## Authentication & Security

### Quick Setup
Add dependency and create security configuration:

```gradle
dependencies {
    implementation project(':libs:auth-starter')
}
```

```java
@Configuration
@EnableWebSecurity
public class MyServiceSecurityConfig extends BaseSecurityConfig {
    @Override
    protected void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        authz.requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }
}
```

### Key Usage Patterns
```java
// Controllers - use @AuthenticationPrincipal
@PostMapping("/documents")
public ApiResult<Document> createDocument(
    @AuthenticationPrincipal UserPrincipal user,
    @RequestBody CreateDocumentRequest request
) {
    Long userId = user.getId();
    boolean isAdmin = user.isAdmin();
    // Service logic...
}

// Method-level security
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ApiResult<List<User>> getAllUsers() {
    // Admin-only endpoint
}
```

**📋 For detailed configuration, testing, and components, see [`libs/auth-starter/README.md`](libs/auth-starter/README.md)**

## Attachment Service Integration

### Quick Setup
Add attachment-client-starter dependency to enable attachment service integration:

```gradle
dependencies {
    implementation project(':libs:attachment-client-starter')
}
```

### Key Usage Patterns
```java
// Entity with embedded attachments
@Entity
public class Document {
    @ElementCollection
    private List<AttachmentInfo> attachments = new ArrayList<>();
}

// Service usage
@Service 
public class DocumentService {
    private final AttachmentClientService attachmentClientService;
    
    // Validate and convert fileId lists to entities
    List<AttachmentInfo> attachments = attachmentClientService
        .validateAndConvertAttachments(request.getAttachments());
    
    // Convert entities to response DTOs  
    List<AttachmentInfoResponse> responses = attachmentClientService
        .convertToResponseList(document.getAttachments());
}
```

**📋 For detailed configuration, DTOs, and circuit breaker setup, see [`libs/attachment-client-starter/README.md`](libs/attachment-client-starter/README.md)**

## Multi-Tenancy

### Quick Setup
Add mt-starter dependency and minimal configuration:

```gradle
dependencies {
    implementation project(':libs:mt-starter')
}
```

```yaml
hermes:
  multitenancy:
    enabled: true
    schema:
      auto-create: true
    rabbitmq:
      enabled: true
```

### Key Implementation Details
- **Schema-per-tenant** pattern with PostgreSQL (`tenant_{tenantId}`)
- **Automatic schema creation/deletion** via RabbitMQ events
- **JWT-based tenant identification** from token payload
- **Dynamic DataSource routing** - transparent to application code

### Usage Pattern
Standard JPA entities and repositories work automatically:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;
    // fields...
}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

**📋 For detailed configuration options, events, and advanced usage, see [`libs/mt-starter/README.md`](libs/mt-starter/README.md)**

## Notification System

### Quick Setup
Add notification-starter dependency and minimal configuration:

```gradle
dependencies {
    implementation project(':libs:notification-starter')
}
```

```yaml
hermes:
  notification:
    enabled: true
```

### Key Usage Pattern
Inject NotificationPublisher and send notifications:

```java
@Service
public class MyService {
    private final NotificationPublisher notificationPublisher;
    
    public void sendNotification() {
        NotificationRequest request = NotificationRequest.builder()
            .userIds(Arrays.asList(1L, 2L, 3L))
            .type(NotificationType.ANNOUNCEMENT)
            .content("Notification content")
            .referenceId(123L)
            .createdAt(LocalDateTime.now())
            .build();
            
        NotificationResponse response = notificationPublisher.publish(request);
    }
}
```

**📋 For detailed configuration, notification types, and error handling, see [`libs/notification-starter/README.md`](libs/notification-starter/README.md)**

## Development Patterns

## API Documentation

### Integrated Swagger Documentation
Hermes provides centralized API documentation through Gateway integration:

- **Gateway Swagger UI**: All services unified through Gateway
- **Individual Service**: Service-specific documentation available

### Adding Swagger to New Services

**1. Add Dependencies:**
```gradle
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.11'
}
```

**2. Create OpenApiConfig:**
```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Service Name API",
        description = "Service description",
        version = "1.0.0"
    ),
    security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT Bearer 토큰을 입력하세요"
)
public class OpenApiConfig {
    // Configuration if needed
}
```

**3. Gateway Integration:**
- Add service group to `GatewayOpenApiConfig.java`
- Add API docs routing to `gateway-server/application.yml`

### Standards
- **Language**: All descriptions in Korean
- **Error Codes**: Document 403 for admin-only endpoints
- **Parameters**: Use `@Parameter` for clear documentation

## Development Guidelines

- **Commit messages**: Always write in Korean, keep them concise and clear
- **No automatic commits**: Never commit without explicit user instruction
- **Encoding**: Always use `-Dfile.encoding=UTF-8` when building with gradlew
- **Selective building**: Only build modified modules when testing, not the entire project
- Always use `ApiResult<T>` from `api-common` for API responses
- **Time Handling**: Use `Instant` only, never `LocalDateTime`. Timezone conversion is client responsibility