import { Router, Request, Response } from 'express';
import { createNotification, getNotification } from '../service/notification';
import { withTimeout } from '../lib/retry';
import { config } from '../config';

const router = Router();

router.post('/', (req: Request, res: Response) => {
  const { userId, channel, subject, body } = req.body ?? {};
  if (!userId || !channel || !body) {
    res.status(400).json({ error: 'userId, channel and body required' });
    return;
  }
  const run = () =>
    Promise.resolve().then(() =>
      createNotification(userId, channel, subject ?? null, body)
    );
  withTimeout(run(), config.operationTimeoutMs)
    .then((notification) => res.status(201).json(notification))
    .catch((err) => res.status(500).json({ error: err.message }));
});

router.get('/:id', (req: Request, res: Response) => {
  const notification = getNotification(req.params.id);
  if (!notification) {
    res.status(404).json({ error: 'Not found' });
    return;
  }
  res.json(notification);
});

export { router as notificationRouter };
