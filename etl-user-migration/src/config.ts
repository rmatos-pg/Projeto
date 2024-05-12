export const config = {
  operationTimeoutMs: parseInt(process.env.OPERATION_TIMEOUT_MS || '30000', 10),
  retryAttempts: parseInt(process.env.RETRY_ATTEMPTS || '3', 10),
  retryDelayMs: parseInt(process.env.RETRY_DELAY_MS || '1000', 10),
  outboxPollIntervalMs: parseInt(process.env.OUTBOX_POLL_MS || '2000', 10),
};

export function parseArgs(): { migrationId: string } {
  const args = process.argv.slice(2);
  const migrationIdIdx = args.indexOf('--migrationId');
  const migrationId =
    migrationIdIdx >= 0 && args[migrationIdIdx + 1]
      ? args[migrationIdIdx + 1]
      : 'migration-' + Date.now();
  return { migrationId };
}
