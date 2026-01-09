import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import { initRedis, closeRedis } from '../services/redis.service.js';

let mongoServer;

/**
 * Setup before all tests
 */
export const setupTests = async () => {
  // Close any existing MongoDB connection first
  if (mongoose.connection.readyState !== 0) {
    await mongoose.connection.close();
  }

  // Start MongoDB Memory Server
  try {
    mongoServer = await MongoMemoryServer.create();
    const mongoUri = mongoServer.getUri();
    
    await mongoose.connect(mongoUri);
    console.log('✅ Test MongoDB (Memory) Connected');
  } catch (error) {
    console.error('❌ Test MongoDB connection error:', error);
    throw error;
  }

  // Initialize Redis for tests
  try {
    await initRedis();
    console.log('✅ Test Redis Connected');
  } catch (error) {
    console.error('❌ Test Redis connection error:', error.message);
    console.error('Full error:', error);
    // Don't throw - Redis might not be available in test environment
    // Tests will handle this gracefully
  }
};

/**
 * Cleanup after all tests
 */
export const cleanupTests = async () => {
  // Close MongoDB connection and stop memory server
  try {
    if (mongoose.connection.readyState !== 0) {
      await mongoose.connection.dropDatabase();
      await mongoose.connection.close();
    }
    
    if (mongoServer) {
      await mongoServer.stop();
    }
    console.log('✅ Test MongoDB (Memory) Disconnected');
  } catch (error) {
    console.error('❌ Test MongoDB disconnection error:', error);
  }

  // Clear Redis and close connection
  try {
    const { getRedisClient } = await import('../services/redis.service.js');
    const client = getRedisClient();
    if (client) {
      // Clear all session keys before closing
      const keys = await client.keys('session:*');
      if (keys && keys.length > 0) {
        await client.del(keys);
      }
      const userSessionKeys = await client.keys('user:sessions:*');
      if (userSessionKeys && userSessionKeys.length > 0) {
        await client.del(userSessionKeys);
      }
    }
    await closeRedis();
    console.log('✅ Test Redis Disconnected');
  } catch (error) {
    console.error('❌ Test Redis disconnection error:', error);
  }
};

/**
 * Cleanup after each test
 */
export const cleanupAfterEach = async () => {
  // Clear all collections
  const collections = mongoose.connection.collections;
  for (const key in collections) {
    await collections[key].deleteMany({});
  }

  // Note: We don't clean Redis here because tokens created in beforeEach
  // need to persist for the tests. Redis will be cleaned in cleanupTests.
};

