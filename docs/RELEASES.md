# Release History – Financial Accounting Prototype

---

## Versioning Strategy

**Pre-production versions**: 0.0.x  
**Production release**: 1.0.0

All versions before production release are numbered as **0.0.x** where x increments with each release.
---

## Version 0.0.3 – Banking System Module

**Release Date**: November 2025  
**Status**: Current Version

### Features Implemented

#### Banking System
- ✅ Bank management (CRUD operations)
    - Full CRUD operations for banks with country relationships
    - SWIFT code validation and unique constraints
    - Bank activation/deactivation functionality
    - Search by country and active status
    - Counterparty relationship support

- ✅ Bank Account management (CRUD operations)
    - Multi-holder bank accounts (Organizations and Counterparties)
    - Account status management (Active, Inactive, Closed)
    - Default account designation per holder
    - Account number uniqueness validation
    - Filtering by holder, bank, and status

#### Technical Implementation
- ✅ RESTful API with layered architecture (Controller → Service → Repository)
- ✅ JPA/Hibernate with PostgreSQL, indexed queries for performance
- ✅ MapStruct for DTO-Entity conversions
- ✅ Jakarta Validation with custom business rules
- ✅ OpenAPI/Swagger annotations for all endpoints
- ✅ Comprehensive test coverage with Testcontainers for integration tests
- ✅ Custom exception hierarchy with proper HTTP status codes
- ✅ Discriminator pattern for account holder types
- ✅ Composite indexes on frequently queried fields (SWIFT codes, account numbers, holder identifiers)
- ✅ Immutable audit fields (createdAt, updatedAt) with JPA auditing
- ✅ Branch coverage > 95% with unit and integration tests

#### API Endpoints
- `/api/banks` - Bank management
- `/api/bank-accounts` - Bank account management

### Known Limitations
- No frontend interface
- No authentication/authorization
- No transaction history tracking
- No account balance tracking (planned for 0.0.4)

---

## Version 0.0.2 – Exchange Rate Automation

**Release Date**: November 2025  
**Status**: Current Version

### Features Implemented

#### Exchange Rate Automation
- ✅ Historical exchange rate loading from ECB (all available history)
- ✅ Batch saving of exchange rates (optimized bulk operations)
- ✅ Scheduled automatic exchange rate updates (daily at 16:05 CET)
- ✅ Integration with European Central Bank API
- ✅ Fallback mechanism for rate fetching failures

#### Code Quality & Testing
- ✅ JaCoCo test coverage reporting (80% line coverage, 75% branch coverage)
- ✅ Automated coverage badge generation in CI/CD
- ✅ Separate workflow for test coverage monitoring
- ✅ Coverage enforcement in CI profile

#### Code Quality & Testing
- ✅ JaCoCo test coverage reporting (80% line coverage, 75% branch coverage)
- ✅ Automated coverage badge generation in CI/CD

### Known Limitations
- No frontend interface
- No authentication/authorization
- Limited to currencies supported by ECB
- No retry mechanism for failed scheduled updates

---

## Version 0.0.1 – Initial Release

**Release Date**: October 2025  
**Status**: Previous Version

### Features Implemented

#### Core Entities
- ✅ Currency management (CRUD)
- ✅ Country management (CRUD)
- ✅ Organization management (CRUD)
- ✅ Accounting policy management (CRUD)
- ✅ Exchange rate management (CRUD)

#### Technical Implementation
- ✅ REST API with Spring Boot 3.5.5
- ✅ PostgreSQL 17 database
- ✅ MapStruct for DTO mapping
- ✅ JUnit 5 + Testcontainers tests
- ✅ Swagger/OpenAPI documentation
- ✅ GitHub Actions CI/CD

#### API Endpoints
- `/api/currencies`
- `/api/countries`
- `/api/organizations`
- `/api/accounting-policies`
- `/api/exchange-rates`

### Known Limitations
- No frontend interface
- Manual exchange rate entry only
- No historical data tracking
- No authentication/authorization

---

## Version 0.0.3 (Planned)

**Expected**: Q1 2026

### Planned Features
- 🔄 Code refactoring and improvements
- 🔄 API endpoints redesign

---

## Development Branches

**Branch Structure**:
- `feature/*` — Feature development branches
- `dev` — Development integration branch
- `stable` — Stable release branch

**Workflow**:
1. Feature development on `feature/*` branches
2. Merge to `dev` after review
3. CI/CD tests run automatically
4. Merge to `stable` when ready for release

---

## Changelog Format

Each release includes:
- **Version number** (0.0.x until production)
- **Release date**
- **Features**: ✅ completed, 🔄 in progress, ❌ removed
- **Bug fixes**
- **Breaking changes** (if any)
- **Known issues**

---

## Version History

| Version | Date     | Status  | Key Features                           |
|---------|----------|---------|----------------------------------------|
| 0.0.1   | Oct 2025 | Released| Initial CRUD operations                |
| 0.0.2   | Nov 2025 | Current | Automatic exchange rate updates        |
| 0.0.3   | Q1 2026  | Planned | Account management                     |
| 1.0.0   | TBD      | Planned | Production-ready release               |

---

## Version 0.0.2 – Detailed Changelog

### Added
- Historical exchange rate loading from ECB API
- Batch saving operations for exchange rates
- Scheduled task for daily exchange rate updates (16:05 CET)
- JaCoCo test coverage with automated badge generation
- Separate GitHub Actions workflow for test coverage
- Coverage enforcement in CI profile (80% line, 75% branch)

---

## Version 0.0.1 – Detailed Changelog

### Added
- Initial project setup with Spring Boot 3.5.5
- PostgreSQL 17 database integration
- All core entities (Currency, Country, Organization, Accounting Policy, Exchange Rate)
- REST controllers for all entities
- Service layer implementation
- DTO mapping with MapStruct
- Unit tests with JUnit 5
- Integration tests with Testcontainers
- Swagger/OpenAPI documentation
- GitHub Actions CI/CD workflow
- Project documentation

### Technical Stack
- Java 21
- Spring Boot 3.5.5
- PostgreSQL 17
- MapStruct 1.6.3
- JUnit 5
- Testcontainers
- Maven

---

## Breaking Changes Policy

**Before version 1.0.0**:
- Breaking changes may occur in any 0.0.x release
- API is subject to change during prototype phase

**After version 1.0.0**:
- Will follow semantic versioning (MAJOR.MINOR.PATCH)
- Breaking changes only in major versions

---

**Document Version**: 0.0.2  
**Last Updated**: November 2025