import {
  getAllUsers as getAllUsersService,
  getUserById as getUserByIdService,
  canAccessUserProfile,
  isEmailTakenByOtherUser,
  isTwitchUsernameTakenByOtherUser,
  buildUpdateData,
  updateUser as updateUserService,
  updateUserRole as updateUserRoleService,
  deleteUser as deleteUserService,
  isValidRole
} from '../services/user.service.js';
import { BadRequestError, ForbiddenError, NotFoundError } from '../utils/errors.js';
import { emitToAll } from '../services/socket.service.js';

/**
 * Get all users (Admin only)
 */
export const getAllUsers = async (req, res, next) => {
  try {
    const result = await getAllUsersService(req.query);
    
    res.status(200).json({
      success: true,
      data: result.users
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get a specific user by ID
 */
export const getUserById = async (req, res, next) => {
  try {
    const { id } = req.params;
    
    // Check access permission
    if (!canAccessUserProfile(req.user, id)) {
      throw new ForbiddenError('Access denied. You can only view your own profile.');
    }
    
    const user = await getUserByIdService(id);
    
    if (!user) {
      throw new NotFoundError('User not found');
    }
    
    res.status(200).json({
      success: true,
      data: user
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get current authenticated user profile
 */
export const getMe = async (req, res, next) => {
  try {
    const user = await getUserByIdService(req.user._id);
    
    if (!user) {
      throw new NotFoundError('User not found');
    }
    
    res.status(200).json({
      success: true,
      data: user
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update current user profile
 */
export const updateMe = async (req, res, next) => {
  try {
    const userId = req.user._id;
    const { twitchUsername } = req.body;
    
    // Check if twitchUsername is already taken
    if (twitchUsername && await isTwitchUsernameTakenByOtherUser(twitchUsername, userId)) {
      throw new BadRequestError('This Twitch username is already registered');
    }
    
    const updateData = buildUpdateData(req.body, ['firstName', 'lastName', 'twitchUsername', 'preferences', 'location', 'avatar']);
    const user = await updateUserService(userId, updateData);
    
    // Emit socket event for real-time sync
    emitToAll('user:updated', { userId: user._id.toString(), user });
    
    res.status(200).json({
      success: true,
      message: 'Profile updated successfully',
      data: user
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update a user (Admin only)
 */
export const updateUser = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { email, twitchUsername } = req.body;
    
    // Check if user exists
    const existingUser = await getUserByIdService(id);
    if (!existingUser) {
      throw new NotFoundError('User not found');
    }
    
    // Check if email is already taken
    if (email && email !== existingUser.email && await isEmailTakenByOtherUser(email, id)) {
      throw new BadRequestError('Email is already registered');
    }
    
    // Check if twitchUsername is already taken
    if (twitchUsername && twitchUsername !== existingUser.twitchUsername && 
        await isTwitchUsernameTakenByOtherUser(twitchUsername, id)) {
      throw new BadRequestError('This Twitch username is already registered');
    }
    
    const updateData = buildUpdateData(req.body, ['firstName', 'lastName', 'email', 'twitchUsername', 'preferences', 'location', 'avatar']);
    const user = await updateUserService(id, updateData);
    
    // Emit socket event for real-time sync
    emitToAll('user:updated', { userId: user._id.toString(), user });
    
    res.status(200).json({
      success: true,
      message: 'User updated successfully',
      data: user
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update user role (Admin only)
 */
export const updateUserRole = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { role } = req.body;
    
    // Validate role
    if (!isValidRole(role)) {
      throw new BadRequestError(`Invalid role. Must be one of: member, captain, admin`);
    }
    
    // Prevent anyone from becoming admin through this route
    if (role === 'admin') {
      throw new BadRequestError('Cannot assign admin role. Admin users can only be created through system initialization.');
    }
    
    // Prevent changing own role
    if (req.user._id.toString() === id) {
      throw new BadRequestError('You cannot change your own role');
    }
    
    const user = await updateUserRoleService(id, role);
    
    if (!user) {
      throw new NotFoundError('User not found');
    }
    
    // Emit socket event for real-time sync
    emitToAll('user:roleUpdated', { userId: user._id.toString(), user, newRole: role });
    
    res.status(200).json({
      success: true,
      message: 'User role updated successfully',
      data: user
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Delete current user's own account
 */
export const deleteMe = async (req, res, next) => {
  try {
    const userId = req.user._id.toString();
    const user = await deleteUserService(userId);
    
    if (!user) {
      throw new NotFoundError('User not found');
    }
    
    // Emit socket event for real-time sync
    emitToAll('user:deleted', { userId: user._id.toString() });
    
    res.status(200).json({
      success: true,
      message: 'Account deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Delete a user (Admin only)
 */
export const deleteUser = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { teamTransfers } = req.body; // Format: { "teamId1": "newCaptainId1", "teamId2": "newCaptainId2" }
    
    // Prevent deleting own account (use deleteMe instead)
    if (req.user._id.toString() === id) {
      throw new BadRequestError('You cannot delete your own account using this endpoint. Use DELETE /api/users/me instead.');
    }
    
    const user = await deleteUserService(id, { teamTransfers: teamTransfers || {} });
    
    if (!user) {
      throw new NotFoundError('User not found');
    }
    
    // Emit socket event for real-time sync
    emitToAll('user:deleted', { userId: user._id.toString() });
    
    res.status(200).json({
      success: true,
      message: 'User deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};
