import express from 'express';
import { userRouter } from './routes/user';
import { idempotencyMiddleware } from './middleware/idempotency';

const app = express();
app.use(express.json());
app.use(idempotencyMiddleware);
app.use('/api/users', userRouter);
export { app };
