import jwt from 'jsonwebtoken';
import env from '../config/env.js';
import { getToken } from '../services/redis.service.js';
import { getUserByIdForAuth } from '../services/auth.service.js';
import { UnauthorizedError, ForbiddenError } from '../utils/errors.js';

/**
 * Middleware to verify JWT token and check if it exists in Redis
 */
export const authenticate = async (req, res, next) => {
  try {
    const token = req.headers.authorization?.split(' ')[1]; // Bearer TOKEN
    
    if (!token) {
      throw new UnauthorizedError('No token provided. Authorization required.');
    }

    // Verify JWT token
    const decoded = jwt.verify(token, env.JWT_SECRET);
    
    // Check if token exists in Redis (not revoked)
    const userIdInRedis = await getToken(token);
    
    if (!userIdInRedis) {
      throw new UnauthorizedError('Token has been revoked or expired. Please sign in again.');
    }

    // Verify that the user ID in token matches the one in Redis
    if (userIdInRedis !== decoded.id.toString()) {
      throw new UnauthorizedError('Token invalid.');
    }
    
    // Get user from database
    const user = await getUserByIdForAuth(decoded.id);
    
    if (!user) {
      throw new UnauthorizedError('User not found. Token invalid.');
    }

    req.user = user;
    req.token = token; // Store token for use in controllers
    next();
  } catch (error) {
    // JWT errors are handled by error middleware
    next(error);
  }
};

/**
 * Middleware to check user roles
 */
export const authorize = (...roles) => {
  return (req, res, next) => {
    if (!req.user) {
      return next(new UnauthorizedError('Authentication required.'));
    }

    if (!roles.includes(req.user.role)) {
      return next(new ForbiddenError('Insufficient permissions. Access denied.'));
    }

    next();
  };
};
