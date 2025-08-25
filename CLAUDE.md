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
- **leave-service**: Employee leave request and management
- **communication-service**: File transfer and communication utilities

### Shared Libraries (Spring Boot Starters)
- **auth-starter**: Spring Boot Starter for JWT authentication and authorization with auto-configuration
- **mt-starter**: Multi-tenancy auto-configuration starter with RabbitMQ event-driven schema management
- **ftp-starter**: FTP file transfer utilities with auto-configuration
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
Simply add the dependency to enable JWT authentication:

```gradle
dependencies {
    implementation project(':libs:auth-starter')
}
```

**Auto-Configured Components:**
- **JwtTokenProvider**: JWT token creation and validation (`@Component`)
- **JwtService**: High-level JWT operations (`@Service`)
- **TokenBlacklistService**: Token revocation and blacklist management (`@Service`)
- **AuthContextFilter**: Request authentication and context setup (`@Component`)
- **JwtProperties**: Configuration properties with `jwt.*` prefix (`@Component`)


### AuthContext System
Hermes uses a ThreadLocal-based authentication context for clean, consistent user access:

```java
// In controllers - no @RequestHeader needed
Long currentUserId = AuthContext.getCurrentUserId();
boolean isAdmin = AuthContext.isCurrentUserAdmin();
String userRole = AuthContext.getCurrentUserRole();

// Automatic permission checking
AuthContext.requireAdmin(); // Throws exception if not admin
AuthContext.requirePermission(Role.USER); // Throws if insufficient permission

// Type-safe role checking
Role currentRole = AuthContext.getCurrentUser().getRole();
boolean hasPermission = currentRole.hasPermission(Role.ADMIN);
```

**Key Components:**
- **AuthContext**: ThreadLocal-based user information storage
- **AuthContextFilter**: Extracts user info from JWT tokens → AuthContext
- **UserInfo**: User data model (userId, email, role, tenantId)
- **Role**: Enum for type-safe role management (ADMIN, USER)

### Authentication Flow
1. Login via user-service
2. JWT token with tenant context
3. Gateway validates tokens and forwards them
4. Services extract user info from JWT via AuthContextFilter → AuthContext

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

## Testing

### Authentication Testing
Use AuthTestUtils for easy test authentication setup:

```java
@Test
void testAdminFunction() {
    // Set up admin user for test
    AuthTestUtils.setAdminUser(1L);
    
    // Test admin functionality
    assertTrue(AuthContext.isCurrentUserAdmin());
    assertEquals(1L, AuthContext.getCurrentUserId());
}

@Test  
void testUserFunction() {
    // Set up regular user
    AuthTestUtils.setUserUser(100L);
    
    // Test user functionality
    assertFalse(AuthContext.isCurrentUserAdmin());
    assertTrue(AuthContext.getCurrentUser().isUser());
}

@AfterEach
void cleanUp() {
    AuthTestUtils.clearAuthContext(); // Clean up after each test
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
    )
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