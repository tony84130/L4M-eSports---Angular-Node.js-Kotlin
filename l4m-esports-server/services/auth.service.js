import User from '../models/user.model.js';
import jwt from 'jsonwebtoken';
import env from '../config/env.js';
import { BadRequestError } from '../utils/errors.js';
import { storeToken, revokeAllUserTokens } from './redis.service.js';

/**
 * Generate JWT token (valid for 24 hours)
 */
export const generateToken = (id) => {
  return jwt.sign({ id }, env.JWT_SECRET, {
    expiresIn: '24h'
  });
};

/**
 * Check if email already exists
 */
export const checkEmailExists = async (email) => {
  const user = await User.findOne({ email: email.toLowerCase() });
  return !!user;
};

/**
 * Check if gamertag already exists
 */
export const checkGamertagExists = async (gamertag) => {
  const user = await User.findOne({ gamertag });
  return !!user;
};

/**
 * Check if twitchUsername already exists
 */
export const checkTwitchUsernameExists = async (twitchUsername) => {
  const user = await User.findOne({ twitchUsername });
  return !!user;
};

/**
 * Validate sign up data (check for duplicates)
 */
export const validateSignUpData = async (email, gamertag, twitchUsername) => {
  if (await checkEmailExists(email)) {
    throw new BadRequestError('Cet email est déjà utilisé. Veuillez utiliser un autre email.');
  }

  if (await checkGamertagExists(gamertag.trim())) {
    throw new BadRequestError('Ce pseudonyme est déjà pris. Veuillez en choisir un autre.');
  }

  if (twitchUsername && await checkTwitchUsernameExists(twitchUsername)) {
    throw new BadRequestError('Ce nom d\'utilisateur Twitch est déjà enregistré.');
  }
};

/**
 * Create a new user
 */
export const createUser = async (userData) => {
  const user = await User.create({
    email: userData.email.toLowerCase(),
    password: userData.password,
    firstName: userData.firstName,
    lastName: userData.lastName,
    gamertag: userData.gamertag.trim(),
    twitchUsername: userData.twitchUsername || undefined,
    role: 'member'
  });
  
  // Remove password from user object
  user.password = undefined;
  return user;
};

/**
 * Find user by email with password
 */
export const findUserByEmailWithPassword = async (email) => {
  return await User.findOne({ email: email.toLowerCase() }).select('+password');
};

/**
 * Create session token and store in Redis
 * Revokes all previous tokens for the user to ensure single session
 * @param {string|ObjectId} userId - User ID
 * @param {boolean} revokePrevious - Whether to revoke previous tokens (default: true)
 */
export const createSession = async (userId, revokePrevious = true) => {
  const userIdString = userId.toString();
  
  // Revoke all previous tokens if requested
  if (revokePrevious) {
    await revokeAllUserTokens(userIdString);
  }
  
  // Generate and store new token
  const token = generateToken(userId);
  await storeToken(token, userIdString, 86400); // 24h = 86400 seconds
  return token;
};

/**
 * Get user by ID without password (for authentication)
 */
export const getUserByIdForAuth = async (userId) => {
  return await User.findById(userId).select('-password');
};

