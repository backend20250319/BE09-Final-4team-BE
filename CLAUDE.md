# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Hermes is a Spring Boot microservices system implementing a multi-tenant architecture with schema-per-tenant strategy. It provides enterprise workforce management capabilities including user management, organization structure, attendance tracking, document approval, and news crawling.

## Architecture

### Microservices Structure
- **config-server** (port 8888): Spring Cloud Config Server for centralized configuration
- **discovery-server** (port 8761): Eureka service discovery 
- **gateway-server** (port 9000): Spring Cloud Gateway for API routing and authentication
- **user-service** (port 8081): User authentication, authorization, and management
- **org-service** (port 8083): Organization hierarchy and employee assignment management
- **attendance-service** (port 8082): Employee attendance tracking and work hour management  
- **news-crawler-service** (port 8083): News article crawling and management
- **tenant-service** (port 8083): Multi-tenant management and schema operations
- **approval-service** (port 8084): Document approval workflow and template management
- **companyinfo-service** (port 8084): Company information and settings management
- **leave-service** (port 8087): Employee leave request and management
- **communication-service** (port 8086): File transfer and communication utilities

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
- **External Config Repo**: https://github.com/backend20250319/hermes-config.git

### Service Discovery
All services register with Eureka at `http://localhost:8761/eureka`

### Gateway Routing
- `/api/users/**` → user-service  
- `/api/news/**` → news-crawler-service
- `/api/approval/**` → approval-service

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

**Configuration Example:**
```yaml
jwt:
  secret: your-secret-key-base64
  expiration-time: 3600000  # 1 hour
  refresh-expiration: 604800000  # 7 days
```

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
- **AuthContextFilter**: Extracts user info from Gateway headers → AuthContext
- **UserInfo**: User data model (userId, email, role, tenantId)
- **Role**: Enum for type-safe role management (ADMIN, USER)

### Authentication Flow
1. Login via user-service `/api/auth/login`
2. JWT token with tenant context
3. Gateway validates tokens via user-service and injects user headers
4. Services extract user info via AuthContextFilter → AuthContext

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

## Common Troubleshooting

### Multi-Tenancy Issues
- Verify RabbitMQ is running: `sudo systemctl status rabbitmq-server`
- Check tenant event queues: `tenant.events.{service-name}`
- Monitor dead letter queues: `tenant.events.dlq.{service-name}`
- Ensure database users have schema creation privileges

### Service Discovery
- Ensure Eureka server is running first
- Check service registration in Eureka dashboard: `http://localhost:8761`
- Verify `spring.application.name` is unique per service

## Development Guidelines

- **Commit messages**: Always write in Korean, keep them concise and clear
- **No automatic commits**: Never commit without explicit user instruction
- **Encoding**: Always use `-Dfile.encoding=UTF-8` when building with gradlew
- **Selective building**: Only build modified modules when testing, not the entire project
- **Zero Configuration**: Prefer auto-configuration starters over manual bean registration