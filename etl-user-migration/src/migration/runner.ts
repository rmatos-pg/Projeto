import { v4 as uuid } from 'uuid';
import { db } from '../db/schema';
import { insertOutbox } from '../outbox/repository';
import { withTimeout } from '../lib/retry';
import { config } from '../config';

export async function runMigration(migrationId: string): Promise<number> {
  const existing = db.prepare('SELECT 1 FROM migration_runs WHERE migration_id = ?').get(migrationId);
  if (existing) {
    console.log('Idempotent skip: migrationId already processed');
    return 0;
  }

  const startedAt = new Date().toISOString();
  db.prepare(
    'INSERT INTO migration_runs (migration_id, started_at, status) VALUES (?, ?, ?)'
  ).run(migrationId, startedAt, 'RUNNING');

  const run = async (): Promise<number> => {
    let count = 0;
    for (let i = 0; i < 5; i++) {
      const sourceUserId = `SRC-${migrationId}-${i}`;
      const existingRow = db
        .prepare('SELECT 1 FROM user_target WHERE migration_id = ? AND source_user_id = ?')
        .get(migrationId, sourceUserId);
      if (existingRow) continue;

      const id = uuid();
      const migratedAt = new Date().toISOString();
      const insert = db.transaction(() => {
        db.prepare(
          'INSERT INTO user_target (id, migration_id, source_user_id, email, name, migrated_at) VALUES (?, ?, ?, ?, ?, ?)'
        ).run(id, migrationId, sourceUserId, `user${i}@example.com`, `User ${i}`, migratedAt);
        insertOutbox(id, 'UserMigrated', JSON.stringify({ sourceUserId, email: `user${i}@example.com` }));
      });
      insert();
      count++;
    }
    const completedAt = new Date().toISOString();
    db.prepare('UPDATE migration_runs SET completed_at = ?, status = ? WHERE migration_id = ?').run(
      completedAt,
      'COMPLETED',
      migrationId
    );
    return count;
  };

  return withTimeout(Promise.resolve().then(run), config.operationTimeoutMs);
}
