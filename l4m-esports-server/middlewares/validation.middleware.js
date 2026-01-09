import { BadRequestError } from '../utils/errors.js';

/**
 * Validation middleware for authentication
 */

/**
 * Validate sign up request
 */
export const validateSignUp = (req, res, next) => {
  const { email, password, firstName, lastName, gamertag } = req.body;

  // Check required fields
  if (!email || !password) {
    return next(new BadRequestError('Email and password are required'));
  }

  if (!firstName || firstName.trim() === '') {
    return next(new BadRequestError('First name is required'));
  }

  if (!lastName || lastName.trim() === '') {
    return next(new BadRequestError('Last name is required'));
  }

  if (!gamertag || gamertag.trim() === '') {
    return next(new BadRequestError('Gamertag is required'));
  }

  // Validate email format
  const emailRegex = /^\S+@\S+\.\S+$/;
  if (!emailRegex.test(email)) {
    return next(new BadRequestError('Please provide a valid email address'));
  }

  // Validate password length
  if (password.length < 6) {
    return next(new BadRequestError('Password must be at least 6 characters long'));
  }

  // Validate gamertag length
  const trimmedGamertag = gamertag.trim();
  if (trimmedGamertag.length < 2) {
    return next(new BadRequestError('Gamertag must be at least 2 characters long'));
  }

  if (trimmedGamertag.length > 30) {
    return next(new BadRequestError('Gamertag must not exceed 30 characters'));
  }

  next();
};

/**
 * Validate sign in request
 */
export const validateSignIn = (req, res, next) => {
  const { email, password } = req.body;

  // Check required fields
  if (!email || !password) {
    return next(new BadRequestError('Email and password are required'));
  }

  next();
};

/**
 * Validate game creation/update
 */
export const validateGame = (req, res, next) => {
  const { name } = req.body;

  if (!name || name.trim().length === 0) {
    return next(new BadRequestError('Game name is required'));
  }

  if (name.length < 2) {
    return next(new BadRequestError('Game name must be at least 2 characters long'));
  }

  next();
};

/**
 * Validate team creation
 */
export const validateTeam = (req, res, next) => {
  const { name, game } = req.body;

  if (!name || name.trim().length === 0) {
    return next(new BadRequestError('Team name is required'));
  }

  if (name.length < 2) {
    return next(new BadRequestError('Team name must be at least 2 characters long'));
  }

  if (!game) {
    return next(new BadRequestError('Game is required'));
  }

  if (req.body.maxMembers !== undefined && req.body.maxMembers !== null && (req.body.maxMembers < 1 || req.body.maxMembers > 50)) {
    return next(new BadRequestError('Max members must be between 1 and 50'));
  }

  next();
};

/**
 * Validate team update (game is optional, cannot be changed)
 */
export const validateTeamUpdate = (req, res, next) => {
  const { name } = req.body;

  // Name is optional in update, but if provided, must be valid
  if (name !== undefined) {
    if (name.trim().length === 0) {
      return next(new BadRequestError('Team name cannot be empty'));
    }

    if (name.length < 2) {
      return next(new BadRequestError('Team name must be at least 2 characters long'));
    }
  }

  // Game should not be changed in update (remove it from updateData if present)
  if (req.body.game) {
    delete req.body.game;
  }

  if (req.body.maxMembers && (req.body.maxMembers < 1 || req.body.maxMembers > 50)) {
    return next(new BadRequestError('Max members must be between 1 and 50'));
  }

  // Validate status if provided
  if (req.body.status && !['active', 'inactive', 'archived'].includes(req.body.status)) {
    return next(new BadRequestError('Status must be one of: active, inactive, archived'));
  }

  next();
};

/**
 * Validate team request creation
 */
export const validateTeamRequest = (req, res, next) => {
  const { team } = req.body;

  if (!team) {
    return next(new BadRequestError('Team is required'));
  }

  next();
};

/**
 * Validate invite user to team
 */
export const validateInviteUser = (req, res, next) => {
  const { userId, gamertag } = req.body;

  if (!userId && !gamertag) {
    return next(new BadRequestError('userId or gamertag is required'));
  }

  if (userId && gamertag) {
    return next(new BadRequestError('Provide either userId or gamertag, not both'));
  }

  next();
};
