# ETL User Migration

User migration ETL job. Migrates user data to target with idempotency by migration run. Uses Transactional Outbox for event publishing. Retry, timeout and idempotency patterns applied.

## Features

- **Idempotency**: Runs keyed by `migrationId`; duplicate runs skip processing.
- **Retry**: Outbox publishing with configurable retry (default 3 attempts, 1s delay).
- **Timeout**: Migration phase bounded by configurable timeout (default 30s).
- **Transactional Outbox**: Target insert and outbox in same transaction; publisher runs asynchronously.

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

With migration id (idempotency):

```bash
node dist/index.js --migrationId=2024-01-15-users
```

## CI/CD

Jenkinsfile: Checkout, Build, optional Run Migration. Timeout 15 min; retry 2.

## Configuration

| Env | Default | Description |
|-----|---------|-------------|
| OPERATION_TIMEOUT_MS | 30000 | Max duration for migration. |
| RETRY_ATTEMPTS | 3 | Retries for outbox publish. |
| RETRY_DELAY_MS | 1000 | Delay between retries. |
| OUTBOX_POLL_MS | 2000 | Outbox poll interval. |

## License

MIT
