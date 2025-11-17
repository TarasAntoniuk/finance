# System Description – Financial Accounting Prototype

## Purpose

This system is a prototype for **financial accounting**, focusing on **currency rate management**, **banking operations**, and backend services.  
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
- **Discriminator Pattern**: Multi-type entity handling

---

## Core Features

### 1. Currency Management
**Purpose**: Manage currency reference data

- Create, read, update, delete currencies
- Load standard currency list from file
- Currency code validation (ISO 4217 standard)

**Entities**: `Currency`

### 2. Country Management
**Purpose**: Manage country reference data

- CRUD operations for countries
- Country information with ISO codes

**Entities**: `Country`

### 3. Organization Management
**Purpose**: Manage organizational entities

- Store organization details
- Link organizations to countries

**Entities**: `Organization`

### 4. Accounting Policy Management
**Purpose**: Define accounting rules and methods per organization and fiscal year

- Configure accounting methods (depreciation, inventory, revenue recognition, VAT)
- Set fiscal year start month
- Define base currency for each policy
- Track accounting policies over time

**Entities**: `AccountingPolicy`

### 5. External Exchange Rate Management
**Purpose**: Store and manage currency exchange rates from external sources

- Track rates by date, currency pair, and source
- Store historical rates
- **Automatic daily updates from ECB (16:05 CET)**
- **Historical data loading from ECB**
- **Batch operations support**

**Entities**: `ExternalExchangeRate`

### 6. Bank Management
**Purpose**: Manage banking institutions

- Full CRUD operations for banks
- SWIFT code validation and unique constraints
- Country relationships
- Bank activation/deactivation
- Search by country and active status
- Counterparty relationships

**Entities**: `Bank`

### 7. Bank Account Management
**Purpose**: Manage bank accounts for organizations and counterparties

- Multi-holder bank accounts (Organizations and Counterparties)
- Account status management (Active, Inactive, Closed)
- Default account designation per holder
- Account number uniqueness validation
- Filtering by holder, bank, and status
- Discriminator pattern for holder types

**Entities**: `BankAccount`

---

## Data Model

### Entity Relationships

```
Country ──→ Organization ──→ AccountingPolicy ──→ Currency (base currency)
    ↓              ↓
   Bank      BankAccount (holder: ORGANIZATION)
    ↓              ↓
BankAccount    Currency
(bank accounts)

Counterparty ──→ BankAccount (holder: COUNTERPARTY)
```

### Key Entities

**Currency**
- `id` (Long, PK) — Primary key
- `code` (String, 3 chars, UNIQUE, NOT NULL) — ISO 4217 alphabetic code (USD, EUR, UAH)
- `numericCode` (String, 3 chars, UNIQUE, NOT NULL) — ISO 4217 numeric code (840, 978, 980)
- `name` (String, 100 chars, NOT NULL) — Full name (US Dollar, Euro, Ukrainian Hryvnia)
- `symbol` (String, 10 chars) — Currency symbol ($, €, ₴)
- `minorUnit` (Integer) — Number of decimal places (2 for most currencies)
- `isActive` (Boolean, NOT NULL, DEFAULT true) — Active/inactive flag
- `createdAt` (LocalDateTime, NOT NULL, immutable) — Creation timestamp
- `updatedAt` (LocalDateTime) — Last update timestamp

**Country**
- `id` (Long, PK) — Primary key
- `name` (String, 100 chars, UNIQUE, NOT NULL) — Country name
- `isoCode` (String, 3 chars, UNIQUE, NOT NULL) — ISO 3166-1 alpha-3 code
- `phoneCode` (String, 10 chars) — International phone code
- `createdAt` (LocalDateTime, NOT NULL, immutable) — Creation timestamp
- `updatedAt` (LocalDateTime) — Last update timestamp

**Organization**
- `id` (Long, PK) — Primary key
- `name` (String, 200 chars, NOT NULL) — Organization name
- `registrationNumber` (String, 50 chars, UNIQUE) — Registration/business number
- `vatNumber` (String, 50 chars) — VAT registration number
- `address` (String, 500 chars) — Physical address
- `email` (String, 100 chars) — Contact email
- `phone` (String, 20 chars) — Contact phone
- `country_id` (Long, FK, NOT NULL) — Reference to Country
- `createdAt` (LocalDateTime, NOT NULL, immutable) — Creation timestamp
- `updatedAt` (LocalDateTime) — Last update timestamp

**AccountingPolicy**
- `id` (Long, PK) — Primary key
- `organization_id` (Long, FK, NOT NULL) — Reference to Organization
- `year` (Integer, NOT NULL) — Fiscal year
- `currency_id` (Long, FK, NOT NULL) — Base currency for the policy (reference to Currency)
- `fiscalYearStartMonth` (Integer, 1-12, DEFAULT 1) — Fiscal year start month
- `depreciationMethod` (String, 50 chars) — Depreciation method (STRAIGHT_LINE, DECLINING_BALANCE, etc.)
- `inventoryValuationMethod` (String, 50 chars) — Inventory valuation (FIFO, LIFO, WEIGHTED_AVERAGE)
- `revenueRecognitionMethod` (String, 50 chars) — Revenue recognition (ACCRUAL, CASH)
- `vatAccountingMethod` (String, 50 chars) — VAT method (INVOICE, PAYMENT)
- `isActive` (Boolean, NOT NULL, DEFAULT true) — Active/inactive flag
- `notes` (Text) — Additional notes
- `createdAt` (LocalDateTime, NOT NULL, immutable) — Creation timestamp
- `updatedAt` (LocalDateTime) — Last update timestamp
- `createdBy` (String, 100 chars) — User who created
- `updatedBy` (String, 100 chars) — User who last updated
- **Unique constraint**: (organization_id, year)

