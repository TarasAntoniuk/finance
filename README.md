# Financial Accounting Prototype – Backend Project

## Overview

This is the **first version of my Financial Accounting Prototype**.  
Currently, it focuses on **currency rate management** and demonstrates backend architecture, service design, and automated workflows.  
It serves as a foundation for extending functionality into a broader financial system in the future.

> **Important Note:**  
> This project is developed **in my spare time** as a side project, not as my main occupation.  
> Development pace depends on time availability. This is a prototype and minimal model for future growth.

For more details:
- [System Description](docs/SYSTEM_DESCRIPTION.md)
- [Technology Stack](docs/TECH_STACK.md)
- [Released Versions & Changelog](docs/RELEASES.md)
- [Future Plans](docs/FUTURE_PLANS.md)

---

## What is Implemented

### Backend (Java/Spring Boot)
- **REST API** for managing:
    - Currencies
    - Countries
    - Organizations
    - Accounting policies
    - Exchange rates
- **Currency table/entity** + ability to load standard currency list from file
- **Countries table/entity**
- **Organizations table/entity**
- **Accounting policies table/entity** (defines for which currencies exchange rates are loaded)
- **Exchange rates table/entity**
- Unit and integration tests using JUnit 5 + Testcontainers
- CI/CD pipeline for automated testing before release
- Modular backend architecture for future extensions
- API documentation via Swagger/OpenAPI

### What is NOT Implemented Yet
- ❌ Frontend (completely absent)
- ❌ Historical data tables (for exchange rates history and audit trail of entity changes)
- ❌ Automatic daily exchange rate updates from external resources (planned as first priority)

---

## Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.5.5**
- **Spring Data JPA / Hibernate 6.5**
- **MapStruct** – DTO ↔ Entity mapping
- **JUnit 5 + Testcontainers** – unit and integration tests
- **Maven** – dependency management and build
- **REST API**
- **Lombok is not used**

### Database
- **PostgreSQL 17**

### DevOps / CI/CD
- **GitHub Actions** – all tests run automatically before merging to stable branch
- **Docker** – required for Testcontainers (integration testing only)

### Development Tools
- **Postman / Swagger** – API testing
- **IDE: IntelliJ IDEA**

---

## Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- PostgreSQL 17
- Docker (required for running integration tests with Testcontainers)

### Installation
```bash
# Clone repository
git clone [repository-url]
cd finance-core

# Install dependencies
mvn clean install
```

### Configuration
Create configuration file at *src/main/resources/env/env.dev.properties*:
```properties
# Database configuration
DB_URL=jdbc:postgresql://BD_Host:5432/DB_Name
DB_USERNAME=DB_Login
DB_PASSWORD=DB_Password
```
### Running the Application
Run Spring Boot application:
```bash
mvn spring-boot:run
```
Or run tests
```bash
mvn test
```

### Verify Installation
The application should start on http://localhost:8080

API Documentation:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs (JSON): http://localhost:8080/api-docs
- OpenAPI Docs (YAML): http://localhost:8080/api-docs.yaml

## Planned Features / Next Steps
- **First Priority**: Load exchange rates from external resources (ECB or other APIs) with automatic daily updates.
- **Near-term**: Improve testing, add security, implement account management and balances.
- **Long-term**: Expand to full financial accounting system with advanced analytics, dashboards, and optimized deployment.
> 📋 See detailed roadmap in Future Plans

## Project Status
- ⚠️ **Current Status**: Prototype / Minimal Model
- 📅 **Development Mode**: Spare time / Side project
- 🎯 **Focus**: Backend architecture and core functionality

## Contributing
The project is open for suggestions and discussions. Given the limited development time, any help and feedback are valuable.

Contact: **Taras Antoniuk**  
📧 [bronya2004@gmail.com](mailto:bronya2004@gmail.com)  
🔗 [LinkedIn](https://www.linkedin.com/in/taras-antoniuk-7a550816a/)  
💻 [HackerRank Profile](https://www.hackerrank.com/profile/bronya2004)

---

Documentation Version: 0.0.1

Last Updated: October 2025

