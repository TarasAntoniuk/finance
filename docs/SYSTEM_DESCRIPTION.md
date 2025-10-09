# System Description – Financial Accounting Prototype

## Purpose

This system is a prototype for **financial accounting**, focusing on **currency rate management** and backend operations.  
It demonstrates architecture, service design, and automated workflows as a foundation for future development of a broader financial system.

---

## Architecture Overview

### Backend Architecture
- **Framework**: Java + Spring Boot
- **Design**: Modular, layered architecture
- **Layers**:
    - **Controller Layer**: REST API endpoints
    - **Service Layer**: Business logic
    - **Repository Layer**: Data access with Spring Data JPA
    - **Entity Layer**: Database entities
    - **DTO Layer**: Data Transfer Objects with MapStruct mapping

### Database
- **RDBMS**: PostgreSQL 17
- **ORM**: Hibernate 6.5
- **Schema Management**: JPA/Hibernate

### Testing
- **Unit Tests**: JUnit 5
- **Integration Tests**: Testcontainers with PostgreSQL
- **CI/CD**: GitHub Actions for automated test execution

---

## Core Features Implemented

### 1. Currency Management
- CRUD operations for currencies
- Load standard currency list from file
- REST API endpoints for currency operations

### 2. Country Management
- CRUD operations for countries
- Country entity with relationships

### 3. Organization Management
- CRUD operations for organizations
- Organization entity structure

### 4. Accounting Policy Management
- Defines which currencies require exchange rate updates
- Links organizations with currency policies

### 5. Exchange Rate Management
- Store exchange rates for configured currencies
- REST API for rate queries
- **Planned**: Automatic updates from external sources (ECB, etc.)

### 6. API Documentation
- Swagger UI for interactive API exploration
- OpenAPI specification (JSON/YAML)

---

## Technology Stack

### Backend Technologies
- Java 21
- Spring Boot 3.5.5
- Spring Data JPA / Hibernate 6.5
- MapStruct 1.6.3 (DTO mapping)
- Maven (build tool)

### Testing Technologies
- JUnit 5
- Testcontainers
- Spring Boot Test

### Database
- PostgreSQL 17

### API Documentation
- SpringDoc OpenAPI 2.2.0

---

## Data Model

### Main Entities

1. **Currency**
    - Currency code (ISO)
    - Currency name
    - Symbol

2. **Country**
    - Country code
    - Country name
    - Relationships with currencies

3. **Organization**
    - Organization details
    - Accounting settings

4. **Accounting Policy**
    - Organization reference
    - Currency configuration
    - Rate update settings

5. **Exchange Rate**
    - Currency pair
    - Rate value
    - Date
    - Source

---

## API Structure

### REST Endpoints

Base URL: `http://localhost:8080`

**Currencies**: `/api/currencies`
**Countries**: `/api/countries`
**Organizations**: `/api/organizations`
**Accounting Policies**: `/api/accounting-policies`
**Exchange Rates**: `/api/exchange-rates`

See Swagger UI for complete API documentation: `http://localhost:8080/swagger-ui.html`

---

## Current Limitations

### Not Implemented
- Frontend interface
- Historical data tracking
- Automatic exchange rate updates
- User authentication/authorization
- Advanced reporting
- Multi-currency conversions

### Future Enhancements
See [FUTURE_PLANS.md](FUTURE_PLANS.md) for detailed roadmap.

---

## Design Principles

1. **Modularity**: Each module can be extended independently
2. **Separation of Concerns**: Clear layer separation
3. **Testability**: Comprehensive test coverage with unit and integration tests
4. **API-First**: RESTful API design with OpenAPI documentation
5. **Scalability**: Architecture supports future feature additions

---

## Development Status

**Current Phase**: Prototype / Minimal Viable Product  
**Focus**: Backend core functionality and architecture  
**Next Phase**: External integrations and automation

---

**Document Version**: 1.0  
**Last Updated**: October 2025