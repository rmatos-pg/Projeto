import { runOrchestration } from './orchestrator/runner';
import { startOutboxPublisher } from './outbox/publisher';
import { parseArgs } from './config';

const { runId } = parseArgs();
runOrchestration(runId).then((result) => {
  console.log(`Orchestration completed: ${result.stepsRun} steps`);
});
startOutboxPublisher();
