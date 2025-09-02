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
- **news-crawler-service**: News article crawling and management
- **tenant-service**: Multi-tenant management and schema operations
- **approval-service**: Document approval workflow and template management
- **companyinfo-service**: Company information and settings management
- **communication-service**: File transfer and communication utilities

### Shared Libraries (Spring Boot Starters)
- **auth-starter**: Spring Boot Starter for JWT authentication and authorization with auto-configuration
- **mt-starter**: Multi-tenancy auto-configuration starter with RabbitMQ event-driven schema management
- **ftp-starter**: FTP file transfer utilities with auto-configuration
- **attachment-client-starter**: Attachment service integration and file handling utilities with auto-configuration
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

**Startup Order**: config-server → discovery-server → gateway-server → other services

## Key Configuration

### External Dependencies
- **PostgreSQL**: Primary database for all services
- **RabbitMQ**: Message broker for tenant event distribution

### Service Discovery
All services register with Eureka for service discovery

## Authentication & Security

### auth-starter Usage
Simply add the dependency to enable Spring Security-based JWT authentication:

```gradle
dependencies {
    implementation project(':libs:auth-starter')
}
```

**Auto-Configured Components:**
- **UserPrincipal**: Spring Security `UserDetails` implementation (`@Component`)
- **JwtAuthenticationConverter**: JWT → Spring Security Authentication converter (`@Component`)
- **BaseSecurityConfig**: Abstract security configuration with common JWT settings (`@Configuration`)
- **JwtDecoder**: OAuth2 JWT token decoder (`@Bean`)
- **TenantContextFilter**: Multi-tenant context setup integrated with Spring Security (`@Component`)

### Spring Security Integration
Hermes has migrated from custom AuthContext to standard Spring Security patterns:

```java
// In controllers - use @AuthenticationPrincipal
@PostMapping("/documents")
public ApiResult<Document> createDocument(
    @AuthenticationPrincipal UserPrincipal user,
    @RequestBody CreateDocumentRequest request
) {
    Long userId = user.getUserId();
    boolean isAdmin = user.getRole() == Role.ADMIN;
    // Service logic...
}

// Method-level security with role checking
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ApiResult<List<User>> getAllUsers() {
    // Admin-only endpoint
}
```

**Key Components:**
- **UserPrincipal**: Spring Security UserDetails with userId, email, role, tenantId
- **JwtAuthenticationConverter**: Converts JWT claims to UserPrincipal
- **BaseSecurityConfig**: Shared security configuration for all services
- **Role**: Enum for ADMIN/USER with Spring Security integration

### Security Configuration Pattern
Each service inherits from `BaseSecurityConfig` for consistent security setup:

```java
@Configuration
@EnableWebSecurity
public class MyServiceSecurityConfig extends BaseSecurityConfig {
    
    @Override
    protected void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        authz
            .requestMatchers("/api/public/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }
}
```

### JWT Token Management
- **Token Creation**: user-service uses `JwtTokenService` for login/refresh
- **Token Validation**: Spring Security OAuth2 Resource Server handles verification
- **Token Blacklist**: user-service manages logout token blacklist

### Authentication Flow
1. Login via user-service → JWT token creation
2. Client sends JWT in Authorization header
3. Spring Security validates JWT and creates UserPrincipal
4. Controllers access user info via `@AuthenticationPrincipal UserPrincipal`

### Testing Support
Use Spring Security test utilities for authentication testing:

```java
// Using custom test annotation
@WithMockJwtUser(userId = 1L, role = "ADMIN")
@Test
void testAdminEndpoint() {
    // Test with mock admin user
}

// Using test utils programmatically
@Test
void testUserEndpoint() {
    SpringSecurityTestUtils.setUserUser(1L);
    // Test with mock regular user
    SpringSecurityTestUtils.clearSecurityContext(); // Cleanup
}
```

**Test Components:**
- **@WithMockJwtUser**: Custom annotation for JWT-based test users 
- **SpringSecurityTestUtils**: Programmatic test authentication setup
- **WithMockJwtUserSecurityContextFactory**: SecurityContext factory for tests

## Attachment Service Integration

### attachment-client-starter Usage
Add attachment-client-starter dependency to enable attachment service integration:

```gradle
dependencies {
    implementation project(':libs:attachment-client-starter')
}
```

**Auto-Configured Components:**
- **AttachmentInfo**: JPA `@Embeddable` entity for attachment metadata storage
- **AttachmentServiceClient**: Feign client for attachment-service communication with circuit breaker
- **AttachmentClientService**: Business logic for attachment validation and conversion

### Usage Pattern
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
    
    // Validate and convert requests to entities
    List<AttachmentInfo> attachments = attachmentClientService
        .validateAndConvertAttachments(request.getAttachments());
    
    // Convert entities to response DTOs  
    List<AttachmentInfoResponse> responses = attachmentClientService
        .convertToResponseList(document.getAttachments());
}
```

## Multi-Tenancy

### mt-starter Usage
Add mt-starter dependency and minimal configuration:

```gradle
dependencies {
    implementation project(':libs:mt-starter')
}
```

```yaml
spring:
  application:
    name: my-service
hermes:
  multitenancy:
    enabled: true
    schema:
      auto-create: true
    rabbitmq:
      enabled: true
```

This automatically creates `tenant.events.my-service` queue and handles schema lifecycle.

**Auto-Configured Components:**
- **TenantContextFilter**: Tenant routing and context management
- **TenantRoutingDataSource**: Dynamic DataSource routing
- **FlywayTenantInitializer**: Automatic schema migration
- **TenantEventListener**: RabbitMQ event handling for schema operations

### Implementation Details
- **Schema-per-tenant** pattern with PostgreSQL
- **Automatic schema creation/deletion** via RabbitMQ events
- **JWT-based tenant identification** from token payload
- **Dynamic DataSource routing** based on tenant context
- **Schema naming**: `tenant_{tenantId}`

### Tenant Lifecycle Events
- **TENANT_CREATED**: Triggers schema creation in all services
- **TENANT_DELETED**: Triggers schema deletion in all services  
- **TENANT_UPDATED**: Updates tenant metadata

## Development Patterns

### Entity Definition
Standard JPA entities are automatically routed to tenant-specific schemas:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // fields...
}
```

### Repository Usage
Use standard Spring Data JPA repositories - tenant routing is automatic:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

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