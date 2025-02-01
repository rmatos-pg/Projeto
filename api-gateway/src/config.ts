export const config = {
  port: parseInt(process.env.PORT || '4000', 10),
  downstreamTimeoutMs: parseInt(process.env.DOWNSTREAM_TIMEOUT_MS || '8000', 10),
  retryAttempts: parseInt(process.env.RETRY_ATTEMPTS || '3', 10),
  retryDelayMs: parseInt(process.env.RETRY_DELAY_MS || '300', 10),
  userServiceUrl: process.env.USER_SERVICE_URL || 'http://localhost:3000',
  notificationServiceUrl: process.env.NOTIFICATION_SERVICE_URL || 'http://localhost:3001',
};
