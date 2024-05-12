export const config = {
  port: parseInt(process.env.PORT || '3000', 10),
  idempotencyTtlSeconds: parseInt(process.env.IDEMPOTENCY_TTL_SECONDS || '86400', 10),
  operationTimeoutMs: parseInt(process.env.OPERATION_TIMEOUT_MS || '15000', 10),
  retryAttempts: parseInt(process.env.RETRY_ATTEMPTS || '3', 10),
  retryDelayMs: parseInt(process.env.RETRY_DELAY_MS || '500', 10),
};
