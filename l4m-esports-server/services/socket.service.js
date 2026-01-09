import { Server } from 'socket.io';
import jwt from 'jsonwebtoken';
import env from '../config/env.js';

let io = null;

/**
 * Initialize Socket.io server
 */
export const initSocket = (server) => {
  io = new Server(server, {
    cors: {
      origin: ['http://localhost:4200', 'http://localhost:3000'],
      methods: ['GET', 'POST'],
      credentials: false
    }
  });

  // Authentication middleware for Socket.io
  io.use((socket, next) => {
    const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.replace('Bearer ', '');
    
    if (!token) {
      // Allow connection without token (for public events)
      return next();
    }

    try {
      const decoded = jwt.verify(token, env.JWT_SECRET);
      socket.userId = decoded.id;
      socket.userRole = decoded.role;
      next();
    } catch (error) {
      // Allow connection even if token is invalid (for public events)
      next();
    }
  });

  io.on('connection', (socket) => {
    console.log(`🔌 Client connected: ${socket.id}${socket.userId ? ` (User: ${socket.userId})` : ''}`);

    socket.on('disconnect', () => {
      console.log(`🔌 Client disconnected: ${socket.id}`);
    });
  });

  return io;
};

/**
 * Get Socket.io instance
 */
export const getIO = () => {
  if (!io) {
    throw new Error('Socket.io not initialized. Call initSocket() first.');
  }
  return io;
};

/**
 * Emit event to all connected clients
 */
export const emitToAll = (event, data) => {
  if (io) {
    console.log(`📡 Emitting Socket.io event: ${event}`, data);
    io.emit(event, data);
  } else {
    console.warn(`⚠️ Socket.io not initialized, cannot emit event: ${event}`);
  }
};

/**
 * Emit event to specific user
 */
export const emitToUser = (userId, event, data) => {
  if (io) {
    io.to(`user:${userId}`).emit(event, data);
  }
};

/**
 * Emit event to all users with specific role
 */
export const emitToRole = (role, event, data) => {
  if (io) {
    io.to(`role:${role}`).emit(event, data);
  }
};

