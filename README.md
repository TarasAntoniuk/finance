# Financial Accounting Prototype – Backend Project

## Overview

This is a **Financial Accounting Prototype** with backend focus.  
Currently implements **currency rate management**, **banking operations**, and demonstrates enterprise-grade architecture, service design, and automated workflows.

> **Important Note:**  
> This project is developed **in my spare time** as a side project.  
> Development pace depends on time availability. This is a prototype and foundation for future growth.

---

## 📚 Documentation

- **[System Description](docs/SYSTEM_DESCRIPTION.md)** — Architecture, features, data model
- **[Technology Stack](docs/TECH_STACK.md)** — Complete list of technologies and tools
- **[Release History](docs/RELEASES.md)** — Version history and changelog
- **[Future Plans](docs/FUTURE_PLANS.md)** — Roadmap and planned features

---

## 🌐 Live Demo

**Production API**: https://api.tarasantoniuk.com

Explore the interactive API documentation:  
**[Swagger UI](https://api.tarasantoniuk.com/swagger-ui/index.html)** — Try all endpoints directly in your browser

> **Note**: This is a live production environment. Please use responsibly.

---

## Current Status

**Version**: 0.0.3  
**Status**: Prototype / Active Development

![Coverage](https://raw.githubusercontent.com/TarasAntoniuk/finance/badges/jacoco.svg)
![Branches](https://raw.githubusercontent.com/TarasAntoniuk/finance/badges/branches.svg)

[![Coverage](https://raw.githubusercontent.com/TarasAntoniuk/finance/badges/jacoco.svg)](https://github.com/TarasAntoniuk/finance/actions)

### Implemented Features

#### Core Modules
✅ **Currency Management** — ISO 4217 compliant currency operations  
✅ **Country Management** — Country reference data with ISO codes  
✅ **Organization Management** — Organizational entities  
✅ **Accounting Policy Management** — Fiscal year policies per organization  
✅ **Exchange Rate Management** — Automatic daily updates from ECB (16:05 CET)  
✅ **Bank Management** — SWIFT code validation, country relationships  
✅ **Bank Account Management** — Multi-holder accounts (Organizations/Counterparties)

#### Technical Implementation
✅ REST API with Swagger/OpenAPI documentation  
✅ PostgreSQL 17 database with JPA/Hibernate  
✅ MapStruct for DTO-Entity mapping  
✅ Comprehensive test coverage (>95% branch coverage)  
✅ Unit tests (JUnit 5 + Mockito)  
✅ Integration tests (Testcontainers)  
✅ CI/CD pipeline with GitHub Actions  
✅ JaCoCo coverage reports with automated badges

### Coming Next (Version 0.0.4)
🔄 **Account Balance Tracking** — Event sourcing with immutable transactions  
🔄 **Payment Documents** — PaymentOrder, PaymentReceived, BankCommission, Transfer  
🔄 **Transaction History** — Complete audit trail with backdated changes support

### Not Implemented Yet
❌ Frontend interface  
❌ User authentication/authorization  
❌ Financial reporting  
❌ Multi-organization support

---

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 17
- Docker (for integration tests)

### Installation & Configuration
```bash
# Clone repository
git clone [repository-url]
cd finance

# Create configuration file
# src/main/resources/env/env.dev.properties
```

**Configuration file content:**
```properties
DB_URL=jdbc:postgresql://host:5432/dbname
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### Run Application
```bash
# Run application
mvn spring-boot:run

# Run tests
mvn test

# Run tests with coverage
mvn clean test jacoco:report
```

### Verify Local Installation

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs

---

## API Endpoints

### Available Resources
- `/api/currencies` — Currency management
- `/api/countries` — Country management
- `/api/organizations` — Organization management
- `/api/accounting-policies` — Accounting policy management
- `/api/exchange-rates` — Exchange rate management
- `/api/banks` — Bank management
- `/api/bank-accounts` — Bank account management

**Interactive Documentation**: [Swagger UI](https://api.tarasantoniuk.com/swagger-ui/index.html)

---

## Development Workflow

**Branch Strategy**:
- `feature/*` — New feature development
- `dev` — Development integration
- `stable` — Stable releases

**Versioning**: 0.0.x until production release (1.0.0)

**Commit Convention**: Conventional Commits
- `feat:` — New features
- `fix:` — Bug fixes
- `test:` — Test additions
- `refactor:` — Code refactoring
- `docs:` — Documentation updates

---

## Technology Highlights

- **Java 21** with modern language features
- **Spring Boot 3.5.5** enterprise framework
- **PostgreSQL 17** with advanced indexing
- **MapStruct 1.6.3** for type-safe mapping
- **Testcontainers** for production-like testing
- **JaCoCo** for comprehensive coverage reporting

---

## Contact

**Taras Antoniuk**  
📧 [bronya2004@gmail.com](mailto:bronya2004@gmail.com)  
🔗 [LinkedIn](https://www.linkedin.com/in/taras-antoniuk-7a550816a/)  
💻 [HackerRank](https://www.hackerrank.com/profile/bronya2004)

---

**Last Updated**: November 2025