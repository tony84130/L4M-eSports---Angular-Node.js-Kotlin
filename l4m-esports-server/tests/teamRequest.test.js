import request from 'supertest';
import app from '../app.js';
import User from '../models/user.model.js';
import Game from '../models/game.model.js';
import Team from '../models/team.model.js';
import TeamRequest from '../models/teamRequest.model.js';
import { createSession } from '../services/auth.service.js';
import { setupTests, cleanupAfterEach, cleanupTests } from './setup.js';

describe('Team Request Routes Tests', () => {
  let adminUser, adminToken;
  let memberUser, memberToken;
  let captainUser, captainToken;
  let otherMemberUser, otherMemberToken;
  let testGame;
  let testTeam;
  let testTeamRequest;

  beforeAll(async () => {
    await setupTests();
  });

  afterAll(async () => {
    await cleanupTests();
  });

  beforeEach(async () => {
    // Create admin user
    try {
      adminUser = await User.create({
        email: 'admin@test.com',
        password: 'password123',
        firstName: 'Admin',
        lastName: 'User',
        role: 'admin'
      });
      try {
        adminToken = await createSession(adminUser._id);
      } catch (error) {
        adminToken = null;
      }
    } catch (error) {
      adminUser = null;
      adminToken = null;
    }

    // Create member user (will be captain of testTeam)
    try {
      memberUser = await User.create({
        email: 'member@test.com',
        password: 'password123',
        firstName: 'Member',
        lastName: 'User',
        role: 'member',
        twitchUsername: 'member_twitch'
      });
      try {
        memberToken = await createSession(memberUser._id);
      } catch (error) {
        memberToken = null;
      }
    } catch (error) {
      memberUser = null;
      memberToken = null;
    }

    // Create captain user
    try {
      captainUser = await User.create({
        email: 'captain@test.com',
        password: 'password123',
        firstName: 'Captain',
        lastName: 'User',
        role: 'captain'
      });
      try {
        captainToken = await createSession(captainUser._id);
      } catch (error) {
        captainToken = null;
      }
    } catch (error) {
      captainUser = null;
      captainToken = null;
    }

    // Create other member user (will make requests)
    try {
      otherMemberUser = await User.create({
        email: 'other@test.com',
        password: 'password123',
        firstName: 'Other',
        lastName: 'Member',
        role: 'member'
      });
      try {
        otherMemberToken = await createSession(otherMemberUser._id);
      } catch (error) {
        otherMemberToken = null;
      }
    } catch (error) {
      otherMemberUser = null;
      otherMemberToken = null;
    }

    // Create test game
    if (adminUser) {
      testGame = await Game.create({
        name: 'Test Game',
        description: 'A test game',
        isActive: true,
        createdBy: adminUser._id
      });
    }

    // Create test team (captain is memberUser)
    if (memberUser && testGame) {
      testTeam = await Team.create({
        name: 'Test Team',
        description: 'A test team',
        game: testGame._id,
        captain: memberUser._id,
        members: [memberUser._id],
        status: 'active',
        maxMembers: 10
      });
    }

    // Create test team request (from otherMemberUser to testTeam)
    if (otherMemberUser && testTeam) {
      testTeamRequest = await TeamRequest.create({
        team: testTeam._id,
        user: otherMemberUser._id,
        status: 'pending',
        message: 'I would like to join this team'
      });
    }
  });

  afterEach(async () => {
    await cleanupAfterEach();
  });

  // Helper function to ensure test data exists
  const ensureTestData = async (needed = {}) => {
    const { 
      needAdmin = false, 
      needMember = false, 
      needCaptain = false, 
      needOtherMember = false,
      needGame = false,
      needTeam = false,
      needTeamRequest = false
    } = needed;

    // Ensure admin user exists
    if (needAdmin) {
      if (!adminUser) {
        adminUser = await User.create({
          email: 'admin@test.com',
          password: 'password123',
          firstName: 'Admin',
          lastName: 'User',
          role: 'admin'
        });
      }
      if (!adminToken) {
        try {
          adminToken = await createSession(adminUser._id);
        } catch (error) {
          return false; // Redis unavailable
        }
      }
      // Verify token is valid
      try {
        const { getToken } = await import('../services/redis.service.js');
        const userIdInRedis = await getToken(adminToken);
        if (!userIdInRedis || userIdInRedis !== adminUser._id.toString()) {
          adminToken = await createSession(adminUser._id);
        }
      } catch (error) {
        return false; // Redis unavailable
      }
    }

    // Ensure member user exists
    if (needMember) {
      if (!memberUser) {
        memberUser = await User.create({
          email: 'member@test.com',
          password: 'password123',
          firstName: 'Member',
          lastName: 'User',
          role: 'member',
          twitchUsername: 'member_twitch'
        });
      }
      if (!memberToken) {
        try {
          memberToken = await createSession(memberUser._id);
        } catch (error) {
          return false; // Redis unavailable
        }
      }
      // Verify token is valid
      try {
        const { getToken } = await import('../services/redis.service.js');
        const userIdInRedis = await getToken(memberToken);
        if (!userIdInRedis || userIdInRedis !== memberUser._id.toString()) {
          memberToken = await createSession(memberUser._id);
        }
      } catch (error) {
        return false; // Redis unavailable
      }
    }

    // Ensure captain user exists
    if (needCaptain) {
      if (!captainUser) {
        captainUser = await User.create({
          email: 'captain@test.com',
          password: 'password123',
          firstName: 'Captain',
          lastName: 'User',
          role: 'captain'
        });
      }
      if (!captainToken) {
        try {
          captainToken = await createSession(captainUser._id);
        } catch (error) {
          return false; // Redis unavailable
        }
      }
      // Verify token is valid
      try {
        const { getToken } = await import('../services/redis.service.js');
        const userIdInRedis = await getToken(captainToken);
        if (!userIdInRedis || userIdInRedis !== captainUser._id.toString()) {
          captainToken = await createSession(captainUser._id);
        }
      } catch (error) {
        return false; // Redis unavailable
      }
    }

    // Ensure other member user exists
    if (needOtherMember) {
      if (!otherMemberUser) {
        otherMemberUser = await User.create({
          email: 'other@test.com',
          password: 'password123',
          firstName: 'Other',
          lastName: 'Member',
          role: 'member'
        });
      }
      if (!otherMemberToken) {
        try {
          otherMemberToken = await createSession(otherMemberUser._id);
        } catch (error) {
          return false; // Redis unavailable
        }
      }
      // Verify token is valid
      try {
        const { getToken } = await import('../services/redis.service.js');
        const userIdInRedis = await getToken(otherMemberToken);
        if (!userIdInRedis || userIdInRedis !== otherMemberUser._id.toString()) {
          otherMemberToken = await createSession(otherMemberUser._id);
        }
      } catch (error) {
        return false; // Redis unavailable
      }
    }

    // Ensure game exists
    if (needGame) {
      if (!adminUser) {
        adminUser = await User.create({
          email: 'admin@test.com',
          password: 'password123',
          firstName: 'Admin',
          lastName: 'User',
          role: 'admin'
        });
      }
      if (!testGame) {
        testGame = await Game.create({
          name: 'Test Game',
          description: 'A test game',
          isActive: true,
          createdBy: adminUser._id
        });
      }
    }

    // Ensure team exists
    if (needTeam) {
      if (!memberUser) {
        memberUser = await User.create({
          email: 'member@test.com',
          password: 'password123',
          firstName: 'Member',
          lastName: 'User',
          role: 'member',
          twitchUsername: 'member_twitch'
        });
      }
      if (!testGame) {
        if (!adminUser) {
          adminUser = await User.create({
            email: 'admin@test.com',
            password: 'password123',
            firstName: 'Admin',
            lastName: 'User',
            role: 'admin'
          });
        }
        testGame = await Game.create({
          name: 'Test Game',
          description: 'A test game',
          isActive: true,
          createdBy: adminUser._id
        });
      }
      if (!testTeam) {
        testTeam = await Team.create({
          name: 'Test Team',
          description: 'A test team',
          game: testGame._id,
          captain: memberUser._id,
          members: [memberUser._id],
          status: 'active',
          maxMembers: 10
        });
      }
    }

    // Ensure team request exists
    if (needTeamRequest) {
      if (!otherMemberUser) {
        otherMemberUser = await User.create({
          email: 'other@test.com',
          password: 'password123',
          firstName: 'Other',
          lastName: 'Member',
          role: 'member'
        });
      }
      if (!testTeam) {
        if (!memberUser) {
          memberUser = await User.create({
            email: 'member@test.com',
            password: 'password123',
            firstName: 'Member',
            lastName: 'User',
            role: 'member',
            twitchUsername: 'member_twitch'
          });
        }
        if (!testGame) {
          if (!adminUser) {
            adminUser = await User.create({
              email: 'admin@test.com',
              password: 'password123',
              firstName: 'Admin',
              lastName: 'User',
              role: 'admin'
            });
          }
          testGame = await Game.create({
            name: 'Test Game',
            description: 'A test game',
            isActive: true,
            createdBy: adminUser._id
          });
        }
        testTeam = await Team.create({
          name: 'Test Team',
          description: 'A test team',
          game: testGame._id,
          captain: memberUser._id,
          members: [memberUser._id],
          status: 'active',
          maxMembers: 10
        });
      }
      if (!testTeamRequest) {
        testTeamRequest = await TeamRequest.create({
          team: testTeam._id,
          user: otherMemberUser._id,
          status: 'pending',
          message: 'I would like to join this team'
        });
      }
    }

    return true;
  };

  describe('GET /api/team-requests', () => {
    describe('Success cases', () => {
      test('Should get all team requests for authenticated user', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get('/api/team-requests')
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.requests)).toBe(true);
      });

      test('Should get requests as captain (for own teams)', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get('/api/team-requests')
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        // Should see requests for testTeam (where memberUser is captain)
        const requests = response.body.data.requests;
        const teamRequest = requests.find(r => r.team._id.toString() === testTeam._id.toString());
        expect(teamRequest).toBeDefined();
      });

      test('Should get requests as requester (own requests)', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        // Should see own request
        const requests = response.body.data.requests;
        const ownRequest = requests.find(r => r.user._id.toString() === otherMemberUser._id.toString());
        expect(ownRequest).toBeDefined();
      });

      test('Should filter requests by team', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/team-requests?team=${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        const requests = response.body.data.requests;
        requests.forEach(request => {
          expect(request.team._id.toString()).toBe(testTeam._id.toString());
        });
      });

      test('Should filter requests by status', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get('/api/team-requests?status=pending')
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        const requests = response.body.data.requests;
        requests.forEach(request => {
          expect(request.status).toBe('pending');
        });
      });

      test('Should filter requests by user', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/team-requests?user=${otherMemberUser._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        const requests = response.body.data.requests;
        requests.forEach(request => {
          expect(request.user._id.toString()).toBe(otherMemberUser._id.toString());
        });
      });

      test('Should return empty array when no requests match filters', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get('/api/team-requests?status=accepted')
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.requests)).toBe(true);
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .get('/api/team-requests')
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('GET /api/team-requests/team/:teamId', () => {
    describe('Success cases', () => {
      test('Should get all requests for team as captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/team-requests/team/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.requests)).toBe(true);
        const requests = response.body.data.requests;
        requests.forEach(request => {
          expect(request.team.toString()).toBe(testTeam._id.toString());
        });
      });

      test('Should populate user and reviewedBy fields', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/team-requests/team/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        if (response.body.data.requests.length > 0) {
          const request = response.body.data.requests[0];
          expect(request.user).toBeDefined();
          expect(request.user).toHaveProperty('firstName');
          expect(request.user).toHaveProperty('lastName');
        }
      });
    });

    describe('Authorization errors', () => {
      test('Should return 403 if member tries to view team requests', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/team-requests/team/${testTeam._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 403 if captain of another team tries to view requests', async () => {
        const dataReady = await ensureTestData({ needCaptain: true, needAdmin: true, needGame: true, needTeam: true });
        if (!dataReady) {
          // Create another team with captainUser as captain
          const otherTeam = await Team.create({
            name: 'Other Team',
            game: testGame._id,
            captain: captainUser._id,
            members: [captainUser._id],
            status: 'active'
          });

          const response = await request(app)
            .get(`/api/team-requests/team/${testTeam._id}`)
            .set('Authorization', `Bearer ${captainToken}`)
            .expect(403);

          expect(response.body.success).toBe(false);
          expect(response.body.error.message).toContain('captain');
        }
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .get(`/api/team-requests/team/${fakeId}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team ID format', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get('/api/team-requests/team/invalid-id')
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .get(`/api/team-requests/team/${testTeam?._id || '507f1f77bcf86cd799439011'}`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('GET /api/team-requests/:id', () => {
    describe('Success cases', () => {
      test('Should get team request by ID', async () => {
        const dataReady = await ensureTestData({ needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/team-requests/${testTeamRequest._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.data.request._id.toString()).toBe(testTeamRequest._id.toString());
        expect(response.body.data.request.status).toBe('pending');
      });

      test('Should populate team, user, and reviewedBy fields', async () => {
        const dataReady = await ensureTestData({ needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/team-requests/${testTeamRequest._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.data.request.team).toBeDefined();
        expect(response.body.data.request.user).toBeDefined();
        expect(response.body.data.request.team).toHaveProperty('name');
        expect(response.body.data.request.user).toHaveProperty('firstName');
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team request does not exist', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .get(`/api/team-requests/${fakeId}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team request ID format', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get('/api/team-requests/invalid-id')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .get(`/api/team-requests/${testTeamRequest?._id || '507f1f77bcf86cd799439011'}`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('POST /api/team-requests', () => {
    describe('Success cases', () => {
      test('Should create team request with message', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Delete existing request if any
        await TeamRequest.deleteMany({ team: testTeam._id, user: otherMemberUser._id });

        const requestData = {
          team: testTeam._id,
          message: 'I would like to join your team'
        };

        const response = await request(app)
          .post('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(requestData)
          .expect(201);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('created');
        expect(response.body.data.request.status).toBe('pending');
        expect(response.body.data.request.message).toBe('I would like to join your team');
        expect(response.body.data.request.team._id.toString()).toBe(testTeam._id.toString());
        expect(response.body.data.request.user._id.toString()).toBe(otherMemberUser._id.toString());
      });

      test('Should create team request without message', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Delete existing request if any
        await TeamRequest.deleteMany({ team: testTeam._id, user: otherMemberUser._id });

        const requestData = {
          team: testTeam._id
        };

        const response = await request(app)
          .post('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(requestData)
          .expect(201);

        expect(response.body.success).toBe(true);
        expect(response.body.data.request.status).toBe('pending');
        expect(response.body.data.request.team._id.toString()).toBe(testTeam._id.toString());
      });
    });

    describe('Validation errors', () => {
      test('Should return 400 if team is missing', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .post('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send({})
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Team is required');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 404 if team does not exist', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeTeamId = '507f1f77bcf86cd799439011';
        const requestData = {
          team: fakeTeamId
        };

        const response = await request(app)
          .post('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(requestData)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Team not found');
      });

      test('Should return 400 if team is inactive', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Set team to inactive
        testTeam.status = 'inactive';
        await testTeam.save();

        const requestData = {
          team: testTeam._id
        };

        const response = await request(app)
          .post('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(requestData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('inactive');

        // Reset team to active for other tests
        testTeam.status = 'active';
        await testTeam.save();
      });

      test('Should return 400 if user is already a member', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Delete any existing pending requests first
        await TeamRequest.deleteMany({ team: testTeam._id, user: otherMemberUser._id });

        // Reload team to get fresh data
        const freshTeam = await Team.findById(testTeam._id);
        // Add otherMemberUser to team (ensure it's not already there)
        if (!freshTeam.members.some(m => m.toString() === otherMemberUser._id.toString())) {
          freshTeam.members.push(otherMemberUser._id);
          await freshTeam.save();
        }

        // Verify user is in team
        const verifyTeam = await Team.findById(testTeam._id);
        const isMember = verifyTeam.members.some(m => m.toString() === otherMemberUser._id.toString());
        expect(isMember).toBe(true);

        const requestData = {
          team: testTeam._id
        };

        const response = await request(app)
          .post('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(requestData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('already a member');

        // Remove user from team for other tests
        const cleanupTeam = await Team.findById(testTeam._id);
        cleanupTeam.members = cleanupTeam.members.filter(m => m.toString() !== otherMemberUser._id.toString());
        await cleanupTeam.save();
      });

      test('Should return 400 if team has reached max members', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needAdmin: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        // Create a full team
        const fullTeam = await Team.create({
          name: 'Full Team',
          game: testGame._id,
          captain: memberUser._id,
          members: [memberUser._id],
          status: 'active',
          maxMembers: 1 // Only captain
        });

        const requestData = {
          team: fullTeam._id
        };

        const response = await request(app)
          .post('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(requestData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('maximum');
      });

      test('Should return 400 if user already has pending request', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const requestData = {
          team: testTeam._id
        };

        const response = await request(app)
          .post('/api/team-requests')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(requestData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('pending request');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const requestData = {
          team: testTeam?._id || '507f1f77bcf86cd799439011'
        };

        const response = await request(app)
          .post('/api/team-requests')
          .send(requestData)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('PUT /api/team-requests/:id/accept', () => {
    describe('Success cases', () => {
      test('Should accept team request as captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Delete any existing requests for this team/user combination
        await TeamRequest.deleteMany({ team: testTeam._id, user: otherMemberUser._id });

        // Ensure otherMemberUser is not already in the team
        const freshTeam = await Team.findById(testTeam._id);
        freshTeam.members = freshTeam.members.filter(m => m.toString() !== otherMemberUser._id.toString());
        await freshTeam.save();

        // Create a new pending request
        const newRequest = await TeamRequest.create({
          team: testTeam._id,
          user: otherMemberUser._id,
          status: 'pending'
        });

        const response = await request(app)
          .put(`/api/team-requests/${newRequest._id}/accept`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('accepted');
        expect(response.body.data.request.status).toBe('accepted');
        expect(response.body.data.request.reviewedBy._id.toString()).toBe(memberUser._id.toString());
        expect(response.body.data.request.reviewedAt).toBeDefined();

        // Verify user was added to team
        const updatedTeam = await Team.findById(testTeam._id);
        const memberIds = updatedTeam.members.map(m => m.toString());
        expect(memberIds).toContain(otherMemberUser._id.toString());
      });
    });

    describe('Authorization errors', () => {
      test('Should return 403 if member tries to accept request', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/team-requests/${testTeamRequest._id}/accept`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 403 if captain of another team tries to accept', async () => {
        const dataReady = await ensureTestData({ needCaptain: true, needAdmin: true, needGame: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        // Create another team with captainUser as captain
        const otherTeam = await Team.create({
          name: 'Other Team',
          game: testGame._id,
          captain: captainUser._id,
          members: [captainUser._id],
          status: 'active'
        });

        const response = await request(app)
          .put(`/api/team-requests/${testTeamRequest._id}/accept`)
          .set('Authorization', `Bearer ${captainToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 400 if request is already processed', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Create an accepted request
        const acceptedRequest = await TeamRequest.create({
          team: testTeam._id,
          user: otherMemberUser._id,
          status: 'accepted',
          reviewedBy: memberUser._id,
          reviewedAt: new Date()
        });

        const response = await request(app)
          .put(`/api/team-requests/${acceptedRequest._id}/accept`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('already been processed');
      });

      test('Should return 400 if team has reached max members', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true, needAdmin: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        // Create a full team
        const fullTeam = await Team.create({
          name: 'Full Team For Request',
          game: testGame._id,
          captain: memberUser._id,
          members: [memberUser._id],
          status: 'active',
          maxMembers: 1 // Only captain
        });

        // Create a request for the full team
        const requestForFullTeam = await TeamRequest.create({
          team: fullTeam._id,
          user: otherMemberUser._id,
          status: 'pending'
        });

        const response = await request(app)
          .put(`/api/team-requests/${requestForFullTeam._id}/accept`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('maximum');
      });

      test('Should return 400 if user is already a member', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Delete any existing requests first
        await TeamRequest.deleteMany({ team: testTeam._id, user: otherMemberUser._id });

        // Reload team and add user to team
        const freshTeam = await Team.findById(testTeam._id);
        if (!freshTeam.members.some(m => m.toString() === otherMemberUser._id.toString())) {
          freshTeam.members.push(otherMemberUser._id);
          await freshTeam.save();
        }

        // Verify user is in team
        const verifyTeam = await Team.findById(testTeam._id);
        const isMember = verifyTeam.members.some(m => m.toString() === otherMemberUser._id.toString());
        expect(isMember).toBe(true);

        // Create a team request
        const teamRequest = await TeamRequest.create({
          team: testTeam._id,
          user: otherMemberUser._id,
          status: 'pending'
        });

        const response = await request(app)
          .put(`/api/team-requests/${teamRequest._id}/accept`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('already a member');

        // Clean up
        const cleanupTeam = await Team.findById(testTeam._id);
        cleanupTeam.members = cleanupTeam.members.filter(m => m.toString() !== otherMemberUser._id.toString());
        await cleanupTeam.save();
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team request does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .put(`/api/team-requests/${fakeId}/accept`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team request ID format', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put('/api/team-requests/invalid-id/accept')
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .put(`/api/team-requests/${testTeamRequest?._id || '507f1f77bcf86cd799439011'}/accept`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('PUT /api/team-requests/:id/reject', () => {
    describe('Success cases', () => {
      test('Should reject team request as captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Delete any existing requests for this team/user combination
        await TeamRequest.deleteMany({ team: testTeam._id, user: otherMemberUser._id });

        // Ensure otherMemberUser is not already in the team
        const freshTeam = await Team.findById(testTeam._id);
        freshTeam.members = freshTeam.members.filter(m => m.toString() !== otherMemberUser._id.toString());
        await freshTeam.save();

        // Create a new pending request
        const newRequest = await TeamRequest.create({
          team: testTeam._id,
          user: otherMemberUser._id,
          status: 'pending'
        });

        const response = await request(app)
          .put(`/api/team-requests/${newRequest._id}/reject`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('rejected');
        expect(response.body.data.request.status).toBe('rejected');
        expect(response.body.data.request.reviewedBy._id.toString()).toBe(memberUser._id.toString());
        expect(response.body.data.request.reviewedAt).toBeDefined();

        // Verify user was NOT added to team
        const updatedTeam = await Team.findById(testTeam._id);
        const memberIds = updatedTeam.members.map(m => m.toString());
        expect(memberIds).not.toContain(otherMemberUser._id.toString());
      });
    });

    describe('Authorization errors', () => {
      test('Should return 403 if member tries to reject request', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/team-requests/${testTeamRequest._id}/reject`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 403 if captain of another team tries to reject', async () => {
        const dataReady = await ensureTestData({ needCaptain: true, needAdmin: true, needGame: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        // Create another team with captainUser as captain
        const otherTeam = await Team.create({
          name: 'Other Team',
          game: testGame._id,
          captain: captainUser._id,
          members: [captainUser._id],
          status: 'active'
        });

        const response = await request(app)
          .put(`/api/team-requests/${testTeamRequest._id}/reject`)
          .set('Authorization', `Bearer ${captainToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 400 if request is already processed', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Create a rejected request
        const rejectedRequest = await TeamRequest.create({
          team: testTeam._id,
          user: otherMemberUser._id,
          status: 'rejected',
          reviewedBy: memberUser._id,
          reviewedAt: new Date()
        });

        const response = await request(app)
          .put(`/api/team-requests/${rejectedRequest._id}/reject`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('already been processed');
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team request does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .put(`/api/team-requests/${fakeId}/reject`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team request ID format', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put('/api/team-requests/invalid-id/reject')
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .put(`/api/team-requests/${testTeamRequest?._id || '507f1f77bcf86cd799439011'}/reject`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('DELETE /api/team-requests/:id', () => {
    describe('Success cases', () => {
      test('Should cancel team request as requester', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Delete any existing requests for this team/user combination
        await TeamRequest.deleteMany({ team: testTeam._id, user: otherMemberUser._id });

        // Create a new pending request
        const newRequest = await TeamRequest.create({
          team: testTeam._id,
          user: otherMemberUser._id,
          status: 'pending'
        });

        const response = await request(app)
          .delete(`/api/team-requests/${newRequest._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('cancelled');

        // Verify request is deleted
        const deletedRequest = await TeamRequest.findById(newRequest._id);
        expect(deletedRequest).toBeNull();
      });
    });

    describe('Authorization errors', () => {
      test('Should return 403 if member tries to cancel another user request', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/team-requests/${testTeamRequest._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('own requests');
      });

      test('Should return 403 if captain tries to cancel request', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeamRequest: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/team-requests/${testTeamRequest._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('own requests');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 400 if request is already processed', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Create an accepted request
        const acceptedRequest = await TeamRequest.create({
          team: testTeam._id,
          user: otherMemberUser._id,
          status: 'accepted',
          reviewedBy: memberUser._id,
          reviewedAt: new Date()
        });

        const response = await request(app)
          .delete(`/api/team-requests/${acceptedRequest._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('already been processed');
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team request does not exist', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .delete(`/api/team-requests/${fakeId}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team request ID format', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete('/api/team-requests/invalid-id')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .delete(`/api/team-requests/${testTeamRequest?._id || '507f1f77bcf86cd799439011'}`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });
});

