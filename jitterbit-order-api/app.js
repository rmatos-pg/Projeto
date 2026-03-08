const express = require('express');
const path = require('path');
const orderRoutes = require('./src/routes/orderRoutes');
const { middleware: idempotencyMiddleware } = require('./src/middleware/idempotency');

const app = express();
app.use(express.json());
app.use(idempotencyMiddleware);
app.use('/', orderRoutes);
app.use(express.static(path.join(__dirname, 'public')));

module.exports = app;
