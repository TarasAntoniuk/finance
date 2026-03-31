# Financial Accounting System – Backend API

## Overview

**Financial Accounting System** with **Event Sourcing** for banking operations and automatic **ECB currency integration**.

**Architecture**: Modular monolith with Core (foundation) and Banking (transactions) modules.

> **Development Note:** Spare-time project demonstrating production-ready practices and enterprise patterns.

---

## 📚 Documentation

- **[Architecture](docs/ARCHITECTURE.md)** — System design, Event Sourcing, modules
- **[Changelog](docs/CHANGELOG.md)** — Version history
- **[Roadmap](docs/ROADMAP.md)** — Planned features
- **[Technology Stack](docs/TECH_STACK.md)** — Technologies and tools

---

## 🌐 Live Demo

**Frontend**: https://finance.tarasantoniuk.com/    
**API**: https://api.tarasantoniuk.com  
**Swagger**: [Interactive Docs](https://api.tarasantoniuk.com/swagger-ui/index.html)    

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
✅ **Comprehensive Test Coverage** — 903 test methods in 55 test files

### 🔧 Technical Excellence
✅ **PostgreSQL 17** with optimized queries
✅ **Testcontainers** for integration tests
✅ **N+1 Prevention** with `JOIN FETCH`
✅ **CI/CD** with GitHub Actions

---

## 🏗️ System Architecture
```
┌───────────────────────────────────────────────┐
│         External Systems                      │
│  ┌──────────────┐   ┌──────────────────┐      │
│  │ ECB API      │   │ User Browser     │      │
│  │ (Daily sync) │   │ (Swagger UI)     │      │
│  └──────┬───────┘   └────────┬─────────┘      │
└─────────┼──────────────────────┼──────────────┘
          │                      │
          ▼                      ▼
┌───────────────────────────────────────────────┐
│      Spring Boot Application                  │
│                                               │
│  ┌──────────────────────────────────────┐     │
│  │  Core Module                         │     │
│  │  - Currency & Exchange Rates         │     │
│  │  - Organizations                     │     │
│  │  - Counterparties                    │     │
│  │  - Countries                         │     │
│  │  - Accounting Policies               │     │
│  └──────────────────────────────────────┘     │
│                                               │
│  ┌──────────────────────────────────────┐     │
│  │  Banking Module                      │     │
│  │  - Banks & Bank Accounts             │     │
│  │  - Bank Receipts & Payments          │     │
│  │  - Event Sourcing                    │     │
│  │  - Financial Reports                 │     │
│  └──────────────────────────────────────┘     │
└─────────────┬─────────────────────────────────┘
              │
              ▼
┌───────────────────────────────────────────────┐
│         PostgreSQL 17                         │
│  - Transactional tables                       │
│  - Event Store (append-only)                  │
│  - 190k+ exchange rate records                │
└───────────────────────────────────────────────┘
```

*See [ARCHITECTURE.md](docs/ARCHITECTURE.md) for detailed technical documentation*

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
**Frontend**: Vanilla JavaScript (separate deployment)
**Database**: PostgreSQL 17
**Testing**: JUnit 5, Testcontainers, JaCoCo
**API**: REST, Swagger/OpenAPI 3, MapStruct
**DevOps**: Docker, GitHub Actions

---

## 📦 Latest Release

**v0.0.5** (2025-01-13) — Snapshot Validity & Bug Fixes ✅

**Added**:
- Snapshot validity tracking system (7 new files)
- Backdated transaction detection with auto-invalidation
- `BankAccountBalanceService` extracted for cleaner architecture

**Fixed**:
- N+1 query issue: 41 → 1 query using `JOIN FETCH` (~40x faster)
- Boundary condition bug: double-counted opening balance at period start

**Stats**: +1,627 / -211 lines | 903 test methods in 55 files

[Complete changelog](docs/CHANGELOG.md) | [Roadmap](docs/ROADMAP.md)

---

## Development

**Branch Strategy**: `feature/*` → `dev` → `stable`  
**Commits**: Conventional Commits (`feat:`, `fix:`, `test:`, `refactor:`, `docs:`)

---

## 👤 Contact

**Taras Antoniuk**  
🌐 [tarasantoniuk.com](https://tarasantoniuk.com/)  
📧 bronya2004@gmail.com  
🔗 [LinkedIn](https://www.linkedin.com/in/taras-antoniuk-7a550816a/)  
💻 [HackerRank](https://www.hackerrank.com/profile/bronya2004)

**Certifications:**
- SQL (Advanced)
- SQL (Intermediate)
- Java (Basic)  
*\*Java certification: HackerRank currently offers only Basic level for standard accounts*


**Skills:**
- Java: 5 stars
- SQL: 5 stars
- Problem Solving: 2 stars