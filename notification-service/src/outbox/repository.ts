import { v4 as uuid } from 'uuid';
import { db } from '../db/schema';

export interface OutboxRow {
  id: string;
  aggregate_id: string;
  event_type: string;
  payload: string;
  status: string;
  retry_count: number;
  created_at: string;
}

export function insertOutbox(aggregateId: string, eventType: string, payload: string): void {
  const id = uuid();
  const createdAt = new Date().toISOString();
  db.prepare(
    `INSERT INTO notification_outbox (id, aggregate_id, event_type, payload, status, retry_count, created_at)
     VALUES (?, ?, ?, ?, 'PENDING', 0, ?)`
  ).run(id, aggregateId, eventType, payload, createdAt);
}

export function findPendingOutbox(): OutboxRow[] {
  return db.prepare(
    `SELECT * FROM notification_outbox WHERE status = 'PENDING' ORDER BY created_at ASC`
  ).all() as OutboxRow[];
}

export function markPublished(id: string, retryCount: number): void {
  db.prepare(
    `UPDATE notification_outbox SET status = 'PUBLISHED', retry_count = ? WHERE id = ?`
  ).run(retryCount, id);
}

export function markFailed(id: string): void {
  db.prepare(`UPDATE notification_outbox SET status = 'FAILED' WHERE id = ?`).run(id);
}
