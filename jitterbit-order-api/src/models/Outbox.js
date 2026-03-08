const { getDb } = require('../database/connection');

async function insert(clientOrPool, aggregateType, aggregateId, eventType, payload) {
  const db = clientOrPool;
  await db.query(
    `INSERT INTO outbox_events (aggregate_type, aggregate_id, event_type, payload, status)
     VALUES ($1, $2, $3, $4, 'PENDING')`,
    [aggregateType, aggregateId, eventType, payload]
  );
}

async function findPending(limit = 50) {
  const pool = getDb();
  const result = await pool.query(
    `SELECT id, aggregate_type, aggregate_id, event_type, payload, retry_count
     FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at LIMIT $1`,
    [limit]
  );
  return result.rows;
}

async function markPublished(id) {
  const pool = getDb();
  await pool.query(
    `UPDATE outbox_events SET status = 'PUBLISHED' WHERE id = $1`,
    [id]
  );
}

async function markFailed(id) {
  const pool = getDb();
  await pool.query(
    `UPDATE outbox_events SET status = 'FAILED', retry_count = retry_count + 1 WHERE id = $1`,
    [id]
  );
}

module.exports = { insert, findPending, markPublished, markFailed };
