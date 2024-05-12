# ETL Inventory Sync

Inventory ETL sync job. Extracts and loads inventory snapshots with idempotency by sync run. Uses Transactional Outbox for event publishing. Retry, timeout and idempotency patterns applied.

## Features

- **Idempotency**: Runs keyed by `syncId`; duplicate syncs skip processing.
- **Retry**: Outbox publishing with Spring Retry (3 attempts, 800ms backoff).
- **Timeout**: Sync phase bounded by configurable timeout (default 40s).
- **Transactional Outbox**: Snapshot and outbox in same transaction; scheduler publishes.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Java 17, Spring Boot 3.2
- Spring Data JPA, Spring Retry
- H2 (development)

## Build and Run

```bash
mvn clean package
java -jar target/etl-inventory-sync-1.0.0.jar
```

With sync id (idempotency):

```bash
java -jar target/etl-inventory-sync-1.0.0.jar --syncId=2024-01-15-inv
```

## CI/CD

Jenkinsfile: Checkout, Build, Test, optional Sync. Timeout 20 min; retry 2.

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| app.sync-timeout-seconds | 40 | Max duration for sync phase. |

## License

MIT
