import { config } from '../config';
import { findPendingOutbox, markPublished, markFailed } from './repository';
import { withRetry, withTimeout } from '../lib/retry';

function publishEvent(row: { id: string; event_type: string; retry_count: number }): Promise<void> {
  return withRetry(
    () =>
      withTimeout(
        Promise.resolve().then(() => {
          console.log(`Publishing outbox event: ${row.event_type} (${row.id})`);
        }),
        config.outboxPublishTimeoutMs
      ),
    { attempts: config.retryAttempts, delayMs: config.retryDelayMs }
  );
}

export function startOutboxPublisher(): void {
  function run(): void {
    const pending = findPendingOutbox();
    const deadline = Date.now() + config.outboxPublishTimeoutMs;
    for (const row of pending) {
      if (Date.now() > deadline) break;
      publishEvent(row)
        .then(() => markPublished(row.id, row.retry_count + 1))
        .catch((err) => {
          console.warn('Outbox publish failed:', err.message);
          markFailed(row.id);
        });
    }
    setTimeout(run, config.outboxPollIntervalMs);
  }
  setTimeout(run, config.outboxPollIntervalMs);
}
