const { loadConfig } = require('./configLoader');

function runAsync(fn) {
  return (...args) =>
    Promise.resolve(fn(...args)).catch((err) => {
      console.error(err);
      throw err;
    });
}

function withTimeout(promise, ms) {
  return Promise.race([
    promise,
    new Promise((_, reject) =>
      setTimeout(() => reject(new Error('Operation timeout')), ms)
    )
  ]);
}

async function withRetry(fn, options = {}) {
  const config = loadConfig();
  const attempts = options.attempts ?? config.retryAttempts;
  const delayMs = options.delayMs ?? config.retryDelayMs;
  let lastError;
  for (let i = 0; i < attempts; i++) {
    try {
      return await fn();
    } catch (e) {
      lastError = e;
      if (i < attempts - 1) {
        await new Promise((r) => setTimeout(r, delayMs));
      }
    }
  }
  throw lastError;
}

module.exports = { runAsync, withTimeout, withRetry };
