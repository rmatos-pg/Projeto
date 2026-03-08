const LOG_PREFIX = '[jitterbit-order-api]';

function logAction(action, message, meta = {}) {
  const timestamp = new Date().toISOString();
  const entry = { timestamp, action, message, ...meta };
  console.log(`${LOG_PREFIX} ${timestamp} [${action}] ${message}`, Object.keys(meta).length ? JSON.stringify(meta) : '');
  return entry;
}

module.exports = { logAction };
