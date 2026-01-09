import express from 'express';
import { authenticate, authorize } from '../middlewares/auth.middleware.js';
import { validateGame } from '../middlewares/validation.middleware.js';
import {
  getAllGames,
  getGameById,
  createGame,
  updateGame,
  deleteGame
} from '../controllers/game.controller.js';

const router = express.Router();

/**
 * @route   GET /api/games
 * @desc    Get all games (active games)
 * @access  Public
 */
router.get('/', getAllGames);

/**
 * @route   GET /api/games/:id
 * @desc    Get a specific game by ID
 * @access  Public
 */
router.get('/:id', getGameById);

/**
 * @route   POST /api/games
 * @desc    Create a new game (Admin only)
 * @access  Private (Admin)
 */
router.post('/', authenticate, authorize('admin'), validateGame, createGame);

/**
 * @route   PUT /api/games/:id
 * @desc    Update a game (Admin only)
 * @access  Private (Admin)
 */
router.put('/:id', authenticate, authorize('admin'), validateGame, updateGame);

/**
 * @route   DELETE /api/games/:id
 * @desc    Delete a game (Admin only)
 * @access  Private (Admin)
 */
router.delete('/:id', authenticate, authorize('admin'), deleteGame);

export default router;
