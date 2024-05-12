# ETL Inventory Sync - Architecture

## Component Diagram

```
+------------+   syncId (idempotent)   +------------------+
| Jenkins    |   java -jar --syncId    | SyncJobRunner    |
+------------+   timeout: 20 min        +--------+---------+
     | retry(2)                        |        |
     |                                 v        |
     |                    +---------------------+------------------+
     |                    | InventorySyncService                   |
     |                    | - sync (idempotent by syncId)          |
     |                    | - publishOutbox (retry 3x)             |
     |                    +---------------------+------------------+
     |                                          |
     |              +----------------------------+----------------------------+
     |              |                            |                            |
     |              v                            v                            v
     |     +----------------+           +----------------+           +----------------+
     |     | InventorySnap  |           | SyncOutbox     |           | SyncRun       |
     |     | (syncId+sku)   |           | (PENDING)      |           | (syncId UK)   |
     |     +----------------+           +----------------+           +----------------+
     |              |                            |
     |              |                +-----------+-----------+
     |              |                | OutboxScheduler       |
     |              |                | 2s delay, 5s timeout  |
     |              |                +------------------------+
     |              v
     |     +----------------+ (single transaction)
     |     | DB             |
     |     +----------------+
```

## ETL Flow

1. **Sync**: Receive `syncId`. If SyncRun exists, skip (idempotent).
2. In one transaction: insert InventorySnapshot, SyncOutbox (PENDING), SyncRun.
3. **Outbox**: Scheduler publishes PENDING with retry; marks PUBLISHED or FAILED.

## Retry and Timeout

- **Pipeline**: 20 min timeout; retry 2.
- **Sync**: 40s timeout; idempotent by (syncId, sku).
- **Outbox**: Retry 3x, 800ms backoff; 5s cap per cycle.
