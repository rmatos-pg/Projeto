import { v4 as uuid } from 'uuid';
import { db } from '../db/schema';

export function insertOutbox(aggregateId: string, eventType: string, payload: string): void {
  const id = uuid();
  const createdAt = new Date().toISOString();
  db.prepare(
    `INSERT INTO migration_outbox (id, aggregate_id, event_type, payload, status, retry_count, created_at)
     VALUES (?, ?, ?, ?, 'PENDING', 0, ?)`
  ).run(id, aggregateId, eventType, payload, createdAt);
}

export function findPendingOutbox(): { id: string; event_type: string; retry_count: number }[] {
  return db
    .prepare(
      `SELECT id, event_type, retry_count FROM migration_outbox WHERE status = 'PENDING' ORDER BY created_at ASC`
    )
    .all() as { id: string; event_type: string; retry_count: number }[];
}

export function markPublished(id: string, retryCount: number): void {
  db.prepare(
    `UPDATE migration_outbox SET status = 'PUBLISHED', retry_count = ? WHERE id = ?`
  ).run(retryCount, id);
}

export function markFailed(id: string): void {
  db.prepare(`UPDATE migration_outbox SET status = 'FAILED' WHERE id = ?`).run(id);
}
