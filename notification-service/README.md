# Notification Service

Notification creation and delivery with Transactional Outbox, retry and timeout. Notifications are stored and an outbox event is written in the same transaction; a background publisher sends events with retry and timeout.

## Features

- **Transactional Outbox**: Insert notification and outbox row in one transaction; publisher runs asynchronously.
- **Retry**: Outbox publishing uses configurable retry (default 3 attempts, 1s delay).
- **Timeout**: Per-event publish timeout and overall operation timeout for API handlers.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Node.js 18+, TypeScript 5
- Express, better-sqlite3 (replace with PostgreSQL for production)

## Build and Run

```bash
npm install
npm run build
npm start
```

## API

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/notifications | Create notification. Body: `userId`, `channel`, `subject` (optional), `body`. |
| GET | /api/notifications/:id | Get notification by id. |

## Configuration

| Env | Default | Description |
|-----|---------|-------------|
| PORT | 3001 | HTTP port. |
| OPERATION_TIMEOUT_MS | 10000 | Handler timeout. |
| OUTBOX_POLL_MS | 2000 | Outbox poll interval. |
| OUTBOX_PUBLISH_TIMEOUT_MS | 5000 | Timeout per outbox publish. |
| RETRY_ATTEMPTS | 3 | Retries for publish. |
| RETRY_DELAY_MS | 1000 | Delay between retries. |

## License

MIT
