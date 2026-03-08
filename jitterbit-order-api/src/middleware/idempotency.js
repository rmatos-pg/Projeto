const { loadConfig } = require('../utils/configLoader');

const store = new Map();
const HEADER = 'idempotency-key';

function get(key) {
  const entry = store.get(key);
  if (!entry) return null;
  if (Date.now() > entry.expiresAt) {
    store.delete(key);
    return null;
  }
  return entry.response;
}

function set(key, response) {
  const config = loadConfig();
  store.set(key, {
    response,
    expiresAt: Date.now() + config.idempotencyTtlMs
  });
}

function middleware(req, res, next) {
  if (req.method !== 'POST' && req.method !== 'PUT') {
    return next();
  }
  const key = req.headers[HEADER];
  if (!key || typeof key !== 'string' || !key.trim()) {
    return next();
  }
  const cached = get(key.trim());
  if (cached) {
    return res.status(200).json(cached);
  }
  req.idempotencyKey = key.trim();
  next();
}

module.exports = { middleware, get, set };
