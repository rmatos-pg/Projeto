import { config } from '../config';
import { findPendingOutbox, markPublished, markFailed } from './repository';
import { withRetry, withTimeout } from '../lib/retry';

function publishEvent(row: { id: string; event_type: string }): Promise<void> {
  return withRetry(
    () =>
      withTimeout(
        Promise.resolve().then(() => {
          console.log(`Publishing orchestrator outbox: ${row.event_type}`);
        }),
        5000
      ),
    { attempts: config.retryAttempts, delayMs: config.retryDelayMs }
  );
}

export function startOutboxPublisher(): void {
  function run(): void {
    const pending = findPendingOutbox();
    for (const row of pending) {
      publishEvent(row)
        .then(() => markPublished(row.id, row.retry_count + 1))
        .catch((err) => {
          console.warn('Outbox failed:', err.message);
          markFailed(row.id);
        });
    }
    setTimeout(run, config.outboxPollIntervalMs);
  }
  setTimeout(run, config.outboxPollIntervalMs);
}
