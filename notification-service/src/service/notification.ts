import { v4 as uuid } from 'uuid';
import { db } from '../db/schema';
import { insertOutbox } from '../outbox/repository';

export interface Notification {
  id: string;
  user_id: string;
  channel: string;
  subject: string | null;
  body: string;
  status: string;
  created_at: string;
}

export function createNotification(
  userId: string,
  channel: string,
  subject: string | null,
  body: string
): Notification {
  const id = uuid();
  const createdAt = new Date().toISOString();
  const payload = JSON.stringify({ id, userId, channel, subject, body });
  const insertNotification = db.transaction(() => {
    db.prepare(
      `INSERT INTO notifications (id, user_id, channel, subject, body, status, created_at)
       VALUES (?, ?, ?, ?, ?, 'PENDING', ?)`
    ).run(id, userId, channel, subject ?? null, body, createdAt);
    insertOutbox(id, 'NotificationCreated', payload);
  });
  insertNotification();
  return {
    id,
    user_id: userId,
    channel,
    subject,
    body,
    status: 'PENDING',
    created_at: createdAt,
  };
}

export function getNotification(id: string): Notification | undefined {
  const row = db
    .prepare(
      `SELECT id, user_id, channel, subject, body, status, created_at FROM notifications WHERE id = ?`
    )
    .get(id) as Notification | undefined;
  return row;
}
