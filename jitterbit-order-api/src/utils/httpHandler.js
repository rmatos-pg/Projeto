const { logAction } = require('./logAction');

function handleAsync(fn) {
  return async (req, res) => {
    try {
      await fn(req, res);
    } catch (err) {
      logAction('ERROR', err.message, { stack: err.stack });
      res.status(500).json({ error: err.message });
    }
  };
}

module.exports = { handleAsync };
