import Database from 'better-sqlite3';
import { v4 as uuid } from 'uuid';

export interface User {
  id: string;
  email: string;
  name: string;
  createdAt: string;
}

const db = new Database(':memory:');
db.exec(`
  CREATE TABLE users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    created_at TEXT NOT NULL
  );
`);

export function createUser(email: string, name: string): User {
  const id = uuid();
  const createdAt = new Date().toISOString();
  const stmt = db.prepare(
    'INSERT INTO users (id, email, name, created_at) VALUES (?, ?, ?, ?)'
  );
  stmt.run(id, email, name, createdAt);
  return { id, email, name, createdAt };
}

export function findUserById(id: string): User | undefined {
  const row = db.prepare('SELECT id, email, name, created_at as createdAt FROM users WHERE id = ?').get(id) as User | undefined;
  return row;
}
