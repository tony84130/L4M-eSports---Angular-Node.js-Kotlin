import redis from 'redis';
import env from '../config/env.js';

let redisClient = null;

/**
 * Initialize Redis client
 */
export const initRedis = async () => {
  try {
    redisClient = redis.createClient({
      url: env.REDIS_URL || 'redis://localhost:6379'
    });

    redisClient.on('error', (err) => {
      console.error('❌ Redis Client Error:', err);
    });

    redisClient.on('connect', () => {
      console.log('✅ Redis Client Connected');
    });

    await redisClient.connect();
    return redisClient;
  } catch (error) {
    console.error('❌ Redis connection error:', error);
    throw error;
  }
};

/**
 * Get Redis client
 */
export const getRedisClient = () => {
  if (!redisClient) {
    throw new Error('Redis client not initialized. Call initRedis() first.');
  }
  return redisClient;
};

/**
 * Store token in Redis with expiration (24 hours)
 * Also maintains a set of active tokens per user
 * Uses a transaction to ensure atomicity
 * @param {string} token - JWT token
 * @param {string} userId - User ID
 * @param {number} expirationInSeconds - Expiration time in seconds (default: 24h = 86400)
 */
export const storeToken = async (token, userId, expirationInSeconds = 86400) => {
  try {
    const client = getRedisClient();
    const key = `session:${token}`;
    const userSessionsKey = `user:sessions:${userId}`;
    
    // Use a transaction to ensure atomicity
    const multi = client.multi();
    
    // Store the token
    multi.setEx(key, expirationInSeconds, userId);
    
    // Add token to user's session set and set expiration
    multi.sAdd(userSessionsKey, token);
    multi.expire(userSessionsKey, expirationInSeconds);
    
    // Execute transaction
    await multi.exec();
    
    return true;
  } catch (error) {
    console.error('Error storing token in Redis:', error);
    throw error;
  }
};

/**
 * Check if token exists and is valid in Redis
 * @param {string} token - JWT token
 * @returns {string|null} - User ID if token exists, null otherwise
 */
export const getToken = async (token) => {
  try {
    const client = getRedisClient();
    const key = `session:${token}`;
    const userId = await client.get(key);
    return userId;
  } catch (error) {
    console.error('Error getting token from Redis:', error);
    return null;
  }
};

/**
 * Revoke token (delete from Redis)
 * @param {string} token - JWT token
 */
export const revokeToken = async (token) => {
  try {
    const client = getRedisClient();
    const key = `session:${token}`;
    
    // Get userId before deleting to clean up user's session set
    const userId = await client.get(key);
    
    // Delete the token
    await client.del(key);
    
    // Remove from user's session set if userId exists
    if (userId) {
      const userSessionsKey = `user:sessions:${userId}`;
      await client.sRem(userSessionsKey, token);
    }
    
    return true;
  } catch (error) {
    console.error('Error revoking token in Redis:', error);
    throw error;
  }
};

/**
 * Revoke all tokens for a specific user
 * Ensures proper cleanup to prevent accumulation of sessions
 * @param {string} userId - User ID
 */
export const revokeAllUserTokens = async (userId) => {
  try {
    const client = getRedisClient();
    const userSessionsKey = `user:sessions:${userId}`;
    
    // Get all tokens for this user first
    const tokens = await client.sMembers(userSessionsKey);
    
    // Use a transaction to ensure atomicity when deleting
    const multi = client.multi();
    
    if (tokens && tokens.length > 0) {
      // Delete all token keys
      const keysToDelete = tokens.map(token => `session:${token}`);
      if (keysToDelete.length > 0) {
        multi.del(keysToDelete);
      }
    }
    
    // Delete the user's session set (this ensures no accumulation)
    multi.del(userSessionsKey);
    
    // Execute transaction atomically
    await multi.exec();
    
    return true;
  } catch (error) {
    console.error('Error revoking all user tokens in Redis:', error);
    throw error;
  }
};

/**
 * Close Redis connection
 */
export const closeRedis = async () => {
  try {
    if (redisClient) {
      await redisClient.quit();
      console.log('✅ Redis connection closed');
    }
  } catch (error) {
    console.error('Error closing Redis connection:', error);
  }
};
