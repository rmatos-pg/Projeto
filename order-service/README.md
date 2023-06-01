# Order Service

Backend service for order processing in an e-commerce context. Implements resilience and consistency patterns suitable for production: retry, configurable timeouts, idempotency keys, and Transactional Outbox for reliable event publishing.

## Features

- **Idempotency**: `Idempotency-Key` header on `POST /api/orders` ensures duplicate requests return the same result without side effects.
- **Retry**: Spring Retry on outbox publishing with fixed backoff (3 attempts, 1s delay).
- **Timeout**: Configurable operation timeout and dedicated executor for outbox publisher with bounded execution.
- **Transactional Outbox**: Order creation and outbox event insert run in the same transaction; a scheduler publishes pending events asynchronously.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for diagrams and component description.

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA, Spring Retry
- H2 (in-memory; replace with PostgreSQL for production)

## Build and Run

```bash
./mvnw clean package
java -jar target/order-service-1.0.0.jar
```

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/orders | Create order. Required header: `Idempotency-Key`. Body: `customerId`, `totalAmount`. |

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| app.operation-timeout-ms | 30000 | Max time for long-running operations (ms). |
| app.idempotency-key-ttl-hours | 24 | TTL for stored idempotency keys. |

## License

MIT
