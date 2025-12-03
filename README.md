# Financial Accounting System – Backend API

## Overview

**Financial Accounting System** with **Event Sourcing** for banking operations and automatic **ECB currency integration**.

**Architecture**: Modular monolith with Core (foundation) and Banking (transactions) modules.

> **Development Note:** Spare-time project demonstrating production-ready practices and enterprise patterns.

---

## 📚 Documentation

- **[Changelog](docs/CHANGELOG.md)** — Version history
- **[Roadmap](docs/ROADMAP.md)** — Planned features
- **[Technology Stack](docs/TECH_STACK.md)** — Technologies and tools

---

## 🌐 Live Demo

**API**: https://api.tarasantoniuk.com  
**Swagger**: [Interactive Docs](https://api.tarasantoniuk.com/swagger-ui/index.html)  
**Exchange Rates**: https://tarasantoniuk.com/exchange-rates.html

![Coverage](https://raw.githubusercontent.com/TarasAntoniuk/finance/badges/jacoco.svg)
![Branches](https://raw.githubusercontent.com/TarasAntoniuk/finance/badges/branches.svg)

---

## 🎯 Key Features

### 💱 Core Module
✅ **Automatic ECB Integration** — Daily sync at 16:05 CET  
✅ **190k+ Exchange Rates** — Historical data since EUR introduction  
✅ **40+ Currencies** — ISO 4217 compliant  
✅ **Public API** — Real-time currency data

### 🏦 Banking Module
✅ **Event Sourcing** — Immutable transaction audit trail  
✅ **Bank Receipts & Payments** — 10+ transaction types each  
✅ **Document Lifecycle** — DRAFT → POST → UNPOST  
✅ **Financial Reports** — Balance & Turnover with filters  
✅ **99% Test Coverage** — 170+ tests

### 🔧 Technical Excellence
✅ **PostgreSQL 17** with optimized queries  
✅ **Testcontainers** for integration tests  
✅ **N+1 Prevention** with `@EntityGraph`  
✅ **CI/CD** with GitHub Actions

---

## 🏗️ Architecture
```
Core Module (Foundation)
├── Currency & Exchange Rates (ECB integration)
├── Organizations & Banks
└── Counterparties

Banking Module (Transactions)
├── Bank Receipts & Payments
├── Event Sourcing (immutable events)
└── Financial Reports
```

---

## 📊 System Diagram

<!-- TODO: Add high-level architecture diagram -->
![Architecture Overview](docs/diagrams/system-overview.png)

---

## 🚀 Quick Start

### Prerequisites
Java 21, Maven 3.8+, PostgreSQL 17, Docker

### Run
```bash
git clone https://github.com/TarasAntoniuk/finance.git
cd finance

# Configure: src/main/resources/env/env.dev.properties
DB_URL=jdbc:postgresql://localhost:5432/finance
DB_USERNAME=your_username
DB_PASSWORD=your_password

mvn spring-boot:run
```

**Access**: http://localhost:8080/swagger-ui/index.html

---

## 💻 Technology Stack

**Backend**: Java 21, Spring Boot 3.5.5  
**Database**: PostgreSQL 17, Flyway migrations  
**Testing**: JUnit 5, Testcontainers, JaCoCo (99% coverage)  
**API**: REST, Swagger/OpenAPI 3, MapStruct  
**DevOps**: Docker, GitHub Actions

---

## 📦 Latest Release

**v0.0.4** (December 2024) — Banking Module with Event Sourcing 🚧  
[Pull Request #10](https://github.com/TarasAntoniuk/finance/pull/10)

**Key Features**:
- Bank Receipts & Payments with full lifecycle
- Event Sourcing architecture
- Financial reports (Balance, Turnover)
- 170+ tests, 99% coverage
- Documentation in progress

[Complete changelog](docs/CHANGELOG.md) | [Roadmap](docs/ROADMAP.md)

---

## Development

**Branch Strategy**: `feature/*` → `dev` → `stable`  
**Commits**: Conventional Commits (`feat:`, `fix:`, `test:`, `refactor:`, `docs:`)

---

## 👤 Contact

**Taras Antoniuk**  
📧 bronya2004@gmail.com  
🔗 [LinkedIn](https://www.linkedin.com/in/taras-antoniuk-7a550816a/)  
💻 [HackerRank](https://www.hackerrank.com/profile/bronya2004) (5-star SQL, Java)