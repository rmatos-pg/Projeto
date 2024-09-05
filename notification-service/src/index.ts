import { app } from './app';
import { config } from './config';
import { startOutboxPublisher } from './outbox/publisher';

const port = config.port;
app.listen(port, () => {
  console.log(`Notification service listening on port ${port}`);
});
startOutboxPublisher();