**ExternalExchangeRate**
- `id` (Long, PK) — Primary key
- `exchangeDate` (LocalDate, NOT NULL) — Date of exchange rate
- `currency_from_id` (Long, FK, NOT NULL) — Source currency (reference to Currency)
- `currency_to_id` (Long, FK, NOT NULL) — Target currency (reference to Currency)
- `rate` (BigDecimal, precision 19, scale 6, NOT NULL) — Exchange rate value
- `source` (String, 100 chars, NOT NULL) — Rate source (ECB, NBU, MONOBANK, PRIVATBANK, etc.)
- `sourceUrl` (String, 500 chars) — URL of the rate source
- `isActive` (Boolean, NOT NULL, DEFAULT true) — Active/inactive flag
- `createdAt` (LocalDateTime, NOT NULL, immutable) — Creation timestamp
- `updatedAt` (LocalDateTime) — Last update timestamp
- **Unique constraint**: (exchangeDate, currency_from_id, currency_to_id, source)

**Bank**
- `id` (Long, PK) — Primary key
- `name` (String, 200 chars, NOT NULL) — Bank name
- `swiftCode` (String, 11 chars, UNIQUE, NOT NULL) — SWIFT/BIC code
- `country_id` (Long, FK, NOT NULL) — Reference to Country
- `counterparty_id` (Long, FK) — Reference to Counterparty (optional)
- `address` (String, 500 chars) — Bank address
- `phoneNumber` (String, 20 chars) — Contact phone
- `website` (String, 200 chars) — Bank website
- `isActive` (Boolean, NOT NULL, DEFAULT true) — Active/inactive flag
- `createdAt` (LocalDateTime, NOT NULL, immutable) — Creation timestamp
- `updatedAt` (LocalDateTime) — Last update timestamp
- **Index**: swiftCode (unique)

**BankAccount**
- `id` (Long, PK) — Primary key
- `accountNumber` (String, 34 chars, UNIQUE, NOT NULL) — Bank account number (IBAN format)
- `holderType` (String, 50 chars, NOT NULL) — Discriminator: ORGANIZATION or COUNTERPARTY
- `holderId` (Long, NOT NULL) — Reference to Organization or Counterparty
- `bank_id` (Long, FK, NOT NULL) — Reference to Bank
- `currency_id` (Long, FK, NOT NULL) — Account currency (reference to Currency)
- `accountName` (String, 200 chars) — Account description/name
- `status` (String, 20 chars) — Account status: ACTIVE, INACTIVE, CLOSED
- `isDefault` (Boolean, DEFAULT false) — Default account flag for holder
- `createdAt` (LocalDateTime, NOT NULL, immutable) — Creation timestamp
- `updatedAt` (LocalDateTime) — Last update timestamp
- **Unique constraint**: accountNumber
- **Composite index**: (holderType, holderId) for fast holder lookups
- **Index**: bank_id, status

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
| Banks               | `/api/banks`               | GET, POST, PUT, DELETE, PATCH |
| Bank Accounts       | `/api/bank-accounts`       | GET, POST, PUT, DELETE, PATCH |

**API Documentation**: Available via Swagger UI at `/swagger-ui.html`

---

## Testing Strategy

### Unit Tests
- Service layer business logic
- Validation rules
- Mapper functionality (>95% branch coverage)
- JUnit 5 + Mockito framework

### Integration Tests
- Full API endpoint testing
- Database operations
- Real PostgreSQL via Testcontainers
- Repository query testing
- End-to-end scenarios

### Test Coverage
- JaCoCo reports generated on each build
- Minimum requirements: 80% line coverage, 75% branch coverage
- Current achievement: >95% branch coverage for core modules

### CI/CD
- Automated test execution on every push
- Tests must pass before merge to `dev`
- GitHub Actions workflow
- Separate coverage reporting workflow

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
- ❌ User authentication/authorization
- ❌ Account balance tracking
- ❌ Transaction processing
- ❌ Financial reporting
- ❌ Payment document management
- ❌ Audit logging

### Technical Debt
- Basic error handling (needs improvement)
- No caching layer
- No database migrations tool (Liquibase/Flyway)

---

## Design Principles

1. **Modularity**: Independent, loosely coupled modules
2. **Testability**: Comprehensive test coverage (>95% branch coverage)
3. **Maintainability**: Clean code, clear structure
4. **Scalability**: Architecture supports growth
5. **API-First**: RESTful design with OpenAPI documentation
6. **Simplicity**: No unnecessary complexity
7. **Data Integrity**: Database constraints and validation
8. **Immutability**: Audit fields (createdAt, updatedAt) with JPA auditing

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

**Current Approach**:
- Database indexes on frequently queried fields
- Composite indexes for complex queries
- Optimistic locking preparation

**Future Optimizations**:
- Database query optimization
- Caching layer (Redis)
- Connection pooling tuning
- API response pagination
- Asynchronous processing

---

**Document Version**: 0.0.3  
**Last Updated**: November 2025