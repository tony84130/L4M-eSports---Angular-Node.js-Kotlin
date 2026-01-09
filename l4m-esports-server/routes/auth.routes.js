import express from 'express';
import { signUp, signIn, signOut } from '../controllers/auth.controller.js';
import { authenticate } from '../middlewares/auth.middleware.js';
import { validateSignUp, validateSignIn } from '../middlewares/validation.middleware.js';

const router = express.Router();

/**
 * @route   POST /api/auth/sign-up
 * @desc    Sign up a new user
 * @access  Public
 */
router.post('/sign-up', validateSignUp, signUp);

/**
 * @route   POST /api/auth/sign-in
 * @desc    Sign in user
 * @access  Public
 */
router.post('/sign-in', validateSignIn, signIn);

/**
 * @route   POST /api/auth/sign-out
 * @desc    Sign out user
 * @access  Private
 */
router.post('/sign-out', authenticate, signOut);

export default router;
