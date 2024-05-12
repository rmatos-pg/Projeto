# Backend Portfolio

Repository containing backend services, APIs and ETL pipelines used as portfolio pieces. All projects apply production-oriented patterns: retry, timeout, idempotency where applicable, and Transactional Outbox for reliable event publishing.

## Services

| Project | Stack | Patterns |
|---------|-------|----------|
| order-service | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox |
| payment-api | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox |
| inventory-service | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox |
| user-service | Node.js, TypeScript, Express | Idempotency, Timeout, Retry utilities |
| notification-service | Node.js, TypeScript, Express | Transactional Outbox, Retry, Timeout |
| api-gateway | Node.js, TypeScript, Express | Timeout, Retry (downstream) |

## ETL Pipelines

| Project | Stack | Patterns | CI/CD |
|---------|-------|----------|-------|
| etl-order-extract | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox | Jenkinsfile |
| etl-payment-pipeline | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox | Jenkinsfile |
| etl-inventory-sync | Java 17, Spring Boot 3 | Idempotency, Retry, Timeout, Transactional Outbox | Jenkinsfile |
| etl-user-migration | Node.js, TypeScript | Idempotency, Retry, Timeout, Transactional Outbox | Jenkinsfile |
| etl-event-loader | Node.js, TypeScript | Idempotency, Retry, Timeout, Transactional Outbox | Jenkinsfile |
| etl-orchestrator | Node.js, TypeScript | Idempotency, Retry, Timeout, Transactional Outbox | Jenkinsfile |

Each project includes a README (build, run, API/usage, configuration), an ARCHITECTURE document with component diagrams, and Jenkinsfile where applicable.

## License

MIT
