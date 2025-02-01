# API Gateway

BFF/API Gateway that proxies requests to user and notification services with configurable timeout and retry. Suitable for a portfolio backend that aggregates multiple services.

## Features

- **Timeout**: Downstream HTTP calls use a configurable timeout (default 8s).
- **Retry**: Failed calls are retried (default 3 attempts, 300ms delay) before returning an error to the client.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Tech Stack

- Node.js 18+, TypeScript 5
- Express, Axios

## Build and Run

```bash
npm install
npm run build
npm start
```

Ensure user-service and notification-service are running if you use the default URLs.

## API

| Method | Path | Proxies to |
|--------|------|------------|
| ALL | /api/users* | USER_SERVICE_URL/api/users* |
| ALL | /api/notifications* | NOTIFICATION_SERVICE_URL/api/notifications* |

## Configuration

| Env | Default | Description |
|-----|---------|-------------|
| PORT | 4000 | Gateway HTTP port. |
| DOWNSTREAM_TIMEOUT_MS | 8000 | Timeout for downstream requests. |
| RETRY_ATTEMPTS | 3 | Retries on failure. |
| RETRY_DELAY_MS | 300 | Delay between retries. |
| USER_SERVICE_URL | http://localhost:3000 | User service base URL. |
| NOTIFICATION_SERVICE_URL | http://localhost:3001 | Notification service base URL. |

## License

MIT
