# Technology Stack – Financial Accounting Prototype

---

## Backend Technologies

### Core Framework
- **Java 21**
    - Modern LTS version
    - Records, pattern matching, virtual threads support

- **Spring Boot 3.5.10**
    - Enterprise-grade framework
    - Auto-configuration
    - Production-ready features

### Security
- **Spring Security 6.x**
    - Authentication and authorization
    - SecurityFilterChain configuration
    - Method-level security (`@PreAuthorize`)
    - Custom exception handlers (401/403 JSON responses)

- **JJWT 0.12.6** (io.jsonwebtoken)
    - JWT token generation and validation
    - HMAC-SHA256 signing
    - Claims management

- **Resilience4j 2.2.0**
    - Rate limiting on authentication endpoints
    - 5 requests per 60 seconds

- **Caffeine Cache**
    - In-memory token blacklist
    - 15-minute TTL, 10K max entries
    - Used for immediate JWT invalidation on logout

### Data Access
- **Spring Data JPA**
    - Repository abstraction
    - Query derivation
    - Pagination and sorting

- **Hibernate 6.5**
    - JPA implementation
    - ORM capabilities
    - Database schema management

### Object Mapping
- **MapStruct 1.6.3**
    - Compile-time code generation
    - Type-safe DTO ↔ Entity mapping
    - Better performance than reflection-based mappers
    - Spring integration

### Build Tool
- **Maven**
    - Dependency management
    - Build lifecycle
    - Plugin ecosystem

### API & Documentation
- **Spring Web**
    - RESTful service development
    - HTTP request handling

- **SpringDoc OpenAPI 2.8.4**
    - Automatic API documentation
    - Swagger UI integration
    - OpenAPI 3 specification

### Validation
- **Spring Boot Starter Validation**
    - Bean Validation (Jakarta Validation)
    - Request validation
    - Custom validators

---

## Database

- **PostgreSQL 17**
    - Open-source RDBMS
    - ACID compliance
    - Advanced features (JSON, full-text search)
    - Excellent performance

---

## Testing

### Frameworks
- **JUnit 5**
    - Modern testing framework
    - Annotations and assertions
    - Parameterized tests

- **Spring Boot Test**
    - Spring context testing
    - MockMvc for controllers
    - Test slices (@DataJpaTest, @WebMvcTest)

- **Mockito**
    - Mocking framework for unit tests
    - Used for service layer isolation

### Integration Testing
- **Testcontainers**
    - Docker-based integration tests
    - Real PostgreSQL instance
    - Container reuse for performance (~51s total test suite)

### Coverage
- **JaCoCo 0.8.12**
    - Code coverage reports
    - Maven plugin integration
    - CI enforcement: 80% line, 75% branch coverage
    - 1120+ test methods across 67 test files

---

## DevOps & CI/CD

### Continuous Integration
- **GitHub Actions**
    - Automated test execution
    - Build verification
    - Coverage badge generation
    - Runs on push to `dev` and `stable`

### Containerization
- **Docker**
    - Production deployment with Docker Compose
    - Required for Testcontainers (integration testing)
    - Multi-container orchestration (app + database)

---

## Development Tools

### IDE
- **IntelliJ IDEA**
    - Smart code completion
    - Refactoring tools
    - Spring Boot integration
    - Database tools

### API Testing
- **Postman**
    - Manual API testing
    - Request collections
    - Environment management

- **Swagger UI**
    - Interactive API documentation
    - In-browser testing
    - Auto-generated from code

---

## Configuration Management

- **dotenv-java 3.0.0**
    - Environment variable management
    - `.properties` file support
    - Development/production separation

---

## Frontend

- **Vanilla JavaScript** (separate deployment)
    - No frameworks (plain JS, HTML, CSS)
    - Basic UI implementation by backend developer
    - Not included in this repository
    - Features: Exchange rates display, basic navigation
    - Live: https://finance.tarasantoniuk.com/

---

## Notable Decisions

### What We DON'T Use

❌ **Lombok**
- Explicit code preferred
- Better debugging
- Plain Java getters/setters

❌ **Frontend Framework**
- Vanilla JS chosen for simplicity
- Full React/Vue frontend planned for future

❌ **Kubernetes** (currently)
- Prototype phase
- Planned for production

---

## Dependencies Summary

### Production Dependencies
```
<!-- Core -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
spring-boot-starter-actuator
spring-boot-starter-aop
postgresql

<!-- Security -->
jjwt-api (0.12.6)
jjwt-impl (0.12.6)
jjwt-jackson (0.12.6)
caffeine

<!-- Rate Limiting -->
resilience4j-spring-boot3 (2.2.0)
resilience4j-ratelimiter (2.2.0)

<!-- Mapping -->
mapstruct (1.6.3)

<!-- Documentation -->
springdoc-openapi-starter-webmvc-ui (2.8.4)

<!-- Utilities -->
dotenv-java (3.0.0)
commons-lang3
```

### Test Dependencies
```
spring-boot-starter-test
spring-security-test
spring-boot-testcontainers
testcontainers-postgresql
junit-jupiter
```

### Build Plugins
```
spring-boot-maven-plugin
mapstruct-processor (1.6.3)
jacoco-maven-plugin (0.8.12)
maven-surefire-plugin (3.2.5)
plantuml-generator-maven-plugin (1.6.0)
```

---

## Version Strategy

- **Spring Boot**: Latest stable (3.5.10)
- **Java**: LTS version (21)
- **PostgreSQL**: Latest major (17)
- **Dependencies**: Regular security updates

---

## Planned Technology Additions

- Redis (caching)
- Liquibase/Flyway (database migrations)
- Kubernetes
- Monitoring tools (Prometheus, Grafana)

---

**Document Version**: 0.0.6
**Last Updated**: February 2026
