# Technology Stack – Financial Accounting Prototype

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
    - Pagination and sorting support

- **Hibernate 6.5**
    - JPA implementation
    - ORM capabilities
    - Database schema management

### Object Mapping
- **MapStruct 1.6.3**
    - Compile-time code generation
    - Type-safe mapping between DTOs and Entities
    - Better performance than reflection-based mappers
    - Spring integration support

### Build Tool
- **Maven**
    - Dependency management
    - Build lifecycle management
    - Plugin ecosystem

### API & Documentation
- **Spring Web (REST API)**
    - RESTful service development
    - HTTP request handling

- **SpringDoc OpenAPI 2.2.0**
    - Automatic API documentation generation
    - Swagger UI integration
    - OpenAPI 3 specification

### Validation
- **Spring Boot Starter Validation**
    - Bean Validation (Jakarta Validation)
    - Request validation
    - Custom validators support

---

## Database

### RDBMS
- **PostgreSQL 17**
    - Open-source relational database
    - ACID compliance
    - Advanced features (JSON, full-text search, etc.)
    - Excellent performance and reliability

---

## Testing

### Testing Frameworks
- **JUnit 5**
    - Modern testing framework
    - Annotations and assertions
    - Parameterized tests

- **Spring Boot Test**
    - Spring context testing
    - MockMvc for controller testing
    - Test slices (@DataJpaTest, @WebMvcTest)

### Integration Testing
- **Testcontainers**
    - Docker-based integration tests
    - Real PostgreSQL instance for testing
    - Ensures test isolation

---

## DevOps & CI/CD

### Continuous Integration
- **GitHub Actions**
    - Automated test execution
    - Build verification
    - Runs before merging to stable branch

### Containerization
- **Docker**
    - Used for Testcontainers
    - Required for integration testing
    - **Note**: Not used for deployment in current phase

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

### Environment Configuration
- **dotenv-java 3.0.0**
    - Environment variable management
    - `.properties` file support
    - Development/production separation

---

## Code Quality

### Testing Coverage
- **JaCoCo 0.8.10**
    - Code coverage reports
    - Maven plugin integration
    - Coverage metrics tracking

---

## Notable Architectural Decisions

### What We DON'T Use

❌ **Lombok**
- Reason: Explicit code, better debugging
- Alternative: Plain Java getters/setters/constructors

❌ **Frontend Framework** (currently)
- Reason: Backend-first approach
- Future: Will be added in later phases

❌ **Kubernetes** (currently)
- Reason: Prototype phase
- Future: Planned for production deployment

---

## Dependencies Overview

### Main Dependencies (from pom.xml)

```pom
<!-- Core -->
spring-boot-starter-web
spring-boot-starter-data-jpa
postgresql

<!-- Mapping -->
mapstruct (1.6.3)
mapstruct-processor

<!-- Documentation -->
springdoc-openapi-starter-webmvc-ui (2.2.0)

<!-- Validation -->
spring-boot-starter-validation

<!-- Testing -->
spring-boot-starter-test
spring-boot-testcontainers
testcontainers-postgresql
junit-jupiter

<!-- Utilities -->
dotenv-java (3.0.0)
commons-lang3 (3.18.0)

<!-- Code Coverage -->
jacoco-maven-plugin (0.8.10)
```

---

## Version Strategy

- **Spring Boot**: Latest stable version (3.5.5)
- **Java**: LTS version (21)
- **PostgreSQL**: Latest major version (17)
- **Dependencies**: Regular updates for security and features

---

## Future Technology Additions

Planned for future phases:
- Spring Security (authentication/authorization)
- Spring Scheduler (automatic rate updates)
- Redis (caching)
- Liquibase/Flyway (database migrations)
- Frontend framework (React/Vue/Angular)
- Kubernetes (orchestration)
- Monitoring tools (Prometheus, Grafana)

---

**Document Version**: 1.0  
**Last Updated**: October 2025