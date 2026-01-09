import express from 'express';
import { authenticate as protect } from '../middlewares/auth.middleware.js';
import * as matchController from '../controllers/match.controller.js';

const router = express.Router();

// === ROUTES PUBLIQUES ===

/**
 * @route   GET /api/matches
 * @desc    Get all matches (with optional filters: event, status, team)
 * @access  Public
 */
router.get('/', matchController.getAllMatches);

/**
 * @route   GET /api/matches/event/:eventId
 * @desc    Get all matches for a specific event
 * @access  Public
 * @note    Doit être AVANT /:id pour éviter les conflits de routes
 */
router.get('/event/:eventId', matchController.getMatchesByEvent);

/**
 * @route   GET /api/matches/team/:teamId
 * @desc    Get all matches for a specific team
 * @access  Public
 * @note    Doit être AVANT /:id pour éviter les conflits de routes
 */
router.get('/team/:teamId', matchController.getMatchesByTeam);

/**
 * @route   GET /api/matches/:id
 * @desc    Get a specific match by ID
 * @access  Public
 */
router.get('/:id', matchController.getMatchById);

// === ROUTES PRIVÉES ===

/**
 * @route   PUT /api/matches/:id/status
 * @desc    Update match status (Admin or Captain of participating team)
 * @access  Private (Admin or Captain)
 */
router.put('/:id/status', protect, matchController.updateMatchStatus);

/**
 * @route   PUT /api/matches/:id/score
 * @desc    Update match score (Admin or Captain of participating team)
 * @access  Private (Admin or Captain)
 */
router.put('/:id/score', protect, matchController.updateMatchScore);

/**
 * @route   POST /api/matches/:id/validate
 * @desc    Validate match result (Admin or Captain of participating team)
 * @access  Private (Admin or Captain)
 */
router.post('/:id/validate', protect, matchController.validateMatchResult);

/**
 * @route   PUT /api/matches/:id
 * @desc    Update match (e.g., scheduled time) (Admin only)
 * @access  Private (Admin)
 */
router.put('/:id', protect, matchController.updateMatch);

export default router;

