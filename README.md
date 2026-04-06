# Finance Access Backend

Finance Access Backend is a focused Spring Boot service for a finance dashboard. It handles user administration, role-based access control, financial record management, and dashboard-oriented summary analytics with a deliberate, minimal setup.

## Why this design

- `AppUser` models operational users with explicit role and status fields.
- `FinancialRecord` models auditable income and expense entries with filters that map cleanly to dashboard use cases.
- Services hold business rules and aggregation logic so controllers stay thin.
- HTTP Basic authentication keeps local setup simple while still enforcing backend authorization correctly.
- H2 file storage gives a real SQL database without extra infrastructure, while tests use an in-memory H2 instance for fast isolation.

## Stack

- Java 21
- Spring Boot 3.3
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security
- H2 SQL database
- JUnit 5 and MockMvc integration tests

## Project structure

```text
src/main/java/com/financedashboard/access
|- auth
|- common
|- dashboard
|- finance
|- security
|- setup
|- user
```

## Local setup

1. Install Java 21 and Maven.
2. From the project root, run `mvn spring-boot:run`.
3. The API starts on `http://localhost:8080`.
4. The H2 console is available at `http://localhost:8080/h2-console`.

Connection settings for the H2 console:

- JDBC URL: `jdbc:h2:file:./data/finance-access-db;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: leave blank

## Seeded users

| Username | Password | Role | Status |
| --- | --- | --- | --- |
| `admin.priya` | `Admin@123` | `ADMIN` | `ACTIVE` |
| `analyst.rahul` | `Analyst@123` | `ANALYST` | `ACTIVE` |
| `viewer.meera` | `Viewer@123` | `VIEWER` | `ACTIVE` |
| `archived.aisha` | `Inactive@123` | `ANALYST` | `INACTIVE` |

Inactive users remain in the database but cannot authenticate.

## Access model

- `VIEWER`: can access dashboard endpoints only.
- `ANALYST`: can access dashboard endpoints and read financial records.
- `ADMIN`: full access to dashboard, records, and user administration APIs.

## API surface

### Authentication

- `GET /api/auth/me`

### Users

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`

### Financial records

- `POST /api/records`
- `GET /api/records`
- `GET /api/records/{id}`
- `PUT /api/records/{id}`
- `DELETE /api/records/{id}`

Supported record filters:

- `type`
- `category`
- `fromDate`
- `toDate`
- `minAmount`
- `maxAmount`

### Dashboard

- `GET /api/dashboard/overview`
- `GET /api/dashboard/trends?months=6`

`/api/dashboard/overview` returns:

- total income
- total expenses
- net balance
- category totals grouped by record type
- recent activity list

## Example requests

Create a record as admin:

```bash
curl -u admin.priya:Admin@123 -X POST http://localhost:8080/api/records \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 510.00,
    "type": "EXPENSE",
    "category": "Hardware",
    "recordDate": "2026-04-01",
    "description": "Replacement keyboard"
  }'
```

Read filtered records as analyst:

```bash
curl -u analyst.rahul:Analyst@123 "http://localhost:8080/api/records?type=EXPENSE&category=Rent&minAmount=1000&maxAmount=2000"
```

Fetch dashboard overview as viewer:

```bash
curl -u viewer.meera:Viewer@123 http://localhost:8080/api/dashboard/overview
```

## Validation and error handling

- Request payloads use bean validation with field-specific messages.
- Invalid filters such as `fromDate > toDate` return `400 Bad Request`.
- Missing resources return `404 Not Found`.
- Duplicate usernames return `409 Conflict`.
- Unauthorized requests return `401 Unauthorized`.
- Role violations return `403 Forbidden`.

Error responses follow a consistent JSON shape:

```json
{
  "timestamp": "2026-04-06T14:55:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/records",
  "validationErrors": {
    "amount": "Amount must be greater than zero"
  }
}
```

## Tests

Run the integration suite with:

```bash
mvn test
```

The test suite covers:

- role-based access behavior
- record CRUD authorization
- user administration
- filtering logic
- dashboard summaries
- inactive-user authentication rejection
- validation failures

## Assumptions and tradeoffs

- Usernames are immutable after creation to keep identity stable and simplify audit behavior.
- HTTP Basic is used instead of JWT because the assignment emphasizes backend logic, access rules, and correctness over token infrastructure.
- H2 was chosen to keep the SQL setup essential and friction-free while still persisting data between runs.
- Soft delete was not added because inactive user status already covers the user lifecycle requirement, and record deletion is explicitly required.
