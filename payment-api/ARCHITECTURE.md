# Payment API - Architecture

## Component Diagram

```
+--------+   Idempotency-Key    +------------------+
| Client |   POST /api/payments | PaymentController|
+--------+   + body             +--------+--------+
                                    |
                                    v
                    +---------------+---------------+
                    | PaymentService                 |
                    | - processPayment (idempotent)  |
                    | - publishOutbox (retry)       |
                    +---------------+---------------+
                                    |
        +---------------------------+---------------------------+
        |                           |                           |
        v                           v                           v
+---------------+         +------------------+         +------------------+
| PaymentRepo   |         | PaymentOutboxRepo |         | IdempotencyRepo   |
+---------------+         +------------------+         +------------------+
        |                           |
        |               +-----------+-----------+
        |               | OutboxScheduler       |
        |               | fixedDelay 3s, 5s cap |
        |               +------------------------+
        v
+---------------+ (single transaction)
| DB            |
+---------------+
```

## Idempotency Flow

1. Extract `Idempotency-Key` from request.
2. If key exists and not expired: return stored response (no new payment).
3. Else: in one transaction create Payment, PaymentOutbox (PENDING), IdempotencyRecord; return payment.

## Outbox and Retry

- Outbox rows written in same transaction as payment.
- Scheduler picks PENDING events; for each calls `publishOutbox` (retry 3x, 500ms backoff).
- Timeout: 5s total per scheduler run; failed events marked FAILED.
