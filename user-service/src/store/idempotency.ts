interface Entry {
  response: unknown;
  expiresAt: number;
}

const store = new Map<string, Entry>();

function prune(): void {
  const now = Date.now();
  for (const [k, v] of store.entries()) {
    if (v.expiresAt <= now) store.delete(k);
  }
}

setInterval(prune, 60_000);

export const idempotencyStore = {
  get(key: string): { response: unknown } | null {
    const entry = store.get(key);
    if (!entry || entry.expiresAt <= Date.now()) return null;
    return { response: entry.response };
  },
  set(key: string, value: Entry): void {
    store.set(key, value);
  },
};
