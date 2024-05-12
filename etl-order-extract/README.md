# ETL Order Extract

Extract phase of an order ETL pipeline. Reads from source systems, stages data, and publishes events via Transactional Outbox. Implements retry, timeout and idempotency for reliable batch execution.

## Features

- **Idempotency**: Job runs keyed by `runKey`; duplicate runs return without re-processing.
- **Retry**: Outbox publishing uses Spring Retry (3 attempts, 1s backoff).
- **Timeout**: Extract loop bounded by configurable timeout (default 60s).
- **Transactional Outbox**: Staging insert and outbox row in same transaction; scheduler publishes asynchronously.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Java 17, Spring Boot 3.2, Spring Batch
- Spring Data JPA, Spring Retry
- H2 (development); use PostgreSQL for production.

## Build and Run

```bash
./mvnw clean package
java -jar target/etl-order-extract-1.0.0.jar
```

With custom run key (idempotency):

```bash
java -jar target/etl-order-extract-1.0.0.jar --runKey=2024-01-15-batch-1
```

## CI/CD

Jenkinsfile provided. Pipeline stages: Checkout, Build, Test, optional Run ETL. Pipeline timeout 30 minutes; retry 2 on failure.

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| app.extract-timeout-seconds | 60 | Max duration for extract phase. |
| app.idempotency-run-key-ttl-hours | 24 | Job run key retention. |

## License

MIT
