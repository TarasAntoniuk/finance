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
✅ **Event Sourcing** — Immutable transaction audit trail
✅ **Bank Receipts & Payments** — 10+ transaction types each
✅ **Document Lifecycle** — DRAFT → POST → UNPOST
✅ **Financial Reports** — Balance & Turnover with filters
✅ **Balance Snapshots** — Validity tracking with auto-invalidation

### 🔒 Security Module
✅ **JWT Authentication** — Access tokens (15 min) + refresh tokens (7 days)
✅ **Role-Based Access Control** — ADMIN, USER, GUEST roles
✅ **Account Lockout** — 5 failed attempts = 30-minute lock
✅ **Rate Limiting** — Resilience4j (5 requests/60s on auth endpoints)
✅ **HttpOnly Secure Cookies** — Refresh tokens via SameSite=Strict cookies
✅ **Token Blacklist** — Immediate token invalidation on logout
✅ **Security Audit Logging** — Login, registration, lockout events
✅ **Multi-Tenancy Foundation** — User-to-organization binding

### 🔧 Technical Excellence
✅ **1120+ test methods** across 67 test files
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
|  |  - Event Sourcing (immutable events)      | |
|  |  - Financial Reports                      | |
|  +------------------------------------------+ |
+-------------------+---------------------------+
                    |
                    v
+-----------------------------------------------+
|         PostgreSQL 17                         |
|  - Transactional tables                       |
|  - Event Store (append-only)                  |
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

**Backend**: Java 21, Spring Boot 3.5.10, Spring Security 6.x
**Security**: JWT (JJWT 0.12.6), Resilience4j, Caffeine cache
**Database**: PostgreSQL 17
**Testing**: JUnit 5, Testcontainers, JaCoCo (80% line / 75% branch)
**API**: REST, Swagger/OpenAPI 3, MapStruct 1.6.3
**DevOps**: Docker, GitHub Actions

---

## 📦 Latest Release

**v0.0.6** (2026-02) — Spring Security & JWT Authentication ✅

**Added**:
- Complete Spring Security with JWT authentication
- Role-based access control (ADMIN, USER, GUEST)
- Account lockout, rate limiting, token blacklist
- HttpOnly secure cookies for refresh tokens
- Admin user management endpoints
- Security audit logging with event system
- Multi-tenancy foundation (user-organization binding)

**Improved**:
- Test suite optimized: ~51s execution (down from 90-120s)
- 1120+ test methods across 67 test files

[Complete changelog](docs/CHANGELOG.md) | [Roadmap](docs/ROADMAP.md)

---

## API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register new user |
| POST | `/api/auth/login` | No | Login, receive access token + refresh cookie |
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
