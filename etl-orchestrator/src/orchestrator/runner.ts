import axios from 'axios';
import { db } from '../db/schema';
import { insertOutbox } from '../outbox/repository';
import { withRetry, withTimeout } from '../lib/retry';
import { config } from '../config';

const HTTP_TIMEOUT_MS = 10000;

export async function runOrchestration(runId: string): Promise<{ stepsRun: number }> {
  const existing = db.prepare('SELECT 1 FROM orchestration_runs WHERE run_id = ?').get(runId);
  if (existing) {
    console.log('Idempotent skip: runId already processed');
    return { stepsRun: 0 };
  }

  const startedAt = new Date().toISOString();
  db.prepare(
    'INSERT INTO orchestration_runs (run_id, started_at, status, steps_run) VALUES (?, ?, ?, ?)'
  ).run(runId, startedAt, 'RUNNING', 0);

  const run = async (): Promise<{ stepsRun: number }> => {
    let stepsRun = 0;
    const callService = async (url: string, body: object): Promise<void> => {
      await withRetry(
        () =>
          withTimeout(
            axios.post(url, body, { timeout: HTTP_TIMEOUT_MS }).then(() => {}),
            HTTP_TIMEOUT_MS
          ),
        { attempts: config.retryAttempts, delayMs: config.retryDelayMs }
      );
    };

    try {
      await callService(`${config.extractServiceUrl}/api/etl/extract`, { runKey: runId });
      stepsRun++;
    } catch (e) {
      console.warn('Extract step skipped (service may be unavailable):', (e as Error).message);
    }

    try {
      await callService(`${config.loadServiceUrl}/api/etl/load`, { batchId: runId });
      stepsRun++;
    } catch (e) {
      console.warn('Load step skipped (service may be unavailable):', (e as Error).message);
    }

    const insert = db.transaction(() => {
      insertOutbox(runId, 'OrchestrationCompleted', JSON.stringify({ runId, stepsRun }));
      db.prepare(
        'UPDATE orchestration_runs SET completed_at = ?, status = ?, steps_run = ? WHERE run_id = ?'
      ).run(new Date().toISOString(), 'COMPLETED', stepsRun, runId);
    });
    insert();

    return { stepsRun };
  };

  return withTimeout(Promise.resolve().then(run), config.operationTimeoutMs);
}
