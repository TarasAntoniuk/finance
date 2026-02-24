# Roadmap

Planned features and enhancements for the Financial Accounting System.

---

## Completed

### v0.0.6 — Spring Security Implementation ✅

- **JWT Authentication** — HMAC-SHA256 access tokens (15 min) + HttpOnly refresh cookies (7 days)
- **Role-Based Access Control** — ADMIN, USER, GUEST roles with endpoint-level authorization
- **User Management** — Registration, login, logout, password change, admin user management
- **Account Lockout** — 5 failed attempts = 30-minute lock
- **Rate Limiting** — Resilience4j (5 requests/60s on auth endpoints)
- **Token Blacklist** — Caffeine cache for immediate token invalidation
- **Security Headers** — HSTS, CSP, X-Frame-Options, X-Content-Type-Options
- **Audit Logging** — Security event publishing (login, registration, lockout)
- **Multi-Tenancy Foundation** — User-to-organization binding
- **Method-Level Security** — `@PreAuthorize` annotations
- **Custom Error Responses** — JSON 401/403 error handlers

### v0.0.5 — Frontend & Snapshot Validity ✅

- Basic frontend interface for API demonstration
- Snapshot validity tracking system
- Backdated transaction detection
- N+1 query fix (~40x improvement)
- Balance boundary condition fix

### v0.0.4 — Event Sourcing ✅

- Bank receipts & payments with 10+ types each
- Immutable event store
- Document lifecycle (DRAFT → POSTED → CANCELLED)
- Financial reports (balance & turnover)

### v0.0.3 — Performance Optimization ✅

- Bulk insert optimization (190k+ records)
- JaCoCo coverage enforcement
- Database query optimization

### v0.0.2 — ECB Integration ✅

- Automatic daily sync at 16:05 CET
- 40+ currencies, historical data

### v0.0.1 — Foundation ✅

- Project initialization, core entities, REST API, CI/CD pipeline

---

## Next Priority (v0.0.7)

### 📦 Services & Products Catalog
- Catalog of IT consulting services with flexible pricing models:
    - Hourly / Daily / Monthly rates
    - Developer level tiers (Junior, Middle, Senior)
- Price list management per service type
- Order creation for information & consulting services
- Service assignment to developers by skill level

### 🔄 Enhanced Multi-Tenancy
- Data isolation per organization
- Organization-scoped queries across all modules
- User invitation system

---

## Future Enhancements

### Infrastructure
- Redis caching layer
- Apache Kafka for order processing events
- Database migrations (Liquibase/Flyway)
- Kubernetes deployment
- Monitoring (Prometheus, Grafana)

### Features
- PDF/Excel report generation
- Email notifications
- Document approval workflow
- Advanced financial analytics

---

## Contributing

Have suggestions? Open an issue on [GitHub](https://github.com/TarasAntoniuk/finance/issues) or contact me directly.