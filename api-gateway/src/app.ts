import express from 'express';
import { proxyRouter } from './routes/proxy';

const app = express();
app.use(express.json());
app.use('/api', proxyRouter);
export { app };
