# Internal Wallet Service

A closed-loop virtual currency management system built for gaming platforms and loyalty programs. Handles wallet top-ups, bonuses, and spending with full ACID compliance, concurrency safety, and an auditable double-entry ledger.

---

## Table of Contents

- [Technology Stack](#technology-stack)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Concurrency and Data Integrity](#concurrency-and-data-integrity)
- [Database Schema](#database-schema)
- [Configuration](#configuration)
- [Deployment](#deployment)

---

## Technology Stack

| Component | Technology | Rationale |
|:---|:---|:---|
| Runtime | Java 21, Spring Boot 3.4.2 | LTS release with virtual threads support and mature ecosystem |
| Database | PostgreSQL 16 | ACID compliance, CHECK constraints, JSONB, partial indexes |
| Migrations | Flyway | Version-controlled, repeatable schema evolution |
| Connection Pool | HikariCP | Industry-standard pool with leak detection |
| Secrets | AWS Secrets Manager + Spring Cloud AWS 3.x | Credentials loaded before application context starts |
| Documentation | Springdoc OpenAPI 2.8 | Auto-generated API docs from annotations |
| Containerization | Docker, Docker Compose | Reproducible builds and deployments |

---

## Architecture Overview

The service follows a layered architecture with clear separation of concerns:

```
Controller Layer     →  Request validation, HTTP semantics
Service Layer        →  Business logic, transaction orchestration
Repository Layer     →  Data access, pessimistic locking
Database Layer       →  PostgreSQL with Flyway-managed schema
```

Core design decisions:

- **Double-entry ledger**: Every top-up, bonus, and spend creates paired transactions between user and treasury wallets. This ensures the total currency supply is always reconcilable.
- **Idempotency**: All transaction endpoints require an `X-Idempotency-Key` header. Duplicate requests return the cached response without re-processing.
- **Deadlock avoidance**: When a transaction touches two wallets, locks are acquired in a deterministic order (sorted by UUID) to prevent circular waits.

---

## Getting Started

### Prerequisites

- Java 21 (OpenJDK or equivalent)
- Maven 3.9+
- Docker and Docker Compose
- AWS CLI configured with credentials that can access Secrets Manager

### 1. Set up AWS Secrets Manager

Create a secret named `wallet-service/database` in `us-east-1` with this JSON:

```json
{
  "url": "jdbc:postgresql://localhost:5432/wallet_db",
  "username": "wallet_user",
  "password": "wallet_secret"
}
```

For local development, point `url` to the Docker Compose PostgreSQL instance.

### 2. Start the database

```bash
docker-compose up -d
```

This starts PostgreSQL 16 on port 5432. The database `wallet_db` is created automatically.

### 3. Build and run

```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```

Flyway runs automatically on startup, creating all tables and seeding initial data (3 asset types, 5 users with 15 wallets, and corresponding audit transactions).

### 4. Verify

```bash
curl http://localhost:8080/actuator/health
```

API documentation is available at `http://localhost:8080/swagger-ui.html`.

---

## API Reference

All endpoints are prefixed with `/api/v1`.

### Asset Types

| Method | Endpoint | Description |
|:---|:---|:---|
| GET | `/api/v1/asset-types` | List all active asset types |
| GET | `/api/v1/asset-types/{id}` | Get asset type by ID |
| POST | `/api/v1/asset-types` | Create a new asset type |

### Wallets

| Method | Endpoint | Description |
|:---|:---|:---|
| POST | `/api/v1/wallets` | Create a wallet for a user |
| GET | `/api/v1/wallets/{id}` | Get wallet details |
| GET | `/api/v1/wallets/user/{userId}` | List all wallets for a user |
| GET | `/api/v1/wallets/{id}/balance` | Get wallet balance |

### Transactions

All transaction endpoints require the `X-Idempotency-Key` header.

| Method | Endpoint | Description |
|:---|:---|:---|
| POST | `/api/v1/transactions/credit` | Credit a wallet |
| POST | `/api/v1/transactions/debit` | Debit a wallet |
| POST | `/api/v1/transactions/top-up` | Top-up from treasury |
| POST | `/api/v1/transactions/bonus` | Award bonus from treasury |
| POST | `/api/v1/transactions/spend` | Spend to treasury |
| GET | `/api/v1/transactions/wallet/{walletId}` | Transaction history (paginated) |
| GET | `/api/v1/transactions/reference/{refId}` | Lookup by reference ID |
| GET | `/api/v1/transactions/reconcile/{walletId}` | Reconcile wallet balance |

---

## Concurrency and Data Integrity

The service employs multiple layers of protection against race conditions and data corruption:

### Pessimistic Locking

Critical wallet operations acquire a `SELECT ... FOR UPDATE` lock with a 5-second timeout. This prevents two concurrent transactions from reading stale balances and issuing conflicting writes.

### Optimistic Locking

The `Wallet` entity carries a `@Version` field. If two transactions manage to bypass the pessimistic lock (e.g., due to different transaction boundaries), the second commit fails with an `OptimisticLockException`, which is caught and reported to the caller.

### Deadlock Avoidance

Double-entry transactions lock two wallets. To prevent deadlocks, the service always acquires locks in ascending UUID order:

```java
if (userWallet.getId().compareTo(treasuryWallet.getId()) < 0) {
    firstLock = walletRepository.findByIdForUpdate(userWallet.getId());
    secondLock = walletRepository.findByIdForUpdate(treasuryWallet.getId());
} else {
    firstLock = walletRepository.findByIdForUpdate(treasuryWallet.getId());
    secondLock = walletRepository.findByIdForUpdate(userWallet.getId());
}
```

### Database-Level Guards

- `CHECK (balance >= 0)` on the wallets table prevents negative balances at the database level, acting as a safety net independent of application logic.
- `UNIQUE (reference_id)` on transactions prevents duplicate entries.
- `UNIQUE (user_id, asset_type_id)` on wallets enforces one wallet per user per asset type.

### Idempotency

Each transaction request carries an `X-Idempotency-Key` header. The service checks for a prior record before processing and caches the response. Expired idempotency records are cleaned up hourly.

---

## Database Schema

The schema is managed by Flyway with 7 migration scripts:

| Migration | Purpose |
|:---|:---|
| V1 | Create `asset_types` table |
| V2 | Create `wallets` table with balance constraints |
| V3 | Create `transactions` table (immutable ledger) |
| V4 | Create `idempotency_keys` table |
| V5 | Seed data: 3 asset types, 5 users, 15 wallets, audit transactions |
| V6 | Add treasury wallets and `counter_wallet_id` for double-entry |
| V7 | Expand idempotency constraint for all transaction types |

### Seeded Data

The migration seeds the following:

- **Asset types**: Gold Coins (GC, 0 decimals), Diamonds (DM, 0 decimals), Reward Points (RP, 2 decimals)
- **Users**: Alice (power player), Bob (casual), Charlie (new), Diana (frozen wallet), Eve (closed account)
- **Treasury**: System wallets with initial supply (1M Gold Coins, 500K Diamonds, 10M Reward Points)

---

## Configuration

### Secrets Management

Database credentials are loaded from AWS Secrets Manager at startup using Spring Cloud AWS. The secret is fetched before the application context initializes, ensuring the connection pool always has valid credentials.

```
spring.config.import=optional:aws-secretsmanager:wallet-service/database
```

The `optional:` prefix allows the application to fall back to default values if Secrets Manager is unavailable (useful during initial setup).

AWS credential resolution follows the default provider chain:
- **Local development**: `~/.aws/credentials` or environment variables
- **EC2**: IAM Instance Role (automatic)
- **ECS/Fargate**: IAM Task Role (automatic)

### Application Properties

| Property | Default | Description |
|:---|:---|:---|
| `wallet.transaction.max-page-size` | 100 | Maximum page size for transaction history |
| `wallet.transaction.default-page-size` | 20 | Default page size |
| `wallet.idempotency.ttl-hours` | 24 | Idempotency record TTL |
| `wallet.metadata.max-size-bytes` | 4096 | Maximum transaction metadata size |

---

## Deployment

### Docker Compose (Development)

```bash
docker-compose up -d
```

Starts PostgreSQL and the wallet service. The application connects to the database after it passes health checks.

### EC2 (Production)

```bash
./deploy.sh
```

The deploy script builds the Docker image, stops any running container, starts the new container with production settings, and waits for a health check before reporting success. It requires `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` environment variables (or AWS Secrets Manager configuration).

### Environment Variables

| Variable | Required | Description |
|:---|:---|:---|
| `AWS_ACCESS_KEY_ID` | Local only | AWS credentials for Secrets Manager |
| `AWS_SECRET_ACCESS_KEY` | Local only | AWS credentials for Secrets Manager |
| `SPRING_PROFILES_ACTIVE` | No | Set to `prod` for production hardening |
| `JAVA_OPTS` | No | JVM tuning (default: `-Xms256m -Xmx512m`) |
