import { Request, Response, NextFunction } from 'express';
import { idempotencyStore } from '../store/idempotency';

const HEADER = 'Idempotency-Key';

export function idempotencyMiddleware(req: Request, res: Response, next: NextFunction): void {
  if (req.method !== 'POST' && req.method !== 'PUT') {
    next();
    return;
  }
  const key = req.headers[HEADER.toLowerCase()] as string | undefined;
  if (!key || typeof key !== 'string' || key.trim() === '') {
    next();
    return;
  }
  const cached = idempotencyStore.get(key);
  if (cached) {
    res.status(200).json(cached.response);
    return;
  }
  (req as Request & { idempotencyKey?: string }).idempotencyKey = key.trim();
  next();
}

export function saveIdempotencyResponse(key: string, response: unknown): void {
  idempotencyStore.set(key, { response, expiresAt: Date.now() + 86400_000 });
}
