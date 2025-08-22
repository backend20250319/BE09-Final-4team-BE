# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Hermes is a Spring Boot microservices system implementing a multi-tenant architecture with schema-per-tenant strategy. It provides enterprise workforce management capabilities including user management, organization structure, attendance tracking, and news crawling.

## Architecture

### Microservices Structure
- **config-server** (port 8888): Spring Cloud Config Server for centralized configuration
- **discovery-server** (port 8761): Eureka service discovery 
- **gateway-server** (port 9000): Spring Cloud Gateway for API routing
- **user-service** (port 8081): User authentication, authorization, and management
- **org-service**: Organization hierarchy and employee assignment management
- **attendance-service**: Employee attendance tracking and work hour management  
- **news-crawler-service**: News article crawling and management
- **tenant-service**: Multi-tenant management and schema operations
- **approval-service** (port 8084): Approval workflow and document approval management

### Shared Libraries
- **jwt-common**: JWT token handling, validation, and security utilities
- **mt-starter**: Multi-tenancy auto-configuration starter with RabbitMQ event-driven schema management

### Multi-Tenancy Implementation
- **Schema-per-tenant** pattern with PostgreSQL
- **Automatic schema creation/deletion** via RabbitMQ events
- **JWT-based tenant identification** from token payload
- **Dynamic DataSource routing** based on tenant context

## Development Commands

### Build & Test
```bash
# Build entire project
./gradlew build

# Build specific service
./gradlew :user-service:build

# Run tests
./gradlew test

# Run tests for specific service  
./gradlew :user-service:test

# Clean build
./gradlew clean build
```

### Running Services
```bash
# Run individual service
./gradlew :config-server:bootRun
./gradlew :discovery-server:bootRun
./gradlew :gateway-server:bootRun
./gradlew :user-service:bootRun
./gradlew :approval-service:bootRun
```

**Startup Order**: config-server → discovery-server → other services

### Database Operations
```bash
# PostgreSQL connection per service
# Each service uses independent database:
# - user-service: hermes_user_db
# - org-service: hermes_org_db  
# - news-crawler-service: hermes_news_db
# - tenant-service: hermes_tenant_db
# - approval-service: hermes_approval_db
```

## Key Configuration

### External Dependencies
- **PostgreSQL**: Primary database for all services
- **RabbitMQ**: Message broker for tenant event distribution
- **External Config Repo**: https://github.com/backend20250319/hermes-config.git

### Service Discovery
All services register with Eureka at `http://localhost:8761/eureka`

### Gateway Routing
- `/api/users/**` → user-service  
- `/api/news/**` → news-crawler-service
- `/api/approval/**` → approval-service

## Multi-Tenancy Usage

### Auto-Configuration (Recommended)
Add mt-starter dependency and minimal configuration:

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

### JWT Tenant Context
JWT tokens must include `tenantId` field:
```json
{
  "userId": "user123",
  "tenantId": "company1", 
  "role": "USER"
}
```

### Schema Naming Convention
Tenant schemas follow pattern: `tenant_{tenantId}`

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

### Exception Handling
- Global exception handlers in each service
- Common business exceptions in jwt-common library
- Standardized ApiResponse format across services

## Testing

### Test Structure
- Unit tests in `src/test/java`
- Each service has `{ServiceName}ApplicationTests.java`
- Test configurations in `src/test/resources`

### Test Databases
Use separate test configurations for isolated testing environments.

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

## Security

### AuthContext System (Recommended)
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
- **AuthContextFilter**: Extracts user info from Gateway headers → AuthContext
- **UserInfo**: User data model (userId, email, role, tenantId)
- **Role**: Enum for type-safe role management (ADMIN, USER)

**Benefits:**
- No repetitive `@RequestHeader` in every controller method
- Automatic header validation and user context setup
- Clean service layer without user ID parameters
- Type-safe role management with enum
- Consistent error handling for authentication/authorization

### Role System
Hermes uses a simple 2-level role hierarchy:
- **ADMIN**: Full system access, can perform all operations
- **USER**: Standard user access, limited to user-level operations

The Role enum provides type safety and eliminates string-based role errors:
```java
// In entities - determined by isAdmin flag
boolean isAdmin = user.getIsAdmin();
Role userRole = isAdmin ? Role.ADMIN : Role.USER;

// In services - type-safe permission checking
if (!currentUser.getRole().hasPermission(Role.ADMIN)) {
    throw new InsufficientPermissionException("Admin access required");
}
```

### JWT Implementation
- Token generation/validation in jwt-common
- Multi-tenant JWT payload support
- Token blacklist service for logout
- Gateway validates JWT → injects headers → AuthContext processes

### Authentication Flow
1. Login via user-service `/api/auth/login`
2. JWT token with tenant context
3. Gateway validates tokens via user-service
4. Services extract tenant from JWT for schema routing

## Event-Driven Architecture

### Tenant Lifecycle Events
- **TENANT_CREATED**: Triggers schema creation in all services
- **TENANT_DELETED**: Triggers schema deletion in all services  
- **TENANT_UPDATED**: Updates tenant metadata

### Message Flow
tenant-service → RabbitMQ → service-specific queues → automatic schema operations

## Configuration Management

### Profiles & Environments
- Development: local application.yml files
- Production: centralized via config-server
- Secret management: application-secret.yml files

### Service-Specific Settings
Each service maintains its own `application.yml` with:
- Database connections
- Service discovery configuration  
- Multi-tenancy settings
- Custom business logic configuration

## Monitoring & Operations

### Health Checks
Standard Spring Boot Actuator endpoints available on all services.

### Logging
- Tenant context in log patterns: `[%X{tenantId:-system}]`
- Service-specific log levels configurable
- RabbitMQ operation logging for tenant events

## Common Troubleshooting

### Multi-Tenancy Issues
- Verify RabbitMQ is running: `sudo systemctl status rabbitmq-server`
- Check tenant event queues: `tenant.events.{service-name}`
- Monitor dead letter queues: `tenant.events.dlq.{service-name}`
- Ensure database users have schema creation privileges

### Service Discovery
- Ensure Eureka server is running first
- Check service registration in Eureka dashboard
- Verify `spring.application.name` is unique per service

### Database Connectivity
- Each service requires its own PostgreSQL database
- Connection strings must match service-specific database names
- Schema operations require elevated database privileges

## 기타
- 커밋 메시지는 항상 한국어로 간단 명료하게 작성하세요.
- 사용자의 명시적인 지시 없이 커밋을 하지 마세요.
- gradlew 빌드할 땐 항상 '-Dfile.encoding=UTF-8'을 붙여야 합니다 (따옴표 포함)