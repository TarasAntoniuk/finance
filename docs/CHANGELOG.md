# Changelog

All notable changes to the Financial Accounting System are documented in this file.

The project follows modular development: **Core Module** (foundation) and **Banking Module** (transactions).

---

## Version History

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
- Project initialization with Spring Boot 3.5.5
- PostgreSQL 17 database setup
- Flyway database migrations
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

### Banking Module (In Development)

#### [0.0.4] - December 2024 🚧
**Banking Module: Event Sourcing Implementation** ⭐

**Status**: Feature complete, documentation in progress

**Pull Request**: [#10](https://github.com/TarasAntoniuk/finance/pull/10)

**Completed:**
- ✅ Bank Receipts & Payments implementation
- ✅ Event Sourcing architecture
- ✅ 193 comprehensive tests with 97% line & 95% branch coverage
- ✅ API with Swagger documentation
- 🚧 Documentation (README, CHANGELOG, ROADMAP, ARCHITECTURE)

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
- 193 comprehensive tests (unit + integration)
- 97% line coverage, 95% branch coverage (JaCoCo verified)
- N+1 query prevention with `@EntityGraph`
- Testcontainers for isolated integration tests
- PostgreSQL sequence synchronization
- Comprehensive Swagger documentation with request/response examples

**API Coverage:**
- Bank Receipts CRUD with lifecycle operations (post/unpost)
- Bank Payments CRUD with lifecycle operations (post/unpost)
- Financial Reports (balance, turnover)

*Complete API documentation: [Swagger UI](https://api.tarasantoniuk.com/swagger-ui/index.html)*

**Target Release**: Q1 2025 (after documentation completion)

---

## What's Next?

See [ROADMAP.md](ROADMAP.md) for planned features and enhancements.

---

## Contributing

Have suggestions? Open an issue on [GitHub](https://github.com/TarasAntoniuk/finance/issues) or contact me directly.