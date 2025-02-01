# API Gateway - Architecture

## Component Diagram

```
+--------+   /api/users/*           +------------------+
| Client |   /api/notifications/*   | Express app      |
+--------+ -----------------------> +--------+---------+
                                               |
                                    +----------+----------+
                                    | proxyRouter         |
                                    | /users* -> userClient
                                    | /notifications* -> notificationClient
                                    +----------+----------+
                                               |
                    +--------------------------+--------------------------+
                    | withRetry(request, 3, 300ms)                        |
                    | Axios client with timeout = DOWNSTREAM_TIMEOUT_MS   |
                    +--------------------------+--------------------------+
                                               |
                    +--------------------------+--------------------------+
                    |                          |
                    v                          v
            +----------------+         +----------------+
            | User Service   |         | Notification   |
            | (e.g. :3000)   |         | Service (:3001)|
            +----------------+         +----------------+
```

## Timeout and Retry

- Each downstream request is sent with Axios `timeout: DOWNSTREAM_TIMEOUT_MS`.
- On failure (network or 5xx), the gateway retries up to RETRY_ATTEMPTS with RETRY_DELAY_MS between attempts.
- After exhausting retries or on 4xx, the gateway returns an appropriate status and error body.
