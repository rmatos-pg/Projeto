const { getDb } = require('../database/connection');

async function createMany(orderId, items) {
  const pool = getDb();
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    for (const item of items) {
      await client.query(
        'INSERT INTO items ("orderId", "productId", quantity, price) VALUES ($1, $2, $3, $4)',
        [orderId, item.productId, item.quantity, item.price]
      );
    }
    await client.query('COMMIT');
  } catch (e) {
    await client.query('ROLLBACK');
    throw e;
  } finally {
    client.release();
  }
}

async function findByOrderId(orderId) {
  const pool = getDb();
  const result = await pool.query(
    'SELECT * FROM items WHERE "orderId" = $1',
    [orderId]
  );
  return result.rows;
}

async function removeByOrderId(orderId) {
  const pool = getDb();
  await pool.query('DELETE FROM items WHERE "orderId" = $1', [orderId]);
}

module.exports = { createMany, findByOrderId, removeByOrderId };
