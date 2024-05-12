export const config = {
  operationTimeoutMs: parseInt(process.env.OPERATION_TIMEOUT_MS || '25000', 10),
  retryAttempts: parseInt(process.env.RETRY_ATTEMPTS || '3', 10),
  retryDelayMs: parseInt(process.env.RETRY_DELAY_MS || '800', 10),
  outboxPollIntervalMs: parseInt(process.env.OUTBOX_POLL_MS || '2000', 10),
};

export function parseArgs(): { loadId: string } {
  const args = process.argv.slice(2);
  const loadIdIdx = args.indexOf('--loadId');
  const loadId =
    loadIdIdx >= 0 && args[loadIdIdx + 1] ? args[loadIdIdx + 1] : 'load-' + Date.now();
  return { loadId };
}
