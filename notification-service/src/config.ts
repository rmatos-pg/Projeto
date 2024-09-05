export const config = {
  port: parseInt(process.env.PORT || '3001', 10),
  operationTimeoutMs: parseInt(process.env.OPERATION_TIMEOUT_MS || '10000', 10),
  retryAttempts: parseInt(process.env.RETRY_ATTEMPTS || '3', 10),
  retryDelayMs: parseInt(process.env.RETRY_DELAY_MS || '1000', 10),
  outboxPollIntervalMs: parseInt(process.env.OUTBOX_POLL_MS || '2000', 10),
  outboxPublishTimeoutMs: parseInt(process.env.OUTBOX_PUBLISH_TIMEOUT_MS || '5000', 10),
};
