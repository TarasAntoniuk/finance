# Changelog

All notable changes to the Financial Accounting System are documented in this file.

The project follows modular development: **Core Module** (foundation), **Banking Module** (transactions), and **Security Module** (authentication & authorization).

---

## Version History

### Security Module

#### [0.0.6] - February 2026 ✅
**Spring Security & JWT Authentication**

**Added:**
- Complete Spring Security integration with JWT authentication
  - HMAC-SHA256 signed access tokens (15-minute expiration)
  - HttpOnly, Secure, SameSite=Strict refresh token cookies (7-day expiration)
  - Token blacklist via Caffeine cache (15-min TTL, 10K max entries)
- Role-based access control (RBAC) with three roles: ADMIN, USER, GUEST
  - GET `/api/v1/**` — any authenticated user
  - POST/PUT/PATCH `/api/v1/**` — USER or ADMIN
  - DELETE `/api/v1/**` — ADMIN only
  - `/api/admin/**` — ADMIN only
- User management system
  - `User` entity with email (unique), encoded password, role, organization FK
  - Registration, login, logout, password change endpoints
  - Admin endpoints for user listing, role changes, account enable/disable
- Account lockout protection (5 failed attempts = 30-minute lock)
- Rate limiting with Resilience4j (5 requests/60s on auth endpoints)
- Security audit event system (login, registration, lockout events)
- Security headers: HSTS (1 year), CSP, X-Frame-Options: DENY, X-Content-Type-Options: nosniff
- CORS configuration with configurable allowed origins
- Custom JSON error responses for 401 (Unauthorized) and 403 (Forbidden)
- Method-level security with `@PreAuthorize` annotations
- Multi-tenancy foundation: user-to-organization binding
- Admin ECB sync endpoint (`/api/admin/external-rate-sync/sync`)

**API Endpoints:**
- `POST /api/auth/register` — User registration (public)
- `POST /api/auth/login` — Login with access token + refresh cookie (public)
- `POST /api/auth/refresh` — Refresh access token via cookie (public)
- `POST /api/auth/change-password` — Change password (authenticated)
- `POST /api/auth/logout` — Logout and blacklist token (authenticated)
- `GET /api/admin/users` — List users, paginated (ADMIN)
- `GET /api/admin/users/{id}` — User details (ADMIN)
- `PATCH /api/admin/users/{id}/role` — Change user role (ADMIN)
- `PATCH /api/admin/users/{id}/status` — Enable/disable user (ADMIN)

**Breaking Changes:**
- Refresh token moved from response body to HttpOnly secure cookie
- All `/api/v1/**` endpoints now require authentication

**Technical Details:**
- New security module: 22 Java files (entities, services, controllers, filters, config)
- 99% instruction coverage on security subsystem
- 80+ security-specific test methods
- Dependencies added: spring-boot-starter-security, jjwt 0.12.6, resilience4j 2.2.0, caffeine

---

#### Test Performance Optimization (February 2026)

**Improved:**
- Test suite execution time reduced from 90-120s to ~51s
- Enabled `reuseForks=true` in Maven Surefire (was `false`, causing 67 JVM restarts)
- Converted 10 mapper tests from `@SpringBootTest` to pure unit tests
- Enabled Testcontainers reuse across test runs
- Optimized `@Transactional` usage for test cleanup
- Removed double cleanup in test infrastructure

**Stats:**
- 1120+ test methods across 67 test files
- ~51 seconds total execution time

---

### Banking Module (Production)

#### [0.0.5] - 2025-01-13 ✅
**Snapshot Validity & Bug Fixes**

**Added:**
- Snapshot validity tracking system (7 new files in `common/snapshot/`)
  - `SnapshotValidity.java` — Entity for tracking invalid snapshots
  - `ValidityStatus.java` — Enum for validity states
  - `SnapshotValidityRepository.java` — Repository with query methods
  - `AbstractSnapshotScheduler.java` — Base scheduler for recalculation
  - `AbstractSnapshotValidityService.java` — Base service for validity lifecycle
- Backdated transaction detection and auto-invalidation
  - Transactions older than 1 hour trigger snapshot invalidation
  - Integrated into `BankReceiptService` and `BankPaymentService`
- `BankAccountBalanceService.java` — Extracted from transaction service (197 lines)
- `BankAccountSnapshotValidityService.java` — Account-specific validity handling

**Fixed:**
- N+1 query issue in `BankReceiptRepository` and `BankPaymentRepository`
  - Added `LEFT JOIN FETCH b.country` and `LEFT JOIN FETCH b.counterparty`
  - 12 query methods updated
  - Performance: 41 queries → 1 query (~40x improvement)
