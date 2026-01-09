import express from 'express';
import { createServer } from 'http';
import morgan from 'morgan';
import cors from 'cors';
import env from './config/env.js';
import { connectDB } from './config/database.js';
import { initRedis } from './services/redis.service.js';
import { initSocket } from './services/socket.service.js';
import { initDefaultAdmin } from './scripts/initDefaultAdmin.js';
import { initDefaultGames } from './scripts/initDefaultGames.js';
import { initDefaultUsers } from './scripts/initDefaultUsers.js';
import { initDefaultEvents } from './scripts/initDefaultEvents.js';
import { startCleanupJob } from './services/notificationCleanup.service.js';

// Routes
import authRouter from './routes/auth.routes.js';
import gameRouter from './routes/game.routes.js';
import teamRouter from './routes/team.routes.js';
import teamRequestRouter from './routes/teamRequest.routes.js';
import eventRouter from './routes/event.routes.js';
import eventRegistrationRouter from './routes/eventRegistration.routes.js';
import matchRouter from './routes/match.routes.js';
import notificationRouter from './routes/notification.routes.js';
import userRouter from './routes/user.routes.js';
import twitchRouter from './routes/twitch.routes.js';
import aiRouter from './routes/ai.routes.js';

// Middlewares
import errorMiddleware from './middlewares/error.middleware.js';
import { NotFoundError } from './utils/errors.js';

const app = express();
const server = createServer(app);

// MIDDLEWARES
// Logger HTTP - log toutes les requêtes
app.use(morgan('dev')); // Format 'dev' : couleur, méthode, URL, status, temps de réponse
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(
  cors({
    origin: ['http://localhost:4200', 'http://localhost:3000'],
    credentials: false
  })
);

// CONNECT DB AND START SERVER
if (process.env.NODE_ENV !== 'test') {
  const PORT = env.PORT || 3000;
  
  // Initialize Socket.io
  initSocket(server);
  
  server.listen(PORT, async () => {
    console.log(`🚀 L4M Esports server is running on http://localhost:${PORT}`);
    
    try {
      await connectDB();
      await initRedis();
      await initDefaultAdmin();
      await initDefaultGames();
      await initDefaultUsers();
      await initDefaultEvents();
      startCleanupJob(); // Start notification cleanup job
      console.log(`📡 Environment: ${env.NODE_ENV || 'development'}`);
      console.log(`🔌 Socket.io initialized for real-time synchronization`);
    } catch (error) {
      console.error('❌ Failed to initialize services:', error);
      process.exit(1);
    }
  });
}

// APP ENTRY MESSAGE
app.get('/', (req, res) => {
  res.json({
    message: 'L4M Esports API Server',
    status: 'running'
  });
});

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString()
  });
});

// ROUTES
app.use('/api/auth', authRouter);
app.use('/api/games', gameRouter);
app.use('/api/teams', teamRouter);
app.use('/api/team-requests', teamRequestRouter);
app.use('/api/events', eventRouter);
app.use('/api/event-registrations', eventRegistrationRouter);
app.use('/api/matches', matchRouter);
app.use('/api/notifications', notificationRouter);
app.use('/api/users', userRouter);
app.use('/api/twitch', twitchRouter);
app.use('/api/ai', aiRouter);

// 404 handler
app.use((req, res, next) => {
  next(new NotFoundError(`Route ${req.originalUrl} not found`));
});

// ERROR MIDDLEWARE
app.use(errorMiddleware);

export default app;
export { server };
