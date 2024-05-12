# ETL Payment Pipeline

Transform and load phase for payment data. Loads into warehouse with idempotency by batch, publishes events via Transactional Outbox. Retry, timeout and outbox patterns applied.

## Features

- **Idempotency**: Batch identified by `batchId`; duplicate batches skip processing.
- **Retry**: Outbox publishing with Spring Retry (3 attempts, 500ms backoff).
- **Timeout**: Load phase bounded by configurable timeout (default 45s).
- **Transactional Outbox**: Warehouse insert and outbox in same transaction; scheduler publishes.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Java 17, Spring Boot 3.2
- Spring Data JPA, Spring Retry
- H2 (development)

## Build and Run

```bash
mvn clean package
java -jar target/etl-payment-pipeline-1.0.0.jar
```

With batch id (idempotency):

```bash
java -jar target/etl-payment-pipeline-1.0.0.jar --batchId=2024-01-15-payments
```

## CI/CD

Jenkinsfile: Checkout, Build, Test, optional Run Pipeline. Timeout 25 min; retry 2.

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| app.load-timeout-seconds | 45 | Max duration for load phase. |

## License

MIT
