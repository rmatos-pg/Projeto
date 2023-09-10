# Payment API

REST API for payment processing with idempotency, retry, timeout and Transactional Outbox. Designed for integration with order and notification systems.

## Features

- **Idempotency**: All payment creation requests require `Idempotency-Key` header. Duplicate keys return the original response.
- **Retry**: Outbox publishing uses Spring Retry (3 attempts, 500ms backoff).
- **Timeout**: Outbox processing runs with a 5s deadline per cycle; executor has bounded queue and termination.
- **Transactional Outbox**: Payment and outbox row are persisted in one transaction; background job publishes events.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Java 17, Spring Boot 3.2
- Spring Data JPA, Spring Retry
- H2 (development); use PostgreSQL or similar for production.

## Build and Run

```bash
./mvnw clean package
java -jar target/payment-api-1.0.0.jar
```

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/payments | Process payment. Header: `Idempotency-Key`. Body: `orderId`, `customerId`, `amount`. |

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| app.operation-timeout-seconds | 15 | Timeout for long operations. |
| app.idempotency-ttl-hours | 24 | Idempotency key retention. |

## License

MIT
