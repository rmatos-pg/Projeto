const Outbox = require('../models/Outbox');
const { withRetry, withTimeout } = require('../utils/async');
const { loadConfig } = require('../utils/configLoader');
const { logAction } = require('../utils/logAction');

function publishEvent(row) {
  return withRetry(
    () =>
      withTimeout(
        Promise.resolve().then(() => {
          logAction('OUTBOX', `Publishing ${row.event_type} (${row.aggregate_id})`);
        }),
        loadConfig().operationTimeoutMs
      ),
    { attempts: 3, delayMs: 500 }
  );
}

function startOutboxPublisher() {
  const config = loadConfig();
  function run() {
    Outbox.findPending()
      .then((rows) => {
        for (const row of rows) {
          publishEvent(row)
            .then(() => Outbox.markPublished(row.id))
            .catch((err) => {
              logAction('OUTBOX_ERROR', err.message);
              Outbox.markFailed(row.id);
            });
        }
      })
      .catch((err) => logAction('OUTBOX_ERROR', err.message));
    setTimeout(run, config.outboxPollIntervalMs);
  }
  setTimeout(run, config.outboxPollIntervalMs);
}

module.exports = { startOutboxPublisher };
