# Inventory Service

Stock reservation service with idempotency, retry, timeout and Transactional Outbox. Used to reserve or adjust inventory and publish events reliably.

## Features

- **Idempotency**: `Idempotency-Key` on `POST /api/inventory/reserve`; repeated keys return cached result.
- **Retry**: Outbox publishing with Spring Retry (3 attempts, 800ms backoff).
- **Timeout**: Bounded scheduler run (5s) and executor for outbox processing.
- **Transactional Outbox**: Reserve and outbox insert in one transaction; scheduler publishes events.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Java 17, Spring Boot 3.2
- Spring Data JPA, Spring Retry
- H2 (development)

## Build and Run

```bash
./mvnw clean package
java -jar target/inventory-service-1.0.0.jar
```

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/inventory/reserve | Reserve stock. Header: `Idempotency-Key`. Body: `sku`, `name`, `quantity`. |

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| app.operation-timeout-ms | 10000 | Operation timeout. |
| app.idempotency-ttl-hours | 24 | Idempotency key TTL. |

## License

MIT
