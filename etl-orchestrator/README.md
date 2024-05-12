# ETL Orchestrator

Orchestrates ETL pipeline steps (extract, load) with retry, timeout and idempotency. Calls downstream services with configurable retry and timeout. Publishes completion events via Transactional Outbox.

## Features

- **Idempotency**: Runs keyed by `runId`; duplicate runs skip processing.
- **Retry**: Downstream HTTP calls and outbox publishing with configurable retry (default 3 attempts, 2s delay).
- **Timeout**: Per-call timeout (10s) and overall orchestration timeout (default 60s).
- **Transactional Outbox**: Run completion and outbox in same transaction; publisher runs asynchronously.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Node.js 18+, TypeScript 5
- Axios, better-sqlite3 (development)

## Build and Run

```bash
npm install
npm run build
npm start
```

With run id (idempotency):

```bash
node dist/index.js --runId=2024-01-15-full
```

## CI/CD

Jenkinsfile: Checkout, Build, optional Orchestrate. Timeout 20 min; retry 2.

## Configuration

| Env | Default | Description |
|-----|---------|-------------|
| OPERATION_TIMEOUT_MS | 60000 | Max duration for orchestration. |
| RETRY_ATTEMPTS | 3 | Retries for HTTP and outbox. |
| RETRY_DELAY_MS | 2000 | Delay between retries. |
| EXTRACT_SERVICE_URL | http://localhost:8080 | Extract service base URL. |
| LOAD_SERVICE_URL | http://localhost:8081 | Load service base URL. |

## License

MIT
