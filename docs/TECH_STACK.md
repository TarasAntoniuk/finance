# Technology Stack – Financial Accounting Prototype

---

## Backend Technologies

### Core Framework
- **Java 21**
    - Modern LTS version
    - Records, pattern matching, virtual threads support

- **Spring Boot 4.2.0**
    - Enterprise-grade framework
    - Auto-configuration
    - Production-ready features

### Security
- **Spring Security 7.x**
    - Authentication and authorization
    - SecurityFilterChain configuration
    - Method-level security (`@PreAuthorize`)
    - Custom exception handlers (401/403 JSON responses)

- **JJWT 0.13.0** (io.jsonwebtoken)
    - JWT token generation and validation
    - HMAC-SHA256 signing
    - Claims management

- **Resilience4j 2.4.0**
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

- **Hibernate 7.4**
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

- **SpringDoc OpenAPI 3.2.1**
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
- **JUnit Jupiter 6**
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
    - One shared container for the whole suite

### Coverage
- **JaCoCo 0.8.16**
    - Code coverage reports
    - Maven plugin integration
    - Gate in the `ci` Maven profile: 80% line, 75% branch coverage, enforced in CI
    - 1240+ test methods across 78 test files

---

## DevOps & CI/CD

### Continuous Integration
- **GitHub Actions**
    - `Test Coverage` workflow on every pull request and push to `dev`: `mvn clean verify -Pci`, coverage badge
    - `CI/CD Deploy` workflow on push to `dev`: tests, JAR build, deploy over SSH, health check

### Containerization
- **Docker**
    - Production: the app container (`finance-app`, `stable` profile) behind shared nginx and PostgreSQL from the `server-prod` repository
    - Required for Testcontainers (integration testing)

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
spring-boot-starter-webmvc
spring-boot-starter-restclient
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
spring-boot-starter-actuator
spring-boot-starter-aspectj
postgresql

<!-- Security -->
jjwt-api (0.13.0)
jjwt-impl (0.13.0)
jjwt-jackson (0.13.0)
caffeine

<!-- Rate Limiting -->
resilience4j-spring-boot4 (2.4.0)
resilience4j-ratelimiter (2.4.0)

<!-- Mapping -->
mapstruct (1.6.3)

<!-- Documentation -->
springdoc-openapi-starter-webmvc-ui (3.2.1)

<!-- Utilities -->
dotenv-java (3.0.0)
```

### Test Dependencies
```
spring-boot-starter-test
spring-boot-starter-webmvc-test
spring-boot-testcontainers
testcontainers-postgresql
testcontainers-junit-jupiter
```

### Build Plugins
```
spring-boot-maven-plugin
mapstruct-processor (1.6.3)
jacoco-maven-plugin (0.8.16)
maven-surefire-plugin (managed by Spring Boot)
plantuml-generator-maven-plugin (1.6.0)
```

---

## Version Strategy

- **Spring Boot**: Latest stable (4.2.0)
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

**Document Version**: 0.0.8-SNAPSHOT
**Last Updated**: November 2026
