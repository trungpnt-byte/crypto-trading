# Crypto Trading System

A **reactive Spring Boot Java service** for crypto trading with real-time price aggregation, wallet management, and
trade execution. Built using WebFlux and R2DBC for high concurrency and scalability, this service persists data in SQL
databases and provides RESTful APIs for trading and wallet operations.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Database](#database)
- [Build & Run](#build--run)
- [Docker (Optional)](#docker-optional)
- [API Endpoints](#api-endpoints)
- [Reactive Scheduler](#reactive-scheduler)
- [Dependencies](#dependencies)
- [Testing](#testing)
- [Versioning](#versioning)
- [Contribution](#contribution)
- [License](#license)
- [Future Enhancements](#future-enhancements)

---

## Overview

This service enables:

- Real-time **price aggregation** from Binance and Huobi
- **Buy/Sell trading** for supported crypto pairs (ETHUSDT, BTCUSDT),
- **Wallet management** and balance tracking
- **Transaction history** retrieval
- **RESTful reactive APIs** with Spring WebFlux
- **SQL persistence** via R2DBC (PostgreSQL production / H2 in-memory for testing)
- **OpenAPI / Swagger** documentation for API exploration

---

## Tech Stack

- **Java 21**
- **Spring Boot 3.x** (WebFlux, Security)
- **Reactive SQL:** Spring Data R2DBC
- **Database:** PostgreSQL / H2 (testing)
- **Reactive programming:** Project Reactor
- **Build tool:** Gradle 8.x (wrapper included)
- **Testing:** JUnit 5, MockK / Mockito, Testcontainers
- **Security:** Spring Security + JWT for authentication/authorization

---

## Features

1. **Price Aggregation**
    - Fetches latest bid/ask prices from Binance and Huobi every 10 seconds
    - Aggregates best prices and stores them in the database
    - **Bid** = price to SELL, **Ask** = price to BUY

2. **Trading API**
    - Buy/sell ETHUSDT and BTCUSDT using the latest aggregated price
    - Updates user wallet balances in real time

3. **Wallet Management**
    - Retrieve wallet balances (crypto + USDT)
    - Initial user balance: 50,000 USDT
    - Reactive, non-blocking access

4. **Transaction History**
    - List all user trading transactions
    - Supports filtering and sorting

---

## Requirements

- **JDK 21**
- **Gradle 8.x**
- Running SQL database (PostgreSQL recommended) by container with docker-compose setup
- macOS / Linux / Windows for development
- Optional: Docker for containerized setup

---

## Configuration

Application configuration via `src/main/resources/application.yml`. Override using environment variables (e.g.,
`SPRING_DATASOURCE_URL`, `SPRING_PROFILES_ACTIVE`).

Profiles: `dev`, `pp`, `prod`.

Example:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/crypto
    username: user
    password: pass
  flyway:
    enabled: true
logging:
  level:
    com.example: INFO
```

## Future-Proof Design Notes

This section outlines design considerations for scaling the Crypto Trading System safely and efficiently in production.

---

### 1. Multi-Node / Distributed Deployment

- **Scheduler Coordination:**
    - Only one instance should perform the **price aggregation** to avoid duplicate writes or race conditions.
    - Solutions:
        - Use a **distributed lock** (e.g., Redis Redlock or Postgres advisory locks) to ensure a single active
          scheduler.
        - Leader election if deploying on Kubernetes (e.g., via `leader-elector` pattern).

- **Stateless Services:**
    - REST APIs, wallet management, and trade execution are stateless and can scale horizontally.
    - Store all **critical state in the database** to avoid inconsistencies.

---

### 2. Price Aggregation & High-Frequency Updates

- **Reactive & Non-Blocking:**
    - Current WebFlux + R2DBC ensures **non-blocking I/O**, supporting high concurrency.

- **Batch Aggregation:**
    - Aggregate best prices in memory first (O(n)) and persist in **single batch** to reduce DB writes.

- **Caching Layer (Optional):**
    - For extremely high read traffic (e.g., `/api/prices`), maintain an **in-memory cache** (Redis or Caffeine) with
      TTL 1-2s to reduce DB hits.

- **Handling Missing Data:**
    - If one exchange fails, still use available prices and log discrepancies.
    - Optionally, mark incomplete aggregation runs in DB for later reconciliation.

---

### 3. Wallet & Trade Safety

- **Atomic Updates:**
    - Use **database transactions** to update wallet balances and log trades together.
    - Optimistic locking or versioning prevents **double-spending** during concurrent trades.

- **Price Validation:**
    - Include timestamp with each aggregated price. Reject trades older than X milliseconds to prevent stale-price
      execution.

- **Concurrency Controls:**
    - Use **row-level locks** in DB if multiple trades affect the same user simultaneously.

---

### 4. Database Considerations

- **Hot Tables:**
    - Prices table updated frequently: use **index on symbol** and/or **partitioning** for high-frequency writes.
- **Transaction History Growth:**
    - Archive old transactions periodically or move to a **cold storage** table to maintain performance.

- **Read vs Write Scaling:**
    - Consider **read replicas** for wallet and transaction queries if read-heavy.

---

### 5. Observability & Reliability

- **Metrics:**
    - Monitor scheduler latency, trade execution time, DB write times.
    - Prometheus metrics for number of aggregation runs, failed trades, and wallet inconsistencies.

- **Alerts:**
    - Trigger alerts on:
        - Scheduler failures
        - Price deviations between exchanges exceeding threshold
        - Trade failures or concurrency conflicts

- **Logging Best Practices:**
    - Avoid logging sensitive data (balances, JWTs).
    - Use structured logging for easy aggregation in ELK/Datadog.

---

### 6. Extensibility

- **Adding Exchanges:**
    - Scheduler design allows plugging in new exchange APIs without changing aggregation core logic.
    - Simply implement a `MarketTickerProvider` interface for the new exchange.

- **Adding Crypto Pairs:**
    - Support dynamic addition/removal of trading pairs via **configurable `SUPPORTED_PAIRS`**.

- **WebSocket Integration:**
    - Future: Stream real-time prices via WebSocket to clients, reducing polling overhead.

- **High Availability:**
    - Deploy multiple nodes behind a load balancer
    - Ensure **single active scheduler** per cluster
    - Use **circuit breakers** for external exchange calls

---

### 7. Security & Compliance

- **JWT Refresh Tokens:**
    - Complete the refresh token workflow to allow long-lived sessions without compromising security.

- **Role-Based Access:**
    - Differentiate between regular users and admin roles (e.g., ability to view all transactions).

- **Input Validation:**
    - Validate trade quantities, symbols, and prices to prevent injection or manipulation.

- **Audit Logging:**
    - Record critical events: trades executed, wallet updates, and scheduler runs.
    - Useful for regulatory compliance and debugging.

---

### 8. Scaling Strategy Summary

| Layer                 | Scaling Strategy                                          |
|-----------------------|-----------------------------------------------------------|
| Scheduler             | Distributed lock / leader election to prevent duplicates  |
| Database writes       | Batch writes, row-level locking, indexes on hot tables    |
| Read-heavy APIs       | Cache layer (Redis/Caffeine), read replicas               |
| Reactive endpoints    | WebFlux + R2DBC ensures non-blocking, high-concurrency    |
| Multi-node deployment | Stateless APIs, DB-centric state, single scheduler leader |
| Observability         | Prometheus metrics, structured logging, alerts            |
| Security              | JWT, role-based access, input validation, audit logs      |

---

**Conclusion:**  
The system is designed to scale horizontally, handle high-frequency updates, and maintain **data consistency, safety,
and observability**. Future enhancements can be integrated without breaking core functionality.
