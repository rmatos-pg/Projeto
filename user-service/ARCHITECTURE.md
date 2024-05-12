# User Service - Architecture

## Component Diagram

```
+--------+   Idempotency-Key (optional)   +------------------+
| Client |   POST /api/users              | Express app      |
+--------+   GET /api/users/:id            +--------+---------+
                                               |
                          +--------------------+--------------------+
                          | idempotencyMiddleware                   |
                          | - if key present and cached -> 200     |
                          | - else set req.idempotencyKey, next()   |
                          +--------------------+--------------------+
                                               |
                          +--------------------+--------------------+
                          | userRouter                              |
                          | POST: createUser + saveIdempotency      |
                          | GET /:id: findUserById                  |
                          +--------------------+--------------------+
                                               |
                    +--------------------------+--------------------------+
                    |                          |                          |
                    v                          v                          v
            +---------------+          +----------------+          +----------------+
            | users store   |          | idempotency     |          | retry/timeout  |
            | (SQLite)      |          | store (memory)  |          | (lib/retry)    |
            +---------------+          +----------------+          +----------------+
```

## Idempotency Flow

1. Middleware reads `Idempotency-Key`. If missing, pass through.
2. If key present and cached response exists and not expired: return cached response, skip handler.
3. Otherwise attach key to request; handler runs; on success save response in idempotency store with TTL.

## Timeout and Retry

- Create user handler wraps work in `withTimeout(operationTimeoutMs)`.
- `withRetry` available for external calls or internal retryable steps.
