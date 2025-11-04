# Financial Accounting Prototype – Backend Project

## Overview

This is the **first version of my Financial Accounting Prototype**.  
Currently, it focuses on **currency rate management** and demonstrates backend architecture, service design, and automated workflows.

> **Important Note:**  
> This project is developed **in my spare time** as a side project.  
> Development pace depends on time availability. This is a prototype and minimal model for future growth.

---

## 📚 Documentation

- **[System Description](docs/SYSTEM_DESCRIPTION.md)** — Architecture, features, data model
- **[Technology Stack](docs/TECH_STACK.md)** — Complete list of technologies and tools
- **[Release History](docs/RELEASES.md)** — Version history and changelog
- **[Future Plans](docs/FUTURE_PLANS.md)** — Roadmap and planned features

---

## Current Status

**Version**: 0.0.2  
**Status**: Prototype / Minimal Model

![Coverage](https://raw.githubusercontent.com/TarasAntoniuk/finance-core/badges/jacoco.svg)
![Branches](https://raw.githubusercontent.com/TarasAntoniuk/finance-core/badges/branches.svg)

[![Coverage](https://raw.githubusercontent.com/TarasAntoniuk/finance-core/badges/jacoco.svg)](https://github.com/TarasAntoniuk/finance-core/actions)

### Implemented
✅ REST API for currencies, countries, organizations, accounting policies, exchange rates  
✅ PostgreSQL database with JPA/Hibernate  
✅ Unit and integration tests (JUnit 5 + Testcontainers) with **80% line coverage**  
✅ CI/CD pipeline with GitHub Actions  
✅ API documentation (Swagger/OpenAPI)

### Not Implemented Yet
❌ Frontend (completely absent)  
❌ Historical data tables  
❌ Automatic exchange rate updates (first priority)

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
cd finance-core

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
```

### Verify Installation

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs

---

## Development Workflow

**Branches**:
- `feature/*` — New features
- `dev` — Development integration
- `stable` — Stable releases

**Versioning**: 0.0.x until production release

---

## Contact

**Taras Antoniuk**  
📧 [bronya2004@gmail.com](mailto:bronya2004@gmail.com)  
🔗 [LinkedIn](https://www.linkedin.com/in/taras-antoniuk-7a550816a/)  
💻 [HackerRank](https://www.hackerrank.com/profile/bronya2004)

---

**Last Updated**: October 2025