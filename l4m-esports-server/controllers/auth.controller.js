import { 
  validateSignUpData,
  createUser, 
  findUserByEmailWithPassword,
  createSession
} from '../services/auth.service.js';
import { revokeToken } from '../services/redis.service.js';
import { BadRequestError, UnauthorizedError } from '../utils/errors.js';

/**
 * Sign up a new user
 */
export const signUp = async (req, res, next) => {
  try {
    const { email, password, firstName, lastName, gamertag, twitchUsername } = req.body;

    // Validate sign up data (check for duplicates)
    await validateSignUpData(email, gamertag, twitchUsername);

    // Create user
    const user = await createUser({ email, password, firstName, lastName, gamertag, twitchUsername });

    // Create session token
    const token = await createSession(user._id);

    res.status(201).json({
      success: true,
      message: 'User signed up successfully',
      data: {
        user,
        token
      }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Sign in user
 */
export const signIn = async (req, res, next) => {
  try {
    const { email, password } = req.body;

    // Find user with password
    const user = await findUserByEmailWithPassword(email);

    if (!user) {
      throw new UnauthorizedError('Invalid email or password');
    }

    // Check password
    const isPasswordValid = await user.comparePassword(password);

    if (!isPasswordValid) {
      throw new UnauthorizedError('Invalid email or password');
    }

    // Create session token
    const token = await createSession(user._id);

    // Remove password from response
    user.password = undefined;

    res.status(200).json({
      success: true,
      message: 'Sign in successful',
      data: {
        user,
        token
      }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Sign out user - Revoke token in Redis
 */
export const signOut = async (req, res, next) => {
  try {
    if (req.token) {
      await revokeToken(req.token);
    }

    res.status(200).json({
      success: true,
      message: 'Sign out successful. Token has been revoked.'
    });
  } catch (error) {
    next(error);
  }
};
