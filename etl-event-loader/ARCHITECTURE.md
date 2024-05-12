# ETL Event Loader - Architecture

## Component Diagram

```
+------------+   loadId (idempotent)   +------------------+
| Jenkins    |   node dist/index.js   | runLoad         |
+------------+   timeout: 15 min      +--------+--------+
     | retry(2)                        |        |
     |                                 v        |
     |                    +---------------------+------------------+
     |                    | runLoad (idempotent by loadId)         |
     |                    | - check load_runs                      |
     |                    | - insert event_store + outbox (same TX)|
     |                    +---------------------+------------------+
     |                                          |
     |              +----------------------------+-------------------+
     |              |                            |                    |
     |              v                            v                    v
     |     +----------------+           +----------------+   +----------------+
     |     | event_store   |           | loader_outbox  |   | load_runs     |
     |     | (load_id+agg) |           | (PENDING)      |   | (load_id PK)  |
     |     +----------------+           +----------------+   +----------------+
     |              |                            |
     |              |                +-----------+-----------+
     |              |                | startOutboxPublisher   |
     |              |                | retry + timeout        |
     |              |                +------------------------+
     |              v
     |     +----------------+ (single transaction)
     |     | DB             |
     |     +----------------+
```

## ETL Flow

1. **Load**: Receive `loadId`. If load_runs has it, skip (idempotent).
2. In one transaction: insert event_store, loader_outbox (PENDING), update load_runs.
3. **Outbox**: Publisher polls PENDING; publishes with retry and timeout; marks PUBLISHED or FAILED.

## Retry and Timeout

- **Pipeline**: 15 min timeout; retry 2.
- **Load**: 25s timeout; idempotent by (loadId, aggregate_id, event_type).
- **Outbox**: Retry 3x, 800ms delay; 5s timeout per publish.
