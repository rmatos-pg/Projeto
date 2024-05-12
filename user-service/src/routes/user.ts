import { Router, Request, Response } from 'express';
import { createUser, findUserById } from '../store/users';
import { saveIdempotencyResponse } from '../middleware/idempotency';
import { withTimeout } from '../lib/retry';
import { config } from '../config';

const router = Router();

router.post('/', (req: Request, res: Response) => {
  const idempotencyKey = (req as Request & { idempotencyKey?: string }).idempotencyKey;
  const { email, name } = req.body ?? {};
  if (!email || !name) {
    res.status(400).json({ error: 'email and name required' });
    return;
  }
  const run = async () => {
    const user = createUser(email, name);
    if (idempotencyKey) saveIdempotencyResponse(idempotencyKey, user);
    return user;
  };
  withTimeout(Promise.resolve().then(run), config.operationTimeoutMs)
    .then((user) => res.status(201).json(user))
    .catch((err) => res.status(500).json({ error: err.message }));
});

router.get('/:id', (req: Request, res: Response) => {
  const user = findUserById(req.params.id);
  if (!user) {
    res.status(404).json({ error: 'User not found' });
    return;
  }
  res.json(user);
});

export { router as userRouter };
