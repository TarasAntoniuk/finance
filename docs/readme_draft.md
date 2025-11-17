## Banking System Module

Implemented comprehensive banking system functionality with two main modules:

### Bank Management
- Full CRUD operations for banks with country relationships
- SWIFT code validation and unique constraints
- Bank activation/deactivation functionality
- Search by country and active status
- Counterparty relationship support

### Bank Account Management
- Multi-holder bank accounts (Organizations and Counterparties)
- Account status management (Active, Inactive, Closed)
- Default account designation per holder
- Account number uniqueness validation
- Filtering by holder, bank, and status

### Technical Implementation
- **Architecture**: RESTful API with layered architecture (Controller → Service → Repository)
- **Persistence**: JPA/Hibernate with PostgreSQL, indexed queries for performance
- **Mapping**: MapStruct for DTO-Entity conversions
- **Validation**: Jakarta Validation with custom business rules
- **Documentation**: OpenAPI/Swagger annotations
- **Testing**: Comprehensive test coverage with Testcontainers for integration tests
- **Exception Handling**: Custom exception hierarchy with proper HTTP status codes

### Key Features
- Discriminator pattern for account holder types
- Composite indexes on frequently queried fields (SWIFT codes, account numbers, holder identifiers)
- Immutable audit fields (createdAt, updatedAt) with JPA auditing
- Branch coverage > 95% with unit and integration tests