- Boundary condition bug causing double-counted opening balance
  - Problem: Transactions at exact period start (00:00:00) counted twice
  - Fix: Changed query logic from `BETWEEN` to `>= AND <`
  - Removed unreliable `.minusNanos(1)` workarounds
  - Files: `BankAccountTransactionEventRepository`, `BankAccountBalanceSnapshotRepository`, `BankAccountBalanceService`, `AccountTurnoverReportService`

**Changed:**
- Refactored balance calculation logic into separate `BankAccountBalanceService`
- Renamed `common/report/` directory to `common/period/`
- Reorganized swagger config into `common/swagger/`

**Technical Details:**
- +1,627 lines / -211 lines
- 7 new Java files, 16 files modified
- 903 test methods across 55 test files

---

#### [0.0.4] - December 2024 ✅
**Banking Module: Event Sourcing Implementation** ⭐

**Pull Request**: [#10](https://github.com/TarasAntoniuk/finance/pull/10)

**Added:**

**Bank Receipts** — Incoming payment processing with 10+ transaction types:
- Customer payments, loan receipts, investments
- Refunds, interest income, internal transfers, other income

**Bank Payments** — Outgoing payment processing with 10+ transaction types:
- Supplier payments, salary disbursements, tax payments
- Loan repayments, contractor payments, utilities, rent, refunds

**Event Sourcing Architecture**:
- `BankAccountTransactionEvent` — Immutable event log with complete audit trail
- `BankAccountBalanceSnapshot` — Performance optimization cache
- Event-based balance calculation (real-time from event stream)
- Reversal support (unpost creates reversal events, never deletes)

**Document Lifecycle**:
- DRAFT → create and edit documents
- POST → finalize and create immutable events
- UNPOST → reverse with automatic reversal events
- CANCEL → mark as cancelled

**Financial Reports**:
- Account Balance Report (as of date, multi-currency)
- Account Turnover Report (period-based with opening/closing balances)
- Flexible filtering (organization, account, currency, date range)

**Technical Achievements:**
- Testcontainers for isolated integration tests
- PostgreSQL sequence synchronization
- Comprehensive Swagger documentation with request/response examples

**API Coverage:**
- Bank Receipts CRUD with lifecycle operations (post/unpost)
- Bank Payments CRUD with lifecycle operations (post/unpost)
- Financial Reports (balance, turnover)

*Complete API documentation: [Swagger UI](https://api.tarasantoniuk.com/swagger-ui/index.html)*

---

### Core Module (Production)

#### [0.0.3] - November 2024 ✅
**Core Module: Performance Optimization**

**Added:**
- Performance profiling with AOP monitoring for all service methods
- Bulk insert optimization for 190,000+ exchange rate records
- Historical data API endpoints with date range filtering
- JaCoCo code coverage with automated badge generation

**Improved:**
- Database query optimization (eliminated N+1 queries)
- Batch processing for ECB data import
- Test coverage increased to 95%+

**Technical Details:**
- Processing time: 190k records in ~15 seconds
- Memory optimization for large datasets
- Strategic database indexing

---

#### [0.0.2] - October 2024 ✅
**Core Module: ECB Integration**

**Added:**
- Automatic ECB synchronization with Spring `@Scheduled`
- Daily currency rate updates at 16:05 CET (cron: `0 5 16 * * *`)
- XML parsing from ECB endpoint
- Idempotent data loading (duplicate prevention)
- Currency management REST API
- Exchange rate query API with date range support

**Entities:**
- `Currency` — ISO 4217 compliant currency registry
- `ExchangeRate` — Daily rates with date, base/target currencies

**Technical Details:**
- Scheduled job with error handling and retry logic
- Transaction safety (all-or-nothing imports)
- 40+ currencies support

---

#### [0.0.1] - September 2024 ✅
**Core Module: Foundation**

**Added:**
- Project initialization with Spring Boot
- PostgreSQL 17 database setup
- Basic entity structure
- REST API skeleton with Swagger documentation

**Entities:**
- `Organization` — Multi-organization support with fiscal year policies
- `Bank` — Bank registry with SWIFT code validation
- `BankAccount` — Multi-currency accounts, multi-holder support
- `Counterparty` — Customer and supplier registry
- `Currency` — ISO 4217 currency codes
- `ExchangeRate` — Currency exchange rate storage

**Infrastructure:**
- Docker deployment configuration
- GitHub Actions CI/CD pipeline
- Test infrastructure with JUnit 5
- MapStruct for DTO mapping

---

## What's Next?

See [ROADMAP.md](ROADMAP.md) for planned features and enhancements.

---

## Contributing

Have suggestions? Open an issue on [GitHub](https://github.com/TarasAntoniuk/finance/issues) or contact me directly.
