function loadConfig() {
  return {
    port: parseInt(process.env.PORT || '3000', 10),
    dbHost: process.env.DB_HOST || 'localhost',
    dbUser: process.env.DB_USER || 'postgres',
    dbPassword: process.env.DB_PASSWORD || 'postgres',
    dbName: process.env.DB_NAME || 'orders',
    dbPort: parseInt(process.env.DB_PORT || '5432', 10),
    logLevel: process.env.LOG_LEVEL || 'info',
    retryAttempts: parseInt(process.env.RETRY_ATTEMPTS || '3', 10),
    retryDelayMs: parseInt(process.env.RETRY_DELAY_MS || '1000', 10),
    operationTimeoutMs: parseInt(process.env.OPERATION_TIMEOUT_MS || '10000', 10),
    idempotencyTtlMs: parseInt(process.env.IDEMPOTENCY_TTL_MS || '86400000', 10),
    outboxPollIntervalMs: parseInt(process.env.OUTBOX_POLL_MS || '5000', 10)
  };
}

module.exports = { loadConfig };
