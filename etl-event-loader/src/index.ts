import { runLoad } from './loader/runner';
import { startOutboxPublisher } from './outbox/publisher';
import { parseArgs } from './config';

const { loadId } = parseArgs();
runLoad(loadId).then((count) => {
  console.log(`Load completed: ${count} records`);
});
startOutboxPublisher();
