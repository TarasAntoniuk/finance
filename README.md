# Financial Accounting System – Backend API

## Overview

**Financial Accounting System** with **Event Sourcing** for banking operations, **JWT authentication**, and automatic **ECB currency integration**.

**Architecture**: Modular monolith with Core (foundation), Banking (transactions), and Security (authentication & authorization) modules.

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
✅ **Event Sourcing** — Every posting and reversal recorded; corrections never delete history
✅ **Bank Receipts & Payments** — 7 receipt types, 10 payment types
✅ **Document Lifecycle** — DRAFT → POSTED → (unpost) DRAFT, with row locks against concurrent posting
✅ **Financial Reports** — Balance & Turnover with filters
✅ **Balance Snapshots** — Daily snapshots, invalidated by any backdated event and rebuilt nightly

### 🔒 Security Module
✅ **JWT Authentication** — Access tokens (15 min) + refresh tokens (7 days)
✅ **Google Sign-In** — Google ID token verification with GUEST auto-provisioning
✅ **Role-Based Access Control** — ADMIN, USER, GUEST roles
✅ **Organization-Scoped Reads** — Service-layer filtering closes cross-tenant data access
✅ **Account Lockout** — 5 failed attempts = 30-minute lock
✅ **Rate Limiting** — 5 requests/60s per client address on register, login and Google sign-in
✅ **HttpOnly Secure Cookies** — Refresh tokens via SameSite=Strict cookies
✅ **Token Blacklist** — Immediate token invalidation on logout, persisted to DB
✅ **User Revocation** — Revoked users rejected at the JWT filter
✅ **Security Audit Logging** — Login, registration, lockout events persisted
✅ **Error Contract** — 401/403 split, 400/415/409 for client errors, generic 500 with server-side logging

### 🔧 Technical Excellence
✅ **1240+ test methods** across 78 test files, including concurrency tests against PostgreSQL
✅ **PostgreSQL 17** with optimized queries
✅ **Testcontainers** for integration tests
✅ **N+1 Prevention** with `JOIN FETCH`
✅ **CI/CD** with GitHub Actions
✅ **JaCoCo Coverage** — 80% line, 75% branch enforcement

---

## 🏗️ System Architecture
```
                         External Systems
          +--------------+   +------------------+
          | ECB API      |   | User Browser     |
          | (Daily sync) |   | (Swagger UI)     |
          +------+-------+   +--------+---------+
                 |                     |
                 v                     v
+-----------------------------------------------+
|      Spring Boot Application                  |
|                                               |
|  +------------------------------------------+ |
|  |  Security Module                         | |
|  |  - JWT Authentication (HMAC-SHA256)      | |
|  |  - Role-Based Authorization (RBAC)       | |
|  |  - Account Lockout & Rate Limiting       | |
|  |  - Refresh Token Rotation                | |
|  +------------------------------------------+ |
|                                               |
|  +------------------------------------------+ |
|  |  Core Module                              | |
|  |  - Currency & Exchange Rates              | |
|  |  - Organizations & Countries              | |
|  |  - Counterparties                         | |
|  |  - Accounting Policies                    | |
|  +------------------------------------------+ |
|                                               |
|  +------------------------------------------+ |
|  |  Banking Module                           | |
|  |  - Banks & Bank Accounts                  | |
|  |  - Bank Receipts & Payments               | |
|  |  - Event Sourcing (posting events)        | |
|  |  - Financial Reports                      | |
|  +------------------------------------------+ |
+-------------------+---------------------------+
                    |
                    v
+-----------------------------------------------+
|         PostgreSQL 17                         |
|  - Transactional tables                       |
|  - Event store (reversals, no deletes)        |
|  - User & refresh token tables                |
|  - 190k+ exchange rate records                |
+-----------------------------------------------+
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
JWT_SECRET=your-secret-key-min-32-characters-long

mvn spring-boot:run
```

**Access**: http://localhost:8080/swagger-ui/index.html

---

## 💻 Technology Stack

**Backend**: Java 21, Spring Boot 4.2.0, Spring Security 7.x
**Security**: JWT (JJWT 0.13.0), Resilience4j, Caffeine cache
**Database**: PostgreSQL 17
**Testing**: JUnit Jupiter 6, Testcontainers, JaCoCo (80% line / 75% branch)
**API**: REST, Swagger/OpenAPI 3, MapStruct 1.6.3
**DevOps**: Docker, GitHub Actions

---

## 📦 Latest Release

**v0.0.7** (2026-09) — Security Hardening ✅

- Organization-scoped reads close cross-tenant access (P1 IDOR)
- Master data mutations ADMIN-only, operational data USER/ADMIN
- Google Sign-In with GUEST auto-provisioning
- 401/403 contract: missing refresh cookie → 401, service-layer denial → 403
- Token blacklist and security audit persisted to the database

**In progress — v0.0.8**: Spring Boot 4.2, per-client rate limiting, balance snapshot and concurrent-posting fixes, production configuration cleanup. See the changelog.

[Complete changelog](docs/CHANGELOG.md) | [Roadmap](docs/ROADMAP.md)

---

## API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register new user |
| POST | `/api/auth/login` | No | Login, receive access token + refresh cookie |
| POST | `/api/auth/google` | No | Sign in with a Google ID token (new users become GUEST) |
| POST | `/api/auth/refresh` | No | Refresh access token (cookie-based) |
| POST | `/api/auth/change-password` | Yes | Change password |
| POST | `/api/auth/logout` | Yes | Logout, blacklist token |

### Admin (`/api/admin`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/admin/users` | ADMIN | List users (paginated) |
| GET | `/api/admin/users/{id}` | ADMIN | Get user details |
| PATCH | `/api/admin/users/{id}/role` | ADMIN | Change user role |
| PATCH | `/api/admin/users/{id}/status` | ADMIN | Enable/disable user |
| POST | `/api/admin/external-rate-sync/sync` | ADMIN | Trigger ECB sync |

### Core & Banking
See [Swagger UI](https://api.tarasantoniuk.com/swagger-ui/index.html) for full interactive documentation.

---

## Development

**Branch Strategy**: `feature/*` / `fix/*` → `dev`; every push to `dev` is tested and deployed
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
