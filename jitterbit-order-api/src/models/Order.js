const { getDb } = require('../database/connection');

async function create(orderId, value, creationDate) {
  const pool = getDb();
  await pool.query(
    'INSERT INTO orders ("orderId", value, "creationDate") VALUES ($1, $2, $3)',
    [orderId, value, creationDate]
  );
}

async function findByOrderId(orderId) {
  const pool = getDb();
  const result = await pool.query(
    'SELECT * FROM orders WHERE "orderId" = $1',
    [orderId]
  );
  return result.rows[0] || null;
}

async function findAll() {
  const pool = getDb();
  const result = await pool.query(
    'SELECT * FROM orders ORDER BY "creationDate" DESC'
  );
  return result.rows;
}

async function update(orderId, value, creationDate) {
  const pool = getDb();
  const result = await pool.query(
    'UPDATE orders SET value = $1, "creationDate" = $2 WHERE "orderId" = $3',
    [value, creationDate, orderId]
  );
  return { changes: result.rowCount };
}

async function remove(orderId) {
  const pool = getDb();
  const result = await pool.query('DELETE FROM orders WHERE "orderId" = $1', [
    orderId
  ]);
  return { changes: result.rowCount };
}

module.exports = { create, findByOrderId, findAll, update, remove };
