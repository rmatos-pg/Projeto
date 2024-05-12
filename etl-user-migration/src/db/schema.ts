import Database from 'better-sqlite3';

const db = new Database(':memory:');
db.exec(`
  CREATE TABLE user_target (
    id TEXT PRIMARY KEY,
    migration_id TEXT NOT NULL,
    source_user_id TEXT NOT NULL,
    email TEXT NOT NULL,
    name TEXT NOT NULL,
    migrated_at TEXT NOT NULL,
    UNIQUE(migration_id, source_user_id)
  );
  CREATE TABLE migration_outbox (
    id TEXT PRIMARY KEY,
    aggregate_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    payload TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL
  );
  CREATE TABLE migration_runs (
    migration_id TEXT PRIMARY KEY,
    started_at TEXT NOT NULL,
    completed_at TEXT,
    status TEXT NOT NULL
  );
`);

export { db };
