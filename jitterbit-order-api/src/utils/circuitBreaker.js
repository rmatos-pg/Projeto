const CLOSED = 'CLOSED';
const OPEN = 'OPEN';
const HALF_OPEN = 'HALF_OPEN';

function createCircuitBreaker(options = {}) {
  const { failureThreshold = 3, resetTimeout = 5000 } = options;
  let state = CLOSED;
  let failures = 0;
  let lastFailure = 0;

  return {
    execute(fn) {
      if (state === OPEN) {
        if (Date.now() - lastFailure > resetTimeout) state = HALF_OPEN;
        else throw new Error('Circuit breaker is OPEN');
      }
      try {
        const result = fn();
        if (state === HALF_OPEN) {
          state = CLOSED;
          failures = 0;
        }
        return result;
      } catch (err) {
        failures++;
        lastFailure = Date.now();
        if (failures >= failureThreshold) state = OPEN;
        throw err;
      }
    },
    getState: () => state
  };
}

module.exports = { createCircuitBreaker };
