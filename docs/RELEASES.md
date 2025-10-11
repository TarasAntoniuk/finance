# Release History – Financial Accounting Prototype

---

## Versioning Strategy

**Pre-production versions**: 0.0.x  
**Production release**: 1.0.0

All versions before production release are numbered as **0.0.x** where x increments with each release.

---

## Version 0.0.1 – Initial Release

**Release Date**: October 2025  
**Status**: Current Version

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

## Version 0.0.2 (Planned)

**Expected**: Q1 2026

### Planned Features
- 🔄 Automatic exchange rate updates from ECB
- 🔄 Scheduled daily rate fetching
- 🔄 Exchange rate history table
- 🔄 Improved test coverage (80%+)
- 🔄 Enhanced error handling

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

| Version | Date     | Status  | Key Features                    |
|---------|----------|---------|---------------------------------|
| 0.0.1   | Oct 2025 | Current | Initial CRUD operations         |
| 0.0.2   | Q1 2026  | Planned | Automatic exchange rate updates |
| 0.0.3   | TBD      | Planned | Account management              |
| 1.0.0   | TBD      | Planned | Production-ready release        |

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

**Document Version**: 0.0.1  
**Last Updated**: October 2025