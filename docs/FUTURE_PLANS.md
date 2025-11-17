# Future Plans – Financial Accounting Prototype

> **Development Note**: This is a **spare-time side project**.  
> Timeline is flexible and depends on available time.

---

## Priority Levels

- 🔴 **P0**: Next immediate focus
- 🟡 **P1**: Important for core functionality
- 🟢 **P2**: Nice to have
- ⚪ **P3**: Long-term goals

---

## ✅ Completed

### Version 0.0.1 – Initial Release (October 2025)
- ✅ Core entities (Currency, Country, Organization, Accounting Policy, Exchange Rate)
- ✅ REST API with Swagger documentation
- ✅ MapStruct DTO mapping
- ✅ Testcontainers integration tests
- ✅ GitHub Actions CI/CD

### Version 0.0.2 – Exchange Rate Automation (November 2025)
- ✅ European Central Bank (ECB) API integration
- ✅ Scheduled daily updates (16:05 CET)
- ✅ Historical exchange rate data loading
- ✅ Batch operations support
- ✅ JaCoCo test coverage (80% line, 75% branch)

### Version 0.0.3 – Banking System Module (November 2025)
- ✅ Bank management (CRUD operations)
- ✅ Bank Account management with discriminator pattern
- ✅ SWIFT code validation
- ✅ Multi-holder accounts (Organizations and Counterparties)
- ✅ Account status management (Active, Inactive, Closed)
- ✅ Comprehensive test coverage (>95% branch coverage)
- ✅ Composite indexes for performance

---

## 🔴 P0 – Next Release (Version 0.0.4)

### Account Balance & Transaction System
**Status**: In Planning  
**Target**: Q4 2025 / Q1 2026

#### Account Balance Tracking
**Purpose**: Track current balances for all bank accounts with caching mechanism

**New Entity: AccountBalance**
- Current balance per bank account
- Last transaction reference
- Optimistic locking support
- Automatic updates from transactions

#### Transaction History with Event Sourcing
**Purpose**: Immutable transaction log with complete audit trail

**New Entity: AccountTransaction**
- Immutable transaction records (no UPDATE/DELETE)
- Transaction type: DEBIT/CREDIT
- Balance after each transaction
- Source document tracking (type + ID)
- Transaction status: ACTIVE/REVERSED/CANCELLED
- Support for backdated changes with recalculation

#### Payment Documents
**Purpose**: Business documents that create banking transactions

**Document Types to Implement**:
- **PaymentOrder** (outgoing payment) → DEBIT transaction
- **PaymentReceived** (incoming payment) → CREDIT transaction
- **BankCommission** (bank fees) → DEBIT transaction
- **Transfer** (between accounts) → DEBIT + CREDIT transactions
- **InitialBalance** (opening balance) → CREDIT transaction

**Key Features**:
- Documents implement `BankTransactionSource` interface
- Transactions created only through documents (no direct CRUD)
- Document cancellation creates reverse transactions
- Automatic balance recalculation for backdated changes
- Package-private services for transaction control

#### Architecture Highlights
- Event Sourcing pattern for transaction history
- Dual-table approach (AccountBalance as cache, AccountTransaction as source of truth)
- Balance recalculation from any point in time
- Complete audit trail
- Protection against direct transaction manipulation

---

## 🟡 P1 – Near-term Plans (3-6 months)

### 1. Security Implementation
**Target**: Version 0.0.5

- Spring Security integration
- JWT authentication
- Role-based access control (RBAC)
- User management
- Password encryption
- Audit logging

### 2. Payment Document Workflow
**Target**: Version 0.0.5-0.0.6

- Document approval workflow
- Multi-step approval process
- Document templates
- Bulk payment operations
- Payment scheduling

### 3. Reporting System
**Target**: Version 0.0.6

- Balance reports by account/currency/organization
- Transaction history reports
- Financial statements preparation
- Export formats (PDF, Excel, CSV)

---

## 🟢 P2 – Medium-term Plans (6-12 months)

### 1. Financial Modules

#### Chart of Accounts
- Account structure
- Account types and categories
- Account hierarchies

#### Journal Entries
- Transaction recording
- Double-entry bookkeeping
- Entry validation

#### General Ledger
- Ledger maintenance
- Trial balance
- Financial statements (Balance Sheet, P&L)

### 2. Advanced Currency Operations

- Real-time conversion on transactions
- Historical rate conversion
- Multiple rate sources
- Currency exposure reports

### 3. Counterparty Management
- Counterparty CRUD operations
- Counterparty bank accounts
- Payment history by counterparty

---

## ⚪ P3 – Long-term Goals (12+ months)

### Frontend Development
- Admin dashboard
- Interactive reports
- User management UI
- Payment document interface

### Advanced Features
- Banking API integration
- Accounting software integration
- Multi-organization support
- Workflow automation

### Infrastructure
- Docker Compose setup
- Kubernetes deployment
- Monitoring (Prometheus, Grafana)
- Redis caching
- Database migrations (Liquibase/Flyway)

---

## Version Roadmap

| Version | Timeline | Key Features                      |
|---------|----------|-----------------------------------|
| 0.0.1   | Oct 2025 | ✅ Initial CRUD operations         |
| 0.0.2   | Nov 2025 | ✅ Exchange rate automation        |
| 0.0.3   | Nov 2025 | ✅ Banking system module           |
| 0.0.4   | Q1 2026  | 🔴 Account balance & transactions |
| 0.0.5   | Q2 2026  | 🟡 Security & user management     |
| 0.0.6   | Q3 2026  | 🟡 Payment workflow & reporting   |
| 0.0.7+  | Q4 2026+ | 🟢 Financial modules              |
| 1.0.0   | 2027+    | Production release                |

---

## Development Approach

**Iterative Process**:
1. Design architecture carefully
2. Implement core features
3. Write comprehensive tests (>95% coverage)
4. Release stable version
5. Gather feedback
6. Plan next iteration

**Flexibility**:
- Features may be reprioritized based on needs
- Timeline adjusts based on available time
- Focus on quality over speed

---

## Contributing

Ideas and suggestions are welcome!

**Consider**:
- Alignment with project goals
- Implementation complexity
- Maintainability
- Test coverage requirements

---

**Document Version**: 0.0.3  
**Last Updated**: November 2025  
**Next Review**: Quarterly