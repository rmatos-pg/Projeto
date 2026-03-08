const app = require('../app');
const { initDb } = require('./database/connection');
const { loadConfig } = require('./utils/configLoader');
const { logAction } = require('./utils/logAction');
const { startOutboxPublisher } = require('./outbox/publisher');

async function start() {
  await initDb();
  startOutboxPublisher();
  const config = loadConfig();
  app.listen(config.port, () => {
    logAction('START', `API listening on port ${config.port}`);
  });
}

module.exports = { start };
