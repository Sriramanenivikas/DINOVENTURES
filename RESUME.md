# DINOVENTURES — Internal Wallet Service (Resume Reference)

Use this file to add the project to your resume. It includes the tech stack and ready-to-use bullet points.

---

## Project Title

**Internal Wallet Service — Closed-Loop Virtual Currency Platform**

---

## Tech Stack

| Category             | Technologies                                                      |
|----------------------|-------------------------------------------------------------------|
| **Language**         | Java 21                                                           |
| **Framework**        | Spring Boot 3.4.2, Spring Data JPA, Spring Validation, Spring Actuator |
| **Database**         | PostgreSQL 16, Flyway (migrations), HikariCP (connection pooling) |
| **Cloud / DevOps**   | AWS EC2, AWS Secrets Manager, Docker, Docker Compose, GitHub Actions (CI/CD), GitHub Container Registry, Nginx, Certbot (Let's Encrypt) |
| **API Documentation**| Springdoc OpenAPI 2.8 (Swagger UI)                                |
| **Testing**          | JUnit 5, TestContainers                                          |
| **Other**            | Lombok, Logback, RESTful API design                               |

---

## Resume Bullet Points

Pick the bullets that best match the role you are applying for.

### Software Engineering / Backend

- Designed and built a production-ready **Internal Wallet Service** using **Java 21** and **Spring Boot 3.4.2**, supporting virtual currency top-ups, bonuses, and spending with full **ACID compliance**.
- Implemented a **double-entry ledger** system that pairs every user transaction with a treasury counterpart, ensuring 100% reconcilable currency supply across all asset types.
- Engineered concurrency-safe wallet operations using **pessimistic locking** (`SELECT … FOR UPDATE`), **optimistic locking** (`@Version`), and **deterministic lock ordering** (UUID-sorted) to eliminate race conditions and deadlocks.
- Built **idempotent REST APIs** with a server-side idempotency key mechanism, preventing duplicate transaction processing and guaranteeing exactly-once semantics.
- Designed a version-controlled database schema with **7 Flyway migrations**, including seed data for 3 asset types, 5 users, 15 wallets, and treasury system wallets.
- Enforced data integrity at the database level with **PostgreSQL CHECK constraints** (non-negative balances, positive amounts) and **UNIQUE constraints** (one wallet per user per asset type, unique reference IDs).

### Cloud / DevOps

- Containerized the service with a **multi-stage Docker build** (Alpine Linux) and orchestrated local development using **Docker Compose** with health-checked PostgreSQL.
- Set up a fully automated **CI/CD pipeline** with **GitHub Actions** that builds, pushes images to **GitHub Container Registry**, and deploys to **AWS EC2** via SSH.
- Integrated **AWS Secrets Manager** with **Spring Cloud AWS** for secure, externalized database credential management, eliminating hardcoded secrets from the codebase.
- Configured **Nginx** as a reverse proxy with **Certbot / Let's Encrypt** for automated **HTTPS/TLS** termination and **DuckDNS** for dynamic DNS.

### API Design

- Developed a RESTful API with **13 endpoints** across wallet, transaction, and asset-type domains, including paginated transaction history and balance reconciliation.
- Auto-generated interactive **OpenAPI / Swagger documentation** using **Springdoc OpenAPI 2.8**, enabling faster onboarding and integration testing.
- Applied **Jakarta Bean Validation** on all request DTOs to reject malformed input at the controller layer before reaching business logic.

### System Design / Architecture

- Architected a **layered microservice** (Controller → Service → Repository → Database) with clear separation of concerns and a DTO-based contract between layers.
- Designed for **financial-grade reliability**: non-negative balance enforcement, immutable transaction ledger, and correlation-ID-based request tracing for end-to-end debugging.
- Configured **HikariCP** connection pooling (20 max connections) with leak detection and **Spring Actuator** health endpoints for production monitoring and observability.

---

## One-Liner Summary (for resume header)

> Built a closed-loop virtual currency wallet service with double-entry ledger, idempotent transactions, and concurrency-safe operations using Java 21, Spring Boot, PostgreSQL, Docker, and AWS.
