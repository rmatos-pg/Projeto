const { Pool } = require('pg');
const { loadConfig } = require('../utils/configLoader');

let pool;

async function initDb() {
  if (pool) return pool;

  const config = loadConfig();
  const maxRetries = 10;
  const retryDelay = 2000;

  for (let i = 0; i < maxRetries; i++) {
    try {
      pool = new Pool({
        host: config.dbHost,
        user: config.dbUser,
        password: config.dbPassword,
        database: config.dbName,
        port: config.dbPort
      });

      await pool.query('SELECT 1');
      await createTables();
      return pool;
    } catch (err) {
      if (i === maxRetries - 1) throw err;
      await new Promise((r) => setTimeout(r, retryDelay));
    }
  }
}

async function createTables() {
  const client = await pool.connect();
  try {
    await client.query(`
      CREATE TABLE IF NOT EXISTS orders (
        "orderId" TEXT PRIMARY KEY,
        value REAL NOT NULL,
        "creationDate" TEXT NOT NULL
      );
      CREATE TABLE IF NOT EXISTS items (
        "orderId" TEXT NOT NULL,
        "productId" INTEGER NOT NULL,
        quantity INTEGER NOT NULL,
        price REAL NOT NULL,
        FOREIGN KEY ("orderId") REFERENCES orders("orderId")
      );
      CREATE TABLE IF NOT EXISTS outbox_events (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        aggregate_type TEXT NOT NULL,
        aggregate_id TEXT NOT NULL,
        event_type TEXT NOT NULL,
        payload TEXT NOT NULL,
        status TEXT NOT NULL DEFAULT 'PENDING',
        retry_count INTEGER DEFAULT 0,
        created_at TIMESTAMPTZ DEFAULT NOW()
      );
    `);
  } finally {
    client.release();
  }
}

function getDb() {
  return pool;
}

module.exports = { initDb, getDb };
