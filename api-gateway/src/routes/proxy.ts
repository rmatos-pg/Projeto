import { Router, Request, Response } from 'express';
import { createClient, withRetry } from '../lib/http';
import { config } from '../config';

const router = Router();
const userClient = createClient(config.userServiceUrl);
const notificationClient = createClient(config.notificationServiceUrl);

router.all('/users*', async (req: Request, res: Response) => {
  const path = req.path.replace(/^\/?users/, '/api/users');
  const run = () =>
    userClient.request({
      method: req.method as 'GET' | 'POST' | 'PUT' | 'DELETE',
      url: path,
      data: req.body,
      params: req.query,
      headers: { ...req.headers, host: undefined },
    });
  try {
    const response = await withRetry(run, {
      attempts: config.retryAttempts,
      delayMs: config.retryDelayMs,
    });
    res.status(response.status).json(response.data);
  } catch (err: unknown) {
    const status = (err as { response?: { status: number } })?.response?.status ?? 502;
    const message = (err as Error).message ?? 'Gateway error';
    res.status(status).json({ error: message });
  }
});

router.all('/notifications*', async (req: Request, res: Response) => {
  const path = req.path.replace(/^\/?notifications/, '/api/notifications');
  const run = () =>
    notificationClient.request({
      method: req.method as 'GET' | 'POST',
      url: path,
      data: req.body,
      params: req.query,
      headers: { ...req.headers, host: undefined },
    });
  try {
    const response = await withRetry(run, {
      attempts: config.retryAttempts,
      delayMs: config.retryDelayMs,
    });
    res.status(response.status).json(response.data);
  } catch (err: unknown) {
    const status = (err as { response?: { status: number } })?.response?.status ?? 502;
    const message = (err as Error).message ?? 'Gateway error';
    res.status(status).json({ error: message });
  }
});

export { router as proxyRouter };
