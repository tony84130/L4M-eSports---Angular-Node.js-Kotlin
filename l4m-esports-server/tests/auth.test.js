import request from 'supertest';
import app from '../app.js';

import User from '../models/user.model.js';
import { setupTests, cleanupTests, cleanupAfterEach } from './setup.js';
import { revokeToken } from '../services/redis.service.js';

describe('Authentication Tests', () => {
  let testUser;
  let testUserToken;

  beforeAll(async () => {
    await setupTests();
  });

  afterAll(async () => {
    await cleanupTests();
  });

  afterEach(async () => {
    await cleanupAfterEach();
  });

  describe('POST /api/auth/sign-up', () => {
    describe('Success cases', () => {
      test('Should sign up a new user with all fields', async () => {
        const userData = {
          email: 'test@example.com',
          password: 'password123',
          firstName: 'John',
          lastName: 'Doe',
          twitchUsername: 'johndoe'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(201);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toBe('User signed up successfully');
        expect(response.body.data.user).toHaveProperty('_id');
        expect(response.body.data.user.email).toBe('test@example.com');
        expect(response.body.data.user.firstName).toBe('John');
        expect(response.body.data.user.lastName).toBe('Doe');
        expect(response.body.data.user.twitchUsername).toBe('johndoe');
        expect(response.body.data.user.password).toBeUndefined();
        expect(response.body.data.token).toBeDefined();
        expect(typeof response.body.data.token).toBe('string');
      });

      test('Should sign up a new user with minimal required fields', async () => {
        const userData = {
          email: 'minimal@example.com',
          password: 'password123',
          firstName: 'Jane',
          lastName: 'Smith'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(201);

        expect(response.body.success).toBe(true);
        expect(response.body.data.user.email).toBe('minimal@example.com');
        expect(response.body.data.user.firstName).toBe('Jane');
        expect(response.body.data.user.lastName).toBe('Smith');
        expect(response.body.data.token).toBeDefined();
      });

      test('Should sign up without twitchUsername', async () => {
        const userData = {
          email: 'notwitch@example.com',
          password: 'password123',
          firstName: 'No',
          lastName: 'Twitch'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(201);

        expect(response.body.success).toBe(true);
        expect(response.body.data.user.twitchUsername).toBeUndefined();
      });

      test('Should accept password with exactly 6 characters', async () => {
        const userData = {
          email: 'sixchar@example.com',
          password: '123456',
          firstName: 'Six',
          lastName: 'Char'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(201);

        expect(response.body.success).toBe(true);
      });
    });

    describe('Validation errors', () => {
      test('Should return 400 if email is missing', async () => {
        const userData = {
          password: 'password123',
          firstName: 'John',
          lastName: 'Doe'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('required');
      });

      test('Should return 400 if password is missing', async () => {
        const userData = {
          email: 'test@example.com',
          firstName: 'John',
          lastName: 'Doe'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('required');
      });

      test('Should return 400 if firstName is missing', async () => {
        const userData = {
          email: 'test@example.com',
          password: 'password123',
          lastName: 'Doe'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('required');
      });

      test('Should return 400 if lastName is missing', async () => {
        const userData = {
          email: 'test@example.com',
          password: 'password123',
          firstName: 'John'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('required');
      });

      test('Should return 400 if firstName is empty string', async () => {
        const userData = {
          email: 'test@example.com',
          password: 'password123',
          firstName: '   ',
          lastName: 'Doe'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('First name cannot be empty');
      });

      test('Should return 400 if lastName is empty string', async () => {
        const userData = {
          email: 'test@example.com',
          password: 'password123',
          firstName: 'John',
          lastName: '   '
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Last name cannot be empty');
      });

      test('Should return 400 if email format is invalid (no @)', async () => {
        const userData = {
          email: 'invalid-email',
          password: 'password123',
          firstName: 'John',
          lastName: 'Doe'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('valid email');
      });

      test('Should return 400 if email format is invalid (no domain)', async () => {
        const userData = {
          email: 'user@',
          password: 'password123',
          firstName: 'John',
          lastName: 'Doe'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('valid email');
      });

      test('Should return 400 if password is too short (< 6 characters)', async () => {
        const userData = {
          email: 'test@example.com',
          password: '12345',
          firstName: 'John',
          lastName: 'Doe'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('at least 6 characters');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 400 if email already exists', async () => {
        // Create a user first
        await User.create({
          email: 'existing@example.com',
          password: 'password123',
          firstName: 'Existing',
          lastName: 'User'
        });

        // Try to create another user with same email
        const userData = {
          email: 'existing@example.com',
          password: 'password123',
          firstName: 'New',
          lastName: 'User'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('already exists');
      });

      test('Should return 400 if twitchUsername already exists', async () => {
        // Create a user with twitchUsername first
        await User.create({
          email: 'user1@example.com',
          password: 'password123',
          firstName: 'User',
          lastName: 'One',
          twitchUsername: 'existing_twitch'
        });

        // Try to create another user with same twitchUsername
        const userData = {
          email: 'user2@example.com',
          password: 'password123',
          firstName: 'User',
          lastName: 'Two',
          twitchUsername: 'existing_twitch'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Twitch username is already registered');
      });

      test('Should allow sign-up if twitchUsername is not provided', async () => {
        // Create a user without twitchUsername
        await User.create({
          email: 'user1@example.com',
          password: 'password123',
          firstName: 'User',
          lastName: 'One'
        });

        // Create another user without twitchUsername (should work)
        const userData = {
          email: 'user2@example.com',
          password: 'password123',
          firstName: 'User',
          lastName: 'Two'
        };

        const response = await request(app)
          .post('/api/auth/sign-up')
          .send(userData)
          .expect(201);

        expect(response.body.success).toBe(true);
      });
    });
  });

  describe('POST /api/auth/sign-in', () => {
    beforeEach(async () => {
      // Create a test user before each sign-in test
      testUser = await User.create({
        email: 'signin@example.com',
        password: 'password123',
        firstName: 'Sign',
        lastName: 'In'
      });
    });

    describe('Success cases', () => {
      test('Should sign in with valid credentials', async () => {
        const credentials = {
          email: 'signin@example.com',
          password: 'password123'
        };

        const response = await request(app)
          .post('/api/auth/sign-in')
          .send(credentials)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toBe('Sign in successful');
        expect(response.body.data.user).toHaveProperty('_id');
        expect(response.body.data.user.email).toBe('signin@example.com');
        expect(response.body.data.user.password).toBeUndefined();
        expect(response.body.data.token).toBeDefined();
        expect(typeof response.body.data.token).toBe('string');

        // Save token for later tests
        testUserToken = response.body.data.token;
      });

      test('Should sign in with email in different case', async () => {
        const credentials = {
          email: 'SIGNIN@EXAMPLE.COM',
          password: 'password123'
        };

        const response = await request(app)
          .post('/api/auth/sign-in')
          .send(credentials)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.data.token).toBeDefined();
      });
    });

    describe('Validation errors', () => {
      test('Should return 400 if email is missing', async () => {
        const credentials = {
          password: 'password123'
        };

        const response = await request(app)
          .post('/api/auth/sign-in')
          .send(credentials)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('required');
      });

      test('Should return 400 if password is missing', async () => {
        const credentials = {
          email: 'signin@example.com'
        };

        const response = await request(app)
          .post('/api/auth/sign-in')
          .send(credentials)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('required');
      });

      test('Should return 400 if both email and password are missing', async () => {
        const response = await request(app)
          .post('/api/auth/sign-in')
          .send({})
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('required');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 401 if email does not exist', async () => {
        const credentials = {
          email: 'nonexistent@example.com',
          password: 'password123'
        };

        const response = await request(app)
          .post('/api/auth/sign-in')
          .send(credentials)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid email or password');
      });

      test('Should return 401 if password is incorrect', async () => {
        const credentials = {
          email: 'signin@example.com',
          password: 'wrongpassword'
        };

        const response = await request(app)
          .post('/api/auth/sign-in')
          .send(credentials)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid email or password');
      });

      test('Should return 401 if both email and password are incorrect', async () => {
        const credentials = {
          email: 'fake@example.com',
          password: 'wrongpassword'
        };

        const response = await request(app)
          .post('/api/auth/sign-in')
          .send(credentials)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid email or password');
      });
    });

    describe('Session management', () => {
      test('Should revoke previous token on new sign-in', async () => {
        // Check if Redis is available
        try {
          const { getRedisClient } = await import('../services/redis.service.js');
          getRedisClient();
        } catch (error) {
          console.warn('⚠️  Skipping test: Redis not available');
          return;
        }

        // First sign-in
        const firstResponse = await request(app)
          .post('/api/auth/sign-in')
          .send({
            email: 'signin@example.com',
            password: 'password123'
          })
          .expect(200);

        const firstToken = firstResponse.body.data.token;

        // Wait a bit to ensure different token generation (JWT includes timestamp)
        await new Promise(resolve => setTimeout(resolve, 1000));

        // Second sign-in (should revoke first token)
        const secondResponse = await request(app)
          .post('/api/auth/sign-in')
          .send({
            email: 'signin@example.com',
            password: 'password123'
          })
          .expect(200);

        const secondToken = secondResponse.body.data.token;

        // First token should be different from second (JWT includes timestamp, so they should differ)
        // But even if they're the same, the first should be revoked
        if (firstToken === secondToken) {
          console.warn('⚠️  Tokens are identical (unlikely but possible if generated in same second)');
        }

        // Try to use first token on protected route (should fail)
        const protectedResponse = await request(app)
          .get('/api/users/me')
          .set('Authorization', `Bearer ${firstToken}`)
          .expect(401);

        expect(protectedResponse.body.success).toBe(false);
        expect(protectedResponse.body.error.message).toContain('revoked');

        // Second token should work
        const validResponse = await request(app)
          .get('/api/users/me')
          .set('Authorization', `Bearer ${secondToken}`)
          .expect(200);

        expect(validResponse.body.success).toBe(true);
      });
    });
  });

  describe('POST /api/auth/sign-out', () => {
    beforeEach(async () => {
      // Create a test user and sign in before each sign-out test
      testUser = await User.create({
        email: 'signout@example.com',
        password: 'password123',
        firstName: 'Sign',
        lastName: 'Out'
      });

      const signInResponse = await request(app)
        .post('/api/auth/sign-in')
        .send({
          email: 'signout@example.com',
          password: 'password123'
        });

      testUserToken = signInResponse.body.data.token;
    });

    describe('Success cases', () => {
      test('Should sign out successfully with valid token', async () => {
        const response = await request(app)
          .post('/api/auth/sign-out')
          .set('Authorization', `Bearer ${testUserToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('Sign out successful');
      });

      test('Should revoke token after sign-out', async () => {
        // Sign out
        await request(app)
          .post('/api/auth/sign-out')
          .set('Authorization', `Bearer ${testUserToken}`)
          .expect(200);

        // Try to use revoked token on protected route
        const response = await request(app)
          .get('/api/users/me')
          .set('Authorization', `Bearer ${testUserToken}`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('revoked');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token is provided', async () => {
        const response = await request(app)
          .post('/api/auth/sign-out')
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('No token provided');
      });

      test('Should return 401 if token is missing in Authorization header', async () => {
        const response = await request(app)
          .post('/api/auth/sign-out')
          .set('Authorization', 'Bearer')
          .expect(401);

        expect(response.body.success).toBe(false);
      });

      test('Should return 401 if Authorization header format is incorrect', async () => {
        const response = await request(app)
          .post('/api/auth/sign-out')
          .set('Authorization', testUserToken)
          .expect(401);

        expect(response.body.success).toBe(false);
      });

      test('Should return 401 if token is invalid (malformed)', async () => {
        const response = await request(app)
          .post('/api/auth/sign-out')
          .set('Authorization', 'Bearer invalid_token_12345')
          .expect(401);

        expect(response.body.success).toBe(false);
      });

      test('Should return 401 if token is revoked', async () => {
        // Revoke token manually
        try {
          await revokeToken(testUserToken);
        } catch (error) {
          // Redis might not be available, skip this test
          return;
        }

        // Try to sign out with revoked token
        const response = await request(app)
          .post('/api/auth/sign-out')
          .set('Authorization', `Bearer ${testUserToken}`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('revoked');
      });

      test('Should return 401 if token is valid but user is deleted from database', async () => {
        // Delete user from database
        await User.findByIdAndDelete(testUser._id);

        // Try to sign out with token of deleted user
        const response = await request(app)
          .post('/api/auth/sign-out')
          .set('Authorization', `Bearer ${testUserToken}`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('User not found');
      });
    });
  });

  describe('Complete authentication flow', () => {
    test('Should complete full flow: sign-up -> sign-in -> sign-out -> reuse token fails', async () => {
      // 1. Sign up
      const signUpData = {
        email: 'flow@example.com',
        password: 'password123',
        firstName: 'Flow',
        lastName: 'Test'
      };

      const signUpResponse = await request(app)
        .post('/api/auth/sign-up')
        .send(signUpData)
        .expect(201);

      const signUpToken = signUpResponse.body.data.token;

      // 2. Use token on protected route (should work)
      const meResponse1 = await request(app)
        .get('/api/users/me')
        .set('Authorization', `Bearer ${signUpToken}`)
        .expect(200);

      expect(meResponse1.body.data.user.email).toBe('flow@example.com');

      // 3. Sign out
      await request(app)
        .post('/api/auth/sign-out')
        .set('Authorization', `Bearer ${signUpToken}`)
        .expect(200);

      // 4. Try to reuse token (should fail)
      const meResponse2 = await request(app)
        .get('/api/users/me')
        .set('Authorization', `Bearer ${signUpToken}`)
        .expect(401);

      expect(meResponse2.body.success).toBe(false);
      expect(meResponse2.body.error.message).toContain('revoked');

      // 5. Sign in again (should work)
      const signInResponse = await request(app)
        .post('/api/auth/sign-in')
        .send({
          email: 'flow@example.com',
          password: 'password123'
        })
        .expect(200);

      const signInToken = signInResponse.body.data.token;

      // 6. Use new token (should work)
      const meResponse3 = await request(app)
        .get('/api/users/me')
        .set('Authorization', `Bearer ${signInToken}`)
        .expect(200);

      expect(meResponse3.body.success).toBe(true);
    });
  });
});

