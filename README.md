# OpsFlow — Enterprise IT Service & Approval Automation Platform

## Project definition

OpsFlow is a backend platform that automates employee IT service requests —
database access, VPN access, software access, infrastructure requests — through
a configurable multi-level approval workflow (Manager → IT Admin). It is built
as a **single Spring Boot monolith** (no microservices) but internally uses
event-driven processing with Kafka, so it demonstrates the same architectural
concepts (async messaging, decoupled workflow stages, caching, security) in a
project that's easier to build, run, and explain than a microservices system.

**Tech stack:** Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA,
PostgreSQL, Apache Kafka, Redis, Docker / Docker Compose, Lombok.

## Request lifecycle

```
Employee submits request
        │
        ▼
RequestController → RequestService → PostgreSQL (status = PENDING_APPROVAL)
        │
        ▼
Kafka topic: request.created
        │
        ▼
ApprovalService creates MANAGER + IT_ADMIN approval records
        │
   Manager approves ──► IT Admin approves
        │
        ▼   (once all approvals = APPROVED)
Kafka topic: request.approved
        │
        ▼
ExecutionService: status → PROCESSING → COMPLETED
        │
        ▼
Kafka topic: request.completed
        │
        ▼
NotificationService + AuditService record the outcome
```

Every important step (`REQUEST_CREATED`, `MANAGER_APPROVED`,
`IT_ADMIN_APPROVED`, `EXECUTION_STARTED`, `REQUEST_COMPLETED`, rejections)
is written to `audit_logs`, giving a full history per request.

## Project structure

```
opsflow/
├── src/main/java/com/opsflow/
│   ├── controller/      REST endpoints (Auth, User, Request, Approval, Notification, Audit)
│   ├── service/         Business logic
│   ├── repository/      Spring Data JPA repositories
│   ├── entity/           User, ServiceRequest, Approval, Notification, AuditLog
│   ├── dto/              Request/response payloads (entities are never exposed directly)
│   ├── kafka/            Producer, Consumer, topic configuration
│   ├── security/         JWT filter, JWT service, Spring Security config
│   ├── exception/        Global exception handler + custom exceptions
│   ├── config/           Redis cache configuration
│   └── OpsFlowApplication.java
├── src/main/resources/application.properties
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## How to run it

### Option A — everything in Docker (simplest)

Requires only Docker + Docker Compose installed.

```bash
cd opsflow
docker compose up --build
```

This starts PostgreSQL, Zookeeper, Kafka, Redis, and the OpsFlow app together.
The API will be available at `http://localhost:8080`.

To stop everything:

```bash
docker compose down          # keep data
docker compose down -v       # also wipe the Postgres volume
```

### Option B — infra in Docker, app run locally from your IDE

Useful while developing, since it's faster to restart the app than to rebuild
the whole image each time.

1. Start just the infrastructure:

   ```bash
   docker compose up postgres zookeeper kafka redis
   ```

2. Run the app from your IDE (open `OpsFlowApplication.java` and run it), or
   from the command line:

   ```bash
   ./mvnw spring-boot:run
   ```

   The `application.properties` file is already pointed at
   `localhost:5432` (Postgres), `localhost:9092` (Kafka) and
   `localhost:6379` (Redis), which matches the ports Docker Compose exposes.

### Prerequisites for Option B

- Java 21 JDK
- Maven (or use the included `./mvnw` wrapper if you generate one via `mvn -N io.takari:maven:wrapper`, or just use a locally installed Maven)
- Docker (for Postgres/Kafka/Redis only)

## Trying the API

1. **Register a user**

   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
           "username": "lalsingh",
           "email": "lalsingh@company.com",
           "password": "password123",
           "role": "EMPLOYEE"
         }'
   ```

   Response includes a JWT `token`.

2. **Log in** (once registered)

   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email": "lalsingh@company.com", "password": "password123"}'
   ```

3. **Create a request** (use the token from step 1/2 as a Bearer token)

   ```bash
   curl -X POST http://localhost:8080/api/requests \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{
           "title": "Production DB Access",
           "description": "Need read access for debugging",
           "type": "DATABASE_ACCESS",
           "priority": "HIGH"
         }'
   ```

   This writes the request to PostgreSQL, publishes `request.created` to
   Kafka, and the consumer automatically creates the MANAGER + IT_ADMIN
   approval records.

4. **View the pending approvals**

   ```bash
   curl http://localhost:8080/api/approvals/request/1
   ```

5. **Approve as Manager, then as IT Admin**

   ```bash
   curl -X POST http://localhost:8080/api/approvals/1/approve \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{"comments": "Looks good"}'

   curl -X POST http://localhost:8080/api/approvals/2/approve \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{"comments": "Approved"}'
   ```

   Once both approvals are `APPROVED`, the request status flips to
   `APPROVED`, Kafka publishes `request.approved`, `ExecutionService` moves
   it through `PROCESSING` → `COMPLETED`, and a completion notification +
   audit entries are created automatically.

6. **Check the audit trail**

   ```bash
   curl http://localhost:8080/api/audit/request/1
   ```

## Notes / things to implement next if you extend this

- `RequestController` and `ApprovalController` currently hardcode the acting
  user id (`resolveUserId` / `resolveApproverId`) instead of resolving it
  from the authenticated JWT — wire that up via `UserService.getUserByEmail(authentication.getName())`
  once you're ready to test multi-user flows end-to-end.
- No Kafka retry/DLQ handling yet — add `DefaultErrorHandler` +
  a dead-letter topic before claiming "reliable retry" on a resume.
- No automated tests yet — add unit tests for the services and an
  integration test (e.g. Testcontainers) for the full Kafka approval flow
  before claiming test coverage on a resume.
- Only keep resume bullets (Redis caching, Docker, retries, test coverage,
  specific metrics) that you've actually implemented and verified.
