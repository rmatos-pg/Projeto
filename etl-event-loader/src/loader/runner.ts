import { v4 as uuid } from 'uuid';
import { db } from '../db/schema';
import { insertOutbox } from '../outbox/repository';
import { withTimeout } from '../lib/retry';
import { config } from '../config';

export async function runLoad(loadId: string): Promise<number> {
  const existing = db.prepare('SELECT 1 FROM load_runs WHERE load_id = ?').get(loadId);
  if (existing) {
    console.log('Idempotent skip: loadId already processed');
    return 0;
  }

  const startedAt = new Date().toISOString();
  db.prepare(
    'INSERT INTO load_runs (load_id, started_at, status) VALUES (?, ?, ?)'
  ).run(loadId, startedAt, 'RUNNING');

  const run = async (): Promise<number> => {
    let count = 0;
    for (let i = 0; i < 6; i++) {
      const aggregateId = `AGG-${loadId}-${i}`;
      const eventType = 'DomainEvent';
      const existingRow = db
        .prepare(
          'SELECT 1 FROM event_store WHERE load_id = ? AND aggregate_id = ? AND event_type = ?'
        )
        .get(loadId, aggregateId, eventType);
      if (existingRow) continue;

      const id = uuid();
      const payload = JSON.stringify({ aggregateId, eventType, seq: i });
      const loadedAt = new Date().toISOString();
      const insert = db.transaction(() => {
        db.prepare(
          'INSERT INTO event_store (id, load_id, event_type, aggregate_id, payload, loaded_at) VALUES (?, ?, ?, ?, ?, ?)'
        ).run(id, loadId, eventType, aggregateId, payload, loadedAt);
        insertOutbox(id, 'EventLoaded', payload);
      });
      insert();
      count++;
    }
    const completedAt = new Date().toISOString();
    db.prepare('UPDATE load_runs SET completed_at = ?, status = ? WHERE load_id = ?').run(
      completedAt,
      'COMPLETED',
      loadId
    );
    return count;
  };

  return withTimeout(Promise.resolve().then(run), config.operationTimeoutMs);
}
