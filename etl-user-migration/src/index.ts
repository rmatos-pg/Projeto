import { runMigration } from './migration/runner';
import { startOutboxPublisher } from './outbox/publisher';
import { parseArgs } from './config';

const { migrationId } = parseArgs();
runMigration(migrationId).then((count) => {
  console.log(`Migration completed: ${count} records`);
});
startOutboxPublisher();
