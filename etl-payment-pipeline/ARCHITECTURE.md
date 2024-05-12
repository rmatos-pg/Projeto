# ETL Payment Pipeline - Architecture

## Component Diagram

```
+------------+   batchId (idempotent)   +------------------+
| Jenkins    |   java -jar --batchId     | LoadJobRunner    |
+------------+   timeout: 25 min         +--------+--------+
     | retry(2)                          |        |
     |                                   v        |
     |                    +------------------------+------------------+
     |                    | PaymentLoadService                          |
     |                    | - loadBatch (idempotent by batchId)        |
     |                    | - publishOutbox (retry 3x)                |
     |                    +------------------------+------------------+
     |                                          |
     |              +----------------------------+----------------------------+
     |              |                            |                            |
     |              v                            v                            v
     |     +----------------+           +----------------+           +----------------+
     |     | PaymentWarehouse|           | PipelineOutbox |           | BatchRun       |
     |     | (batchId+srcId) |           | (PENDING)      |           | (batchId UK)   |
     |     +----------------+           +----------------+           +----------------+
     |              |                            |
     |              |                +-----------+-----------+
     |              |                | OutboxScheduler       |
     |              |                | 2.5s delay, 5s timeout  |
     |              |                +------------------------+
     |              v
     |     +----------------+ (single transaction)
     |     | DB             |
     |     +----------------+
```

## ETL Flow

1. **Load**: Receive `batchId`. If BatchRun exists, skip (idempotent).
2. In one transaction: insert PaymentWarehouse, PipelineOutbox (PENDING), BatchRun.
3. **Outbox**: Scheduler publishes PENDING with retry; marks PUBLISHED or FAILED.

## Retry and Timeout

- **Pipeline**: 25 min timeout; retry 2.
- **Load**: 45s timeout; idempotent by (batchId, sourcePaymentId).
- **Outbox**: Retry 3x, 500ms backoff; 5s cap per cycle.
