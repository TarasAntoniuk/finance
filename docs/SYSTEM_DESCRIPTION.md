# System Description – Financial Accounting Prototype

## Purpose

This system is a prototype for **financial accounting**, focusing on **currency rate management** and backend operations.  
It demonstrates architecture, service design, and automated workflows as a foundation for future development.

> For technology details see [TECH_STACK.md](TECH_STACK.md)  
> For development plans see [FUTURE_PLANS.md](FUTURE_PLANS.md)

---

## Architecture Overview

### Layered Architecture
```
┌─────────────────────────────────┐
│     Controller Layer (REST)     │  ← API endpoints
├─────────────────────────────────┤
│       Service Layer             │  ← Business logic
├─────────────────────────────────┤
│     Repository Layer (JPA)      │  ← Data access
├─────────────────────────────────┤
│       Entity Layer              │  ← Domain models
└─────────────────────────────────┘
           ↕
    PostgreSQL Database
```

### Key Design Patterns
- **DTO Pattern**: Entity ↔ DTO mapping with MapStruct
- **Repository Pattern**: Spring Data JPA repositories
- **Service Layer**: Business logic separation
- **REST API**: RESTful endpoint design

---

## Core Features

### 1. Currency Management
**Purpose**: Manage currency reference data

- Create, read, update, delete currencies
- Load standard currency list from file
- Currency code validation (ISO standard)

**Entities**: `Currency`

### 2. Country Management
**Purpose**: Manage country reference data

- CRUD operations for countries
- Country-currency relationships

**Entities**: `Country`

### 3. Organization Management
**Purpose**: Manage organizational entities

- Store organization details
- Link to accounting policies

**Entities**: `Organization`

### 4. Accounting Policy Management
**Purpose**: Define currency handling rules per organization

- Configure which currencies need exchange rates
- Set rate update preferences
- Organization-specific settings

**Entities**: `AccountingPolicy`

### 5. Exchange Rate Management
**Purpose**: Store and manage currency exchange rates

- Manual rate entry
- Query rates by date and currency pair
- **Future**: Automatic updates from external sources

**Entities**: `ExchangeRate`

---

## Data Model

### Entity Relationships

```
Organization ──┬─→ AccountingPolicy ──→ Currency
               │
               └─→ Account (future)

ExchangeRate ──→ Currency (from/to)

Country ──→ Currency
```

### Key Entities

**Currency**
- Code (ISO 4217, 3 characters)
- Name
- Symbol
- Numeric code

**Country**
- Country code
- Country name
- Currency reference

**Organization**
- Organization name
- Tax ID
- Address details
- Contact information

**AccountingPolicy**
- Organization reference
- Base currency
- Rate update settings
- Configured currencies

**ExchangeRate**
- Source currency
- Target currency
- Rate value
- Date
- Source (manual/automatic)

---

## API Structure

**Base URL**: `http://localhost:8080`

### Available Endpoints

| Resource            | Endpoint                   | Methods                |
|---------------------|----------------------------|------------------------|
| Currencies          | `/api/currencies`          | GET, POST, PUT, DELETE |
| Countries           | `/api/countries`           | GET, POST, PUT, DELETE |
| Organizations       | `/api/organizations`       | GET, POST, PUT, DELETE |
| Accounting Policies | `/api/accounting-policies` | GET, POST, PUT, DELETE |
| Exchange Rates      | `/api/exchange-rates`      | GET, POST, PUT, DELETE |

**API Documentation**: Available via Swagger UI at `/swagger-ui.html`

---

## Testing Strategy

### Unit Tests
- Service layer business logic
- Validation rules
- Mapper functionality
- JUnit 5 framework

### Integration Tests
- Full API endpoint testing
- Database operations
- Real PostgreSQL via Testcontainers
- End-to-end scenarios

### CI/CD
- Automated test execution on every push
- Tests must pass before merge to `dev`
- GitHub Actions workflow

---

## Development Workflow

### Branch Structure
- **`feature/*`** — New feature development
- **`dev`** — Integration and testing
- **`stable`** — Production-ready code

### Development Process
1. Create feature branch from `dev`
2. Implement feature with tests
3. Push and create pull request
4. CI/CD runs automated tests
5. Code review
6. Merge to `dev`
7. After validation → merge to `stable`

---

## Current Limitations

### Not Yet Implemented
- ❌ Frontend interface
- ❌ Historical data tables
- ❌ Automatic exchange rate updates
- ❌ User authentication/authorization
- ❌ Account and balance management
- ❌ Transaction processing
- ❌ Financial reporting
- ❌ Multi-currency operations

### Technical Debt
- Basic error handling (needs improvement)
- Limited validation rules
- No caching layer
- No database migrations tool

---

## Design Principles

1. **Modularity**: Independent, loosely coupled modules
2. **Testability**: Comprehensive test coverage
3. **Maintainability**: Clean code, clear structure
4. **Scalability**: Architecture supports growth
5. **API-First**: RESTful design with documentation
6. **Simplicity**: No unnecessary complexity

---

## Security Considerations

**Current State**: No security implementation

**Planned** (see [FUTURE_PLANS.md](FUTURE_PLANS.md)):
- Spring Security integration
- JWT/session authentication
- Role-based access control
- API endpoint protection
- Audit logging

---

## Performance Considerations

**Current Approach**: Basic implementation

**Future Optimizations**:
- Database query optimization
- Caching layer (Redis)
- Connection pooling tuning
- API response pagination
- Asynchronous processing

---

**Document Version**: 0.0.1  
**Last Updated**: October 2025