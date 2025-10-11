# Technology Stack – Financial Accounting Prototype

---

## Backend Technologies

### Core Framework
- **Java 21**
    - Modern LTS version
    - Records, pattern matching, virtual threads support

- **Spring Boot 3.5.5**
    - Enterprise-grade framework
    - Auto-configuration
    - Production-ready features

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

- **SpringDoc OpenAPI 2.2.0**
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

### Integration Testing
- **Testcontainers**
    - Docker-based integration tests
    - Real PostgreSQL instance
    - Test isolation

---

## DevOps & CI/CD

### Continuous Integration
- **GitHub Actions**
    - Automated test execution
    - Build verification
    - Runs on push to `dev` and `stable`

### Containerization
- **Docker**
    - Required for Testcontainers
    - Integration testing only
    - **Note**: Not used for deployment yet

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

## Code Quality

- **JaCoCo 0.8.10**
    - Code coverage reports
    - Maven plugin integration
    - Coverage metrics tracking

---

## Notable Decisions

### What We DON'T Use

❌ **Lombok**
- Explicit code preferred
- Better debugging
- Plain Java getters/setters

❌ **Frontend Framework** (currently)
- Backend-first approach
- Planned for future

❌ **Kubernetes** (currently)
- Prototype phase
- Planned for production

---

## Dependencies Summary

### Production Dependencies
```mvn
<!-- Core -->
spring-boot-starter-web
spring-boot-starter-data-jpa
postgresql

<!-- Mapping -->
mapstruct (1.6.3)

<!-- Documentation -->
springdoc-openapi-starter-webmvc-ui (2.2.0)

<!-- Validation -->
spring-boot-starter-validation

<!-- Utilities -->
dotenv-java (3.0.0)
commons-lang3 (3.18.0)
```

### Test Dependencies
```mvn
spring-boot-starter-test
spring-boot-testcontainers
testcontainers-postgresql
junit-jupiter
```

### Build Plugins
```mvn
spring-boot-maven-plugin
mapstruct-processor
jacoco-maven-plugin (0.8.10)
```

---

## Version Strategy

- **Spring Boot**: Latest stable (3.5.5)
- **Java**: LTS version (21)
- **PostgreSQL**: Latest major (17)
- **Dependencies**: Regular security updates

---

## Planned Technology Additions

See [FUTURE_PLANS.md](FUTURE_PLANS.md) for:
- Spring Security
- Spring Scheduler
- Redis (caching)
- Liquibase/Flyway (database migrations)
- Frontend framework
- Kubernetes
- Monitoring tools (Prometheus, Grafana)

---

**Document Version**: 0.0.1  
**Last Updated**: October 2025