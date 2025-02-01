import axios, { AxiosInstance } from 'axios';
import { config } from '../config';

export function createClient(baseURL: string, timeoutMs: number = config.downstreamTimeoutMs): AxiosInstance {
  return axios.create({
    baseURL,
    timeout: timeoutMs,
    headers: { 'Content-Type': 'application/json' },
  });
}

export async function withRetry<T>(
  fn: () => Promise<T>,
  options: { attempts: number; delayMs: number }
): Promise<T> {
  let lastError: Error | undefined;
  for (let i = 0; i < options.attempts; i++) {
    try {
      return await fn();
    } catch (e) {
      lastError = e instanceof Error ? e : new Error(String(e));
      if (i < options.attempts - 1) await sleep(options.delayMs);
    }
  }
  throw lastError;
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
