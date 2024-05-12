# ETL User Migration - Architecture

## Component Diagram

```
+------------+   migrationId (idempotent)   +------------------+
| Jenkins    |   node dist/index.js        | runMigration     |
+------------+   timeout: 15 min            +--------+---------+
     | retry(2)                             |        |
     |                                      v        |
     |                    +--------------------------+------------------+
     |                    | runMigration (idempotent by migrationId)    |
     |                    | - check migration_runs                      |
     |                    | - insert user_target + outbox (same TX)      |
     |                    +--------------------------+------------------+
     |                                              |
     |              +-------------------------------+-------------------+
     |              |                               |                   |
     |              v                               v                   v
     |     +----------------+              +----------------+   +----------------+
     |     | user_target    |              | migration_outbox|   | migration_runs |
     |     | (mig_id+src)   |              | (PENDING)       |   | (migration_id) |
     |     +----------------+              +----------------+   +----------------+
     |              |                               |
     |              |                    +-----------+-----------+
     |              |                    | startOutboxPublisher   |
     |              |                    | retry + timeout        |
     |              |                    +------------------------+
     |              v
     |     +----------------+ (single transaction)
     |     | DB             |
     |     +----------------+
```

## ETL Flow

1. **Migration**: Receive `migrationId`. If migration_runs has it, skip (idempotent).
2. In one transaction: insert user_target, migration_outbox (PENDING), update migration_runs.
3. **Outbox**: Publisher polls PENDING; publishes with retry and timeout; marks PUBLISHED or FAILED.

## Retry and Timeout

- **Pipeline**: 15 min timeout; retry 2.
- **Migration**: 30s timeout; idempotent by (migrationId, source_user_id).
- **Outbox**: Retry 3x, 1s delay; 5s timeout per publish.
