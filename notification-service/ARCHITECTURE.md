# Notification Service - Architecture

## Component Diagram

```
+--------+   POST/GET /api/notifications   +------------------+
| Client | ------------------------------> | Express app      |
+--------+                                 +--------+---------+
                                                    |
                                                    v
                                    +---------------+---------------+
                                    | notificationRouter             |
                                    | POST: createNotification       |
                                    | GET /:id: getNotification      |
                                    +---------------+---------------+
                                                    |
                          +-------------------------+-------------------------+
                          | createNotification (transaction)                    |
                          | 1. INSERT notifications                             |
                          | 2. INSERT notification_outbox (PENDING)             |
                          +-------------------------+-------------------------+
                                                    |
                                    +---------------+---------------+
                                    | startOutboxPublisher (loop)    |
                                    | - find PENDING                 |
                                    | - publish with retry + timeout |
                                    | - mark PUBLISHED or FAILED     |
                                    +---------------+---------------+
```

## Transactional Outbox

1. POST creates a notification row and an outbox row in a single SQLite transaction.
2. Background loop polls `notification_outbox` for status = PENDING.
3. For each row: publish with `withRetry` and `withTimeout`; on success set PUBLISHED, on failure set FAILED.

## Retry and Timeout

- Publish: retry 3x with 1s delay; each attempt capped by OUTBOX_PUBLISH_TIMEOUT_MS.
- API: create handler wrapped in OPERATION_TIMEOUT_MS.
