# ETL Order Extract - Architecture

## Component Diagram

```
+------------+   runKey (idempotent)   +------------------+
| Jenkins    |   java -jar ...         | ExtractJobRunner |
| Pipeline   | ----------------------> +--------+---------+
+------------+   timeout: 30 min       |        |
     | retry(2)                        v        |
     |                    +---------------------+------------------+
     |                    | OrderExtractService                    |
     |                    | - extractOrders (idempotent by runKey) |
     |                    | - publishOutbox (retry 3x)             |
     |                    +---------------------+------------------+
     |                                          |
     |              +---------------------------+---------------------------+
     |              |                           |                           |
     |              v                           v                           v
     |     +----------------+          +----------------+          +----------------+
     |     | OrderStaging   |          | EtlOutbox      |          | JobRun         |
     |     | (sourceId UK)  |          | (PENDING)      |          | (jobId+runKey) |
     |     +----------------+          +----------------+          +----------------+
     |              |                           |
     |              |               +-----------+-----------+
     |              |               | OutboxPublisherScheduler|
     |              |               | 3s delay, 5s timeout   |
     |              |               +------------------------+
     |              v
     |     +----------------+ (single transaction)
     |     | DB             |
     |     +----------------+
```

## ETL Flow

1. **Extract**: Job receives `runKey`. If `job_runs` already has (jobId, runKey), skip (idempotent).
2. In one transaction: insert OrderStaging rows, insert EtlOutbox (PENDING), insert JobRun.
3. **Outbox**: Scheduler polls PENDING; publishes with retry; marks PUBLISHED or FAILED.

## Retry and Timeout

- **Pipeline**: Jenkins timeout 30 min; retry 2 on failure.
- **Extract**: Loop timeout 60s; idempotent by sourceId.
- **Outbox**: Retry 3x, 1s backoff; 5s cap per cycle.
