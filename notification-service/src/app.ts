import express from 'express';
import { notificationRouter } from './routes/notification';

const app = express();
app.use(express.json());
app.use('/api/notifications', notificationRouter);
export { app };
