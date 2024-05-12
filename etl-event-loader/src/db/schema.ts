import Database from 'better-sqlite3';

const db = new Database(':memory:');
db.exec(`
  CREATE TABLE event_store (
    id TEXT PRIMARY KEY,
    load_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    aggregate_id TEXT NOT NULL,
    payload TEXT NOT NULL,
    loaded_at TEXT NOT NULL,
    UNIQUE(load_id, aggregate_id, event_type)
  );
  CREATE TABLE loader_outbox (
    id TEXT PRIMARY KEY,
    aggregate_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    payload TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL
  );
  CREATE TABLE load_runs (
    load_id TEXT PRIMARY KEY,
    started_at TEXT NOT NULL,
    completed_at TEXT,
    status TEXT NOT NULL
  );
`);

export { db };
