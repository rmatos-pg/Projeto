# ETL Orchestrator - Architecture

## Component Diagram

```
+------------+   runId (idempotent)   +------------------+
| Jenkins    |   node dist/index.js  | runOrchestration  |
+------------+   timeout: 20 min     +--------+---------+
     | retry(2)                      |        |
     |                               v        |
     |                    +------------------+------------------+
     |                    | runOrchestration (idempotent by runId)|
     |                    | - call extract (retry + timeout)      |
     |                    | - call load (retry + timeout)        |
     |                    | - insert outbox + update run (same TX)|
     |                    +------------------+------------------+
     |                                          |
     |              +----------------------------+-------------------+
     |              |                            |                    |
     |              v                            v                    v
     |     +----------------+           +----------------+   +----------------+
     |     | Extract Service|           | Load Service    |   | orchestrator   |
     |     | (HTTP)         |           | (HTTP)          |   | _outbox        |
     |     +----------------+           +----------------+   +----------------+
     |              |                            |                    |
     |              |                +------------+------------+
     |              |                | startOutboxPublisher    |
     |              |                | retry + timeout         |
     |              |                +-------------------------+
     |              v
     |     +----------------+
     |     | orchestration   |
     |     | _runs          |
     |     +----------------+
```

## ETL Flow

1. **Orchestration**: Receive `runId`. If orchestration_runs has it, skip (idempotent).
2. Call extract service (retry 3x, 10s timeout); call load service (retry 3x, 10s timeout).
3. In one transaction: insert orchestrator_outbox (PENDING), update orchestration_runs.
4. **Outbox**: Publisher polls PENDING; publishes with retry and timeout; marks PUBLISHED or FAILED.

## Retry and Timeout

- **Pipeline**: 20 min timeout; retry 2.
- **Orchestration**: 60s overall; each HTTP call 10s timeout, 3 retries, 2s delay.
- **Outbox**: Retry 3x, 2s delay; 5s timeout per publish.
