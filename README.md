# OpsFlow

**Enterprise IT Service & Approval Automation Platform**

OpsFlow automates employee IT service requests — database access, VPN access, software access, infrastructure requests — through a configurable multi-level approval workflow (Manager → IT Admin). It uses event-driven processing with Kafka to decouple request creation, approval, execution, and notification into independent, asynchronous stages.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 21 |
| Framework | Spring Boot 3 |
| Security | Spring Security + JWT |
| Persistence | Spring Data JPA, PostgreSQL |
| Messaging | Apache Kafka |
| Caching | Redis |
| Infra | Docker / Docker Compose |
| Tooling | Lombok |

## Architecture

OpsFlow processes each request through a chain of decoupled stages connected by Kafka topics — request creation, approval, execution, and notification each run independently and communicate asynchronously rather than through direct, blocking calls.

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

Every important step (`REQUEST_CREATED`, `MANAGER_APPROVED`, `IT_ADMIN_APPROVED`, `EXECUTION_STARTED`, `REQUEST_COMPLETED`, rejections) is written to `audit_logs`, giving a full, queryable history per request.

## Project Structure

```
opsflow/
├── src/main/java/com/opsflow/
│   ├── controller/      REST endpoints (Auth, User, Request, Approval, Notification, Audit)
│   ├── service/         Business logic
│   ├── repository/      Spring Data JPA repositories
│   ├── entity/          User, ServiceRequest, Approval, Notification, AuditLog
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

## Getting Started

### Option A — Everything in Docker (simplest)

Requires only Docker + Docker Compose.

```bash
cd opsflow
docker compose up --build
```

This starts PostgreSQL, Zookeeper, Kafka, Redis, and the OpsFlow app together. The API is available at `http://localhost:8080`.

To stop:

```bash
docker compose down          # keep data
docker compose down -v       # also wipe the Postgres volume
```

### Option B — Infra in Docker, app run locally

Faster for iterative development, since restarting the app is quicker than rebuilding the whole image.

1. Start just the infrastructure:

   ```bash
   docker compose up postgres zookeeper kafka redis
   ```

2. Run the app from your IDE (open `OpsFlowApplication.java` and run it), or from the command line:

   ```bash
   ./mvnw spring-boot:run
   ```

   `application.properties` is already pointed at `localhost:5432` (Postgres), `localhost:9092` (Kafka), and `localhost:6379` (Redis) — matching the ports Docker Compose exposes.

### Prerequisites (Option B)

- Java 21 JDK
- Maven (or the included `./mvnw` wrapper, or a local Maven install)
- Docker (for Postgres/Kafka/Redis only)

## Trying the API

**1. Register a user**

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

**2. Log in** (once registered)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "lalsingh@company.com", "password": "password123"}'
```

**3. Create a request** (use the token from step 1/2 as a Bearer token)

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

This writes the request to PostgreSQL, publishes `request.created` to Kafka, and the consumer automatically creates the MANAGER + IT_ADMIN approval records.

**4. View the pending approvals**

```bash
curl http://localhost:8080/api/approvals/request/1
```

**5. Approve as Manager, then as IT Admin**

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

Once both approvals are `APPROVED`, the request status flips to `APPROVED`, Kafka publishes `request.approved`, `ExecutionService` moves it through `PROCESSING` → `COMPLETED`, and a completion notification plus audit entries are created automatically.

**6. Check the audit trail**

```bash
curl http://localhost:8080/api/audit/request/1
```

## Roadmap

- [ ] Resolve the acting user from the authenticated JWT (`UserService.getUserByEmail(authentication.getName())`) instead of the current hardcoded `resolveUserId` / `resolveApproverId`, to support real multi-user flows end-to-end
- [ ] Kafka retry/DLQ handling — add a `DefaultErrorHandler` and a dead-letter topic
- [ ] Automated tests — unit tests for the services, plus a Testcontainers-based integration test for the full Kafka approval flow

## License

Add a license of your choice (e.g. MIT) before making this repository public.
