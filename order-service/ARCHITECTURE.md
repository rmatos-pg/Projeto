# Order Service - Architecture

## Component Diagram

```
+------------------+     Idempotency-Key      +-------------------+
|   Client         | -----------------------> | OrderController    |
+------------------+     POST /api/orders     +--------+----------+
                                               |        |
                                               v        v
                                    +----------+--------+----------+
                                    | OrderApplicationService      |
                                    | - createOrder (idempotent)   |
                                    | - publishOutboxEvent (retry) |
                                    +----------+-------------------+
                                               |
                    +--------------------------+--------------------------+
                    |                          |                          |
                    v                          v                          v
         +------------------+       +------------------+       +------------------+
         | OrderRepository  |       | OutboxEventRepository |   | IdempotencyKey   |
         | (orders)         |       | (outbox_events)       |   | Repository       |
         +------------------+       +------------------+       +------------------+
                    |                          |
                    |              +-----------+-----------+
                    |              | OutboxPublisherScheduler |
                    |              | (fixedDelay 2s, timeout 5s)|
                    |              +---------------------------+
                    |
                    v
         +------------------+
         | DB (same TX)     |
         +------------------+
```

## Flow: Create Order (Idempotent)

1. Request arrives with `Idempotency-Key` header.
2. Lookup key in `idempotency_keys`. If found and not expired, return stored response (idempotent reply).
3. Otherwise: in a single transaction:
   - Insert `Order`.
   - Insert `OutboxEvent` (status PENDING).
   - Insert `IdempotencyKey` with serialized response.
4. Return created order.

## Flow: Transactional Outbox

1. Outbox events are written in the same transaction as the aggregate (Order).
2. Background job (`OutboxPublisherScheduler`) polls `outbox_events` where status = PENDING.
3. For each event: call `publishOutboxEvent` (with retry: 3 attempts, 1s backoff).
4. On success: set status to PUBLISHED. On failure: set status to FAILED and store last error.

## Retry and Timeout

- **Retry**: Applied to `publishOutboxEvent` via `@Retryable` (maxAttempts=3, backoff=1000ms).
- **Timeout**: Scheduler loop uses a 5s deadline per publish; executor for outbox has bounded queue and termination wait.
