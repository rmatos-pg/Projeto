# Backend Portfolio

Repository containing backend services and APIs used as portfolio pieces. All projects apply production-oriented patterns: retry, timeout, idempotency where applicable, and Transactional Outbox for reliable event publishing.

## Projects

| Project | Stack | Patterns |
|---------|-------|----------|
| order-service | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox |
| payment-api | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox |
| inventory-service | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox |
| user-service | Node.js, TypeScript, Express | Idempotency, Timeout, Retry utilities |
| notification-service | Node.js, TypeScript, Express | Transactional Outbox, Retry, Timeout |
| api-gateway | Node.js, TypeScript, Express | Timeout, Retry (downstream) |

Each project includes a README (build, run, API, configuration) and an ARCHITECTURE document with component diagrams.

## License

MIT
