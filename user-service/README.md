# User Service

REST API for user management with idempotency, configurable timeout and retry support. Suited for registration and profile creation in a mid-tier portfolio backend.

## Features

- **Idempotency**: `Idempotency-Key` header on `POST /api/users`. Duplicate keys return the same response without creating another user.
- **Timeout**: Create-user operation wrapped with configurable timeout (default 15s).
- **Retry**: Shared `withRetry` and `withTimeout` utilities for downstream or internal operations.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Node.js 18+, TypeScript 5
- Express, better-sqlite3 (in-memory for demo; use PostgreSQL in production)

## Build and Run

```bash
npm install
npm run build
npm start
```

Development:

```bash
npm run dev
```

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/users | Create user. Optional header: `Idempotency-Key`. Body: `email`, `name`. |
| GET | /api/users/:id | Get user by id. |

## Configuration

| Env | Default | Description |
|-----|---------|-------------|
| PORT | 3000 | HTTP port. |
| OPERATION_TIMEOUT_MS | 15000 | Timeout for create operation. |
| IDEMPOTENCY_TTL_SECONDS | 86400 | Idempotency key TTL. |
| RETRY_ATTEMPTS | 3 | Retry attempts for retryable operations. |
| RETRY_DELAY_MS | 500 | Delay between retries. |

## License

MIT
