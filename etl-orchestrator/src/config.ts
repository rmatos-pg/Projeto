export const config = {
  operationTimeoutMs: parseInt(process.env.OPERATION_TIMEOUT_MS || '60000', 10),
  retryAttempts: parseInt(process.env.RETRY_ATTEMPTS || '3', 10),
  retryDelayMs: parseInt(process.env.RETRY_DELAY_MS || '2000', 10),
  outboxPollIntervalMs: parseInt(process.env.OUTBOX_POLL_MS || '3000', 10),
  extractServiceUrl: process.env.EXTRACT_SERVICE_URL || 'http://localhost:8080',
  loadServiceUrl: process.env.LOAD_SERVICE_URL || 'http://localhost:8081',
};

export function parseArgs(): { runId: string } {
  const args = process.argv.slice(2);
  const runIdIdx = args.indexOf('--runId');
  const runId =
    runIdIdx >= 0 && args[runIdIdx + 1] ? args[runIdIdx + 1] : 'run-' + Date.now();
  return { runId };
}
