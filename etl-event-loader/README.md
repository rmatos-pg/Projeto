# ETL Event Loader

Event loading ETL job. Loads domain events into event store with idempotency by load run. Uses Transactional Outbox for downstream publishing. Retry, timeout and idempotency patterns applied.

## Features

- **Idempotency**: Runs keyed by `loadId`; duplicate runs skip processing.
- **Retry**: Outbox publishing with configurable retry (default 3 attempts, 800ms delay).
- **Timeout**: Load phase bounded by configurable timeout (default 25s).
- **Transactional Outbox**: Event store insert and outbox in same transaction; publisher runs asynchronously.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Node.js 18+, TypeScript 5
- better-sqlite3 (development; use PostgreSQL for production)

## Build and Run

```bash
npm install
npm run build
npm start
```

With load id (idempotency):

```bash
node dist/index.js --loadId=2024-01-15-events
```

## CI/CD

Jenkinsfile: Checkout, Build, optional Load. Timeout 15 min; retry 2.

## Configuration

| Env | Default | Description |
|-----|---------|-------------|
| OPERATION_TIMEOUT_MS | 25000 | Max duration for load phase. |
| RETRY_ATTEMPTS | 3 | Retries for outbox publish. |
| RETRY_DELAY_MS | 800 | Delay between retries. |
| OUTBOX_POLL_MS | 2000 | Outbox poll interval. |

## License

MIT
