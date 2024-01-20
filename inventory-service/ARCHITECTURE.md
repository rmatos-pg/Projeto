# Inventory Service - Architecture

## Component Diagram

```
+--------+   Idempotency-Key     +---------------------+
| Client |   POST /reserve       | InventoryController |
+--------+   + body              +----------+----------+
                                    |
                                    v
                    +---------------+------------------+
                    | InventoryApplicationService       |
                    | - reserveStock (idempotent)      |
                    | - publishOutbox (retry)          |
                    +---------------+------------------+
                                    |
        +---------------------------+---------------------------+
        |                           |                           |
        v                           v                           v
+----------------+        +-------------------+        +-------------------+
| StockItemRepo  |        | InventoryOutboxRepo|        | IdempotencyEntry  |
+----------------+        +-------------------+        +-------------------+
        |                           |
        |               +-----------+-----------+
        |               | OutboxPublishScheduler |
        |               | 2.5s delay, 5s timeout |
        |               +------------------------+
        v
+----------------+ (single transaction)
| DB             |
+----------------+
```

## Flows

**Reserve (idempotent):** Check idempotency key; if hit return cached. Else in one TX: upsert StockItem, insert Outbox (PENDING), insert IdempotencyEntry; return item.

**Outbox:** Scheduler loads PENDING, calls publishOutbox (retry 3x, 800ms). Success: PUBLISHED; failure: FAILED.
