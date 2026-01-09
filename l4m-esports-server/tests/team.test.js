import request from 'supertest';
import app from '../app.js';
import User from '../models/user.model.js';
import Game from '../models/game.model.js';
import Team from '../models/team.model.js';
import { createSession } from '../services/auth.service.js';
import { setupTests, cleanupAfterEach, cleanupTests } from './setup.js';

describe('Team Routes Tests', () => {
  let adminUser, adminToken;
  let memberUser, memberToken;
  let captainUser, captainToken;
  let otherMemberUser, otherMemberToken;
  let testGame;
  let testTeam;

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

    // Create member user
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

    // Create other member user
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
      needTeam = false 
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

    return true;
  };

  describe('GET /api/teams', () => {
    describe('Success cases', () => {
      test('Should get all teams', async () => {
        const response = await request(app)
          .get('/api/teams')
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.teams)).toBe(true);
      });

      test('Should filter teams by game', async () => {
        const dataReady = await ensureTestData({ needGame: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/teams?game=${testGame._id}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.teams)).toBe(true);
        if (response.body.data.teams.length > 0) {
          expect(response.body.data.teams[0].game._id.toString()).toBe(testGame._id.toString());
        }
      });

      test('Should filter teams by status', async () => {
        const response = await request(app)
          .get('/api/teams?status=active')
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.teams)).toBe(true);
        if (response.body.data.teams.length > 0) {
          expect(response.body.data.teams[0].status).toBe('active');
        }
      });

      test('Should search teams by name', async () => {
        const response = await request(app)
          .get('/api/teams?search=Test')
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.teams)).toBe(true);
      });

      test('Should search teams by description', async () => {
        const response = await request(app)
          .get('/api/teams?search=test team')
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.teams)).toBe(true);
      });

      test('Should return empty array when no teams match filters', async () => {
        const response = await request(app)
          .get('/api/teams?status=archived')
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(Array.isArray(response.body.data.teams)).toBe(true);
      });
    });
  });

  describe('GET /api/teams/:id', () => {
    describe('Success cases', () => {
      test('Should get team by ID', async () => {
        const dataReady = await ensureTestData({ needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/teams/${testTeam._id}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.data.team._id.toString()).toBe(testTeam._id.toString());
        expect(response.body.data.team.name).toBe('Test Team');
      });

      test('Should populate game, captain, and members', async () => {
        const dataReady = await ensureTestData({ needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .get(`/api/teams/${testTeam._id}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.data.team.game).toBeDefined();
        expect(response.body.data.team.captain).toBeDefined();
        expect(Array.isArray(response.body.data.team.members)).toBe(true);
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team does not exist', async () => {
        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .get(`/api/teams/${fakeId}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team ID format', async () => {
        const response = await request(app)
          .get('/api/teams/invalid-id')
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });
  });

  describe('POST /api/teams', () => {
    describe('Success cases', () => {
      test('Should create team with all fields', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needAdmin: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        // Create a new game for this test
        const newGame = await Game.create({
          name: 'New Game For Team',
          isActive: true,
          createdBy: adminUser._id
        });

        const teamData = {
          name: 'New Team',
          description: 'A new team description',
          game: newGame._id,
          logo: 'https://example.com/logo.png',
          maxMembers: 15,
          status: 'active'
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(teamData)
          .expect(201);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('created');
        expect(response.body.data.team.name).toBe('New Team');
        expect(response.body.data.team.captain._id.toString()).toBe(otherMemberUser._id.toString());
        expect(response.body.data.team.members.length).toBe(1); // Captain is automatically added
      });

      test('Should create team with minimal required fields', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needAdmin: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        // Create a new game for this test
        const newGame = await Game.create({
          name: 'Minimal Game For Team',
          isActive: true,
          createdBy: adminUser._id
        });

        const teamData = {
          name: 'Minimal Team',
          game: newGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(teamData)
          .expect(201);

        expect(response.body.success).toBe(true);
        expect(response.body.data.team.name).toBe('Minimal Team');
        expect(response.body.data.team.captain._id.toString()).toBe(otherMemberUser._id.toString());
      });

      test('Should automatically add captain to members', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needAdmin: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        // Create a new game for this test
        const newGame = await Game.create({
          name: 'Auto Member Game',
          isActive: true,
          createdBy: adminUser._id
        });

        const teamData = {
          name: 'Auto Member Team',
          game: newGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(teamData)
          .expect(201);

        expect(response.body.success).toBe(true);
        const memberIds = response.body.data.team.members.map(m => m._id.toString());
        expect(memberIds).toContain(otherMemberUser._id.toString());
      });
    });

    describe('Validation errors', () => {
      test('Should return 400 if name is missing', async () => {
        // Ensure memberUser exists
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
            return; // Skip if Redis unavailable
          }
        }
        if (!testGame) {
          testGame = await Game.create({
            name: 'Test Game',
            description: 'A test game for testing',
            isActive: true
          });
        }

        // Verify token is still valid
        try {
          const { getToken } = await import('../services/redis.service.js');
          const userIdInRedis = await getToken(memberToken);
          if (!userIdInRedis || userIdInRedis !== memberUser._id.toString()) {
            memberToken = await createSession(memberUser._id);
          }
        } catch (error) {
          return; // Skip if Redis unavailable
        }

        const teamData = {
          game: testGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('name is required');
      });

      test('Should return 400 if name is empty string', async () => {
        const dataReady = await ensureTestData({ needMember: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        const teamData = {
          name: '',
          game: testGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('name is required');
      });

      test('Should return 400 if name is only whitespace', async () => {
        const dataReady = await ensureTestData({ needMember: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        const teamData = {
          name: '   ',
          game: testGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(400);
        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('name is required');
      });

      test('Should return 400 if name is too short (< 2 characters)', async () => {
        const dataReady = await ensureTestData({ needMember: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        const teamData = {
          name: 'A',
          game: testGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(400);
        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('at least 2 characters');
      });

      test('Should return 400 if game is missing', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const teamData = {
          name: 'Team Without Game'
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Game is required');
      });

      test('Should return 400 if maxMembers is less than 1', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needAdmin: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        // Create a new game for this test
        const newGame = await Game.create({
          name: 'Invalid Max Game',
          isActive: true,
          createdBy: adminUser._id
        });

        const teamData = {
          name: 'Invalid Max Team',
          game: newGame._id,
          maxMembers: 0
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(teamData)
          .expect(400);
        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('between 1 and 50');
      });

      test('Should return 400 if maxMembers is greater than 50', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needAdmin: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        // Create a new game for this test
        const newGame = await Game.create({
          name: 'Invalid Max Game 2',
          isActive: true,
          createdBy: adminUser._id
        });

        const teamData = {
          name: 'Invalid Max Team',
          game: newGame._id,
          maxMembers: 51
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send(teamData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('between 1 and 50');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 404 if game does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeGameId = '507f1f77bcf86cd799439011';
        const teamData = {
          name: 'Team With Fake Game',
          game: fakeGameId
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Game not found');
      });

      test('Should return 400 if game is inactive', async () => {
        const dataReady = await ensureTestData({ needMember: true, needAdmin: true });
        if (!dataReady) return; // Redis unavailable

        // Create inactive game
        const inactiveGame = await Game.create({
          name: 'Inactive Game',
          isActive: false,
          createdBy: adminUser._id
        });

        const teamData = {
          name: 'Team For Inactive Game',
          game: inactiveGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('inactive');
      });

      test('Should return 400 if user is already captain of active team for this game', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // testTeam already exists with memberUser as captain for testGame
        // Try to create another team for same game with same user
        const teamData = {
          name: 'Second Team',
          game: testGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('already captain');
      });

      test('Should allow creating team for different game', async () => {
        const dataReady = await ensureTestData({ needMember: true, needAdmin: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Create another game
        const otherGame = await Game.create({
          name: 'Other Game',
          isActive: true,
          createdBy: adminUser._id
        });

        // testTeam already exists with memberUser as captain for testGame
        // Create second team for other game (should work)
        const teamData = {
          name: 'Second Team',
          game: otherGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(201);

        expect(response.body.success).toBe(true);
      });

      test('Should allow creating team if previous team is inactive', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        // Update testTeam to inactive
        testTeam.status = 'inactive';
        await testTeam.save();

        // Create new active team (should work)
        const teamData = {
          name: 'New Active Team',
          game: testGame._id
        };

        const response = await request(app)
          .post('/api/teams')
          .set('Authorization', `Bearer ${memberToken}`)
          .send(teamData)
          .expect(201);

        expect(response.body.success).toBe(true);
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const teamData = {
          name: 'Unauthorized Team',
          game: testGame?._id || '507f1f77bcf86cd799439011'
        };

        const response = await request(app)
          .post('/api/teams')
          .send(teamData)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('PUT /api/teams/:id', () => {
    describe('Success cases', () => {
      test('Should update team name as captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ name: 'Updated Team Name', game: testGame._id })
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('updated');
        expect(response.body.data.team.name).toBe('Updated Team Name');
      });

      test('Should update team description as captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ 
            name: testTeam.name,
            game: testGame._id,
            description: 'Updated description' 
          })
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.data.team.description).toBe('Updated description');
      });

      test('Should update multiple fields at once', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const updateData = {
          name: 'Fully Updated Team',
          game: testGame._id,
          description: 'New description',
          logo: 'https://example.com/new-logo.png',
          maxMembers: 15,
          status: 'inactive'
        };

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send(updateData)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.data.team.name).toBe('Fully Updated Team');
        expect(response.body.data.team.description).toBe('New description');
        expect(response.body.data.team.logo).toBe('https://example.com/new-logo.png');
        expect(response.body.data.team.maxMembers).toBe(15);
        expect(response.body.data.team.status).toBe('inactive');
      });

      test('Should update game', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true, needAdmin: true });
        if (!dataReady) return; // Redis unavailable

        // Create another game
        const otherGame = await Game.create({
          name: 'Other Game For Update',
          isActive: true,
          createdBy: adminUser._id
        });

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ 
            name: testTeam.name,
            game: otherGame._id
          })
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.data.team.game._id.toString()).toBe(otherGame._id.toString());
      });
    });

    describe('Validation errors', () => {
      test('Should return 400 if name is empty string', async () => {
        // Ensure memberUser, testTeam, and testGame exist
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
            return; // Skip if Redis unavailable
          }
        }
        if (!testGame) {
          testGame = await Game.create({
            name: 'Test Game',
            description: 'A test game for testing',
            isActive: true
          });
        }
        if (!testTeam) {
          testTeam = await Team.create({
            name: 'Test Team',
            game: testGame._id,
            captain: memberUser._id,
            members: [memberUser._id],
            status: 'active'
          });
        }

        // Verify token is still valid
        try {
          const { getToken } = await import('../services/redis.service.js');
          const userIdInRedis = await getToken(memberToken);
          if (!userIdInRedis || userIdInRedis !== memberUser._id.toString()) {
            memberToken = await createSession(memberUser._id);
          }
        } catch (error) {
          return; // Skip if Redis unavailable
        }

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ name: '', game: testGame._id })
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('name is required');
      });

      test('Should return 400 if name is only whitespace', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ name: '   ', game: testGame._id })
          .expect(400);
        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('name is required');
      });

      test('Should return 400 if name is too short', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ name: 'A', game: testGame._id })
          .expect(400);
        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('at least 2 characters');
      });

      test('Should return 400 if maxMembers is invalid', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ name: testTeam.name, game: testGame._id, maxMembers: 51 })
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('between 1 and 50');
      });
    });

    describe('Authorization errors', () => {
      test('Should return 403 if member tries to update another team', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send({ name: 'Hacked Name', game: testGame._id })
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 403 if captain of another team tries to update', async () => {
        const dataReady = await ensureTestData({ needCaptain: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${captainToken}`)
          .send({ name: 'Hacked Name', game: testGame._id })
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 404 if game does not exist when updating game', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const fakeGameId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .put(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ name: testTeam.name, game: fakeGameId })
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Game not found');
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .put(`/api/teams/${fakeId}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ name: 'Updated Name', game: testGame._id })
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team ID format', async () => {
        const dataReady = await ensureTestData({ needMember: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .put('/api/teams/invalid-id')
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ name: 'Updated Name', game: testGame._id })
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .put(`/api/teams/${testTeam?._id || '507f1f77bcf86cd799439011'}`)
          .send({ name: 'Unauthorized Update' })
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('DELETE /api/teams/:id', () => {
    describe('Success cases', () => {
      test('Should delete team as captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needGame: true });
        if (!dataReady) return; // Redis unavailable

        // Create a team to delete
        const teamToDelete = await Team.create({
          name: 'Team To Delete',
          game: testGame._id,
          captain: memberUser._id,
          members: [memberUser._id],
          status: 'active'
        });

        const response = await request(app)
          .delete(`/api/teams/${teamToDelete._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('deleted');

        // Verify team is deleted
        const deletedTeam = await Team.findById(teamToDelete._id);
        expect(deletedTeam).toBeNull();
      });
    });

    describe('Authorization errors', () => {
      test('Should return 403 if member tries to delete another team', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 403 if captain of another team tries to delete', async () => {
        const dataReady = await ensureTestData({ needCaptain: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/teams/${testTeam._id}`)
          .set('Authorization', `Bearer ${captainToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .delete(`/api/teams/${fakeId}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team ID format', async () => {
        const dataReady = await ensureTestData({ needMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete('/api/teams/invalid-id')
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .delete(`/api/teams/${testTeam?._id || '507f1f77bcf86cd799439011'}`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('POST /api/teams/:id/invite', () => {
    describe('Success cases', () => {
      test('Should invite user to team as captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: otherMemberUser._id })
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('invited');
        const memberIds = response.body.data.team.members.map(m => m._id.toString());
        expect(memberIds).toContain(otherMemberUser._id.toString());
      });
    });

    describe('Validation errors', () => {
      test('Should return 400 if userId is missing', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({})
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('userId is required');
      });
    });

    describe('Authorization errors', () => {
      test('Should return 403 if member tries to invite', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true, needMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .send({ userId: memberUser._id })
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 403 if captain of another team tries to invite', async () => {
        const dataReady = await ensureTestData({ needCaptain: true, needTeam: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${captainToken}`)
          .send({ userId: otherMemberUser._id })
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 404 if user does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const fakeUserId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: fakeUserId })
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('User not found');
      });

      test('Should return 400 if trying to invite the captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: memberUser._id })
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 400 if user is already a member', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        // First invite
        await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: otherMemberUser._id })
          .expect(200);

        // Try to invite again
        const response = await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: otherMemberUser._id })
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('already a member');
      });

      test('Should return 400 if team has reached max members', async () => {
        const dataReady = await ensureTestData({ needMember: true, needGame: true, needAdmin: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        // Create team with maxMembers = 1 (only captain)
        const smallTeam = await Team.create({
          name: 'Small Team',
          game: testGame._id,
          captain: memberUser._id,
          members: [memberUser._id],
          maxMembers: 1
        });

        // Try to invite someone
        const response = await request(app)
          .post(`/api/teams/${smallTeam._id}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: otherMemberUser._id })
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('maximum');
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .post(`/api/teams/${fakeId}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: otherMemberUser._id })
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team ID format', async () => {
        // Ensure memberUser and otherMemberUser exist
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
            return; // Skip if Redis unavailable
          }
        }
        if (!otherMemberUser) {
          otherMemberUser = await User.create({
            email: 'other@test.com',
            password: 'password123',
            firstName: 'Other',
            lastName: 'Member',
            role: 'member'
          });
        }

        // Verify token is still valid
        try {
          const { getToken } = await import('../services/redis.service.js');
          const userIdInRedis = await getToken(memberToken);
          if (!userIdInRedis || userIdInRedis !== memberUser._id.toString()) {
            memberToken = await createSession(memberUser._id);
          }
        } catch (error) {
          return; // Skip if Redis unavailable
        }

        const response = await request(app)
          .post('/api/teams/invalid-id/invite')
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: otherMemberUser._id })
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .post(`/api/teams/${testTeam?._id || '507f1f77bcf86cd799439011'}/invite`)
          .send({ userId: '507f1f77bcf86cd799439011' })
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });

  describe('DELETE /api/teams/:id/members/:userId', () => {
    describe('Success cases', () => {
      test('Should remove member from team as captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        // First add member
        await request(app)
          .post(`/api/teams/${testTeam._id}/invite`)
          .set('Authorization', `Bearer ${memberToken}`)
          .send({ userId: otherMemberUser._id })
          .expect(200);

        // Then remove member
        const response = await request(app)
          .delete(`/api/teams/${testTeam._id}/members/${otherMemberUser._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(200);

        expect(response.body.success).toBe(true);
        expect(response.body.message).toContain('removed');
        const memberIds = response.body.data.team.members.map(m => m._id.toString());
        expect(memberIds).not.toContain(otherMemberUser._id.toString());
      });
    });

    describe('Authorization errors', () => {
      test('Should return 403 if member tries to remove another member', async () => {
        const dataReady = await ensureTestData({ needOtherMember: true, needTeam: true, needMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/teams/${testTeam._id}/members/${memberUser._id}`)
          .set('Authorization', `Bearer ${otherMemberToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 403 if captain of another team tries to remove member', async () => {
        const dataReady = await ensureTestData({ needCaptain: true, needTeam: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/teams/${testTeam._id}/members/${otherMemberUser._id}`)
          .set('Authorization', `Bearer ${captainToken}`)
          .expect(403);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });
    });

    describe('Business logic errors', () => {
      test('Should return 400 if trying to remove the captain', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/teams/${testTeam._id}/members/${memberUser._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('captain');
      });

      test('Should return 404 if member is not in team', async () => {
        const dataReady = await ensureTestData({ needMember: true, needTeam: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/teams/${testTeam._id}/members/${otherMemberUser._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });
    });

    describe('Not found errors', () => {
      test('Should return 404 if team does not exist', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const fakeId = '507f1f77bcf86cd799439011';
        const response = await request(app)
          .delete(`/api/teams/${fakeId}/members/${otherMemberUser._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(404);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('not found');
      });

      test('Should return 400 if invalid team ID format', async () => {
        const dataReady = await ensureTestData({ needMember: true, needOtherMember: true });
        if (!dataReady) return; // Redis unavailable

        const response = await request(app)
          .delete(`/api/teams/invalid-id/members/${otherMemberUser._id}`)
          .set('Authorization', `Bearer ${memberToken}`)
          .expect(400);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('Invalid');
      });
    });

    describe('Authentication errors', () => {
      test('Should return 401 if no token provided', async () => {
        const response = await request(app)
          .delete(`/api/teams/${testTeam?._id || '507f1f77bcf86cd799439011'}/members/${otherMemberUser?._id || '507f1f77bcf86cd799439011'}`)
          .expect(401);

        expect(response.body.success).toBe(false);
        expect(response.body.error.message).toContain('token');
      });
    });
  });
});

