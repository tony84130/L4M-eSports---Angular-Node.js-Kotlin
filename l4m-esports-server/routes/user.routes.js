import express from 'express';
import { authenticate, authorize } from '../middlewares/auth.middleware.js';
import {
  getAllUsers,
  getUserById,
  getMe,
  updateMe,
  deleteMe,
  updateUser,
  updateUserRole,
  deleteUser
} from '../controllers/user.controller.js';

const router = express.Router();

/**
 * @route   GET /api/users
 * @desc    Get all users (Admin only)
 * @access  Private (Admin)
 */
router.get('/', authenticate, authorize('admin'), getAllUsers);

/**
 * @route   GET /api/users/me
 * @desc    Get current authenticated user profile
 * @access  Private
 */
router.get('/me', authenticate, getMe);

/**
 * @route   PUT /api/users/me
 * @desc    Update current user profile
 * @access  Private
 */
router.put('/me', authenticate, updateMe);

/**
 * @route   DELETE /api/users/me
 * @desc    Delete current user's own account
 * @access  Private
 */
router.delete('/me', authenticate, deleteMe);

/**
 * @route   GET /api/users/:id
 * @desc    Get a specific user by ID
 * @access  Private (User can see own profile, Admin can see all)
 */
router.get('/:id', authenticate, getUserById);

/**
 * @route   PUT /api/users/:id
 * @desc    Update a user (Admin only)
 * @access  Private (Admin)
 */
router.put('/:id', authenticate, authorize('admin'), updateUser);

/**
 * @route   PUT /api/users/:id/role
 * @desc    Update user role (Admin only)
 * @access  Private (Admin)
 */
router.put('/:id/role', authenticate, authorize('admin'), updateUserRole);

/**
 * @route   DELETE /api/users/:id
 * @desc    Delete a user (Admin only)
 * @access  Private (Admin)
 */
router.delete('/:id', authenticate, authorize('admin'), deleteUser);

export default router;

