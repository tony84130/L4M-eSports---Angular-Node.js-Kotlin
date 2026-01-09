import express from 'express';
import { authenticate } from '../middlewares/auth.middleware.js';
import { validateTeamRequest } from '../middlewares/validation.middleware.js';
import {
  getAllTeamRequests,
  getTeamRequestsByTeam,
  getTeamRequestById,
  createTeamRequest,
  acceptTeamRequest,
  rejectTeamRequest,
  cancelTeamRequest
} from '../controllers/teamRequest.controller.js';

const router = express.Router();

/**
 * @route   GET /api/team-requests
 * @desc    Get all team requests for the authenticated user (as captain or as requester)
 * @access  Private
 */
router.get('/', authenticate, getAllTeamRequests);

/**
 * @route   GET /api/team-requests/team/:teamId
 * @desc    Get all pending requests for a specific team (Captain only)
 * @access  Private (Captain of the team)
 */
router.get('/team/:teamId', authenticate, getTeamRequestsByTeam);

/**
 * @route   GET /api/team-requests/:id
 * @desc    Get a specific team request by ID
 * @access  Private
 */
router.get('/:id', authenticate, getTeamRequestById);

/**
 * @route   POST /api/team-requests
 * @desc    Send a request to join a team
 * @access  Private
 */
router.post('/', authenticate, validateTeamRequest, createTeamRequest);

/**
 * @route   PUT /api/team-requests/:id/accept
 * @desc    Accept a team request (Captain only)
 * @access  Private (Captain of the team)
 */
router.put('/:id/accept', authenticate, acceptTeamRequest);

/**
 * @route   PUT /api/team-requests/:id/reject
 * @desc    Reject a team request (Captain only)
 * @access  Private (Captain of the team)
 */
router.put('/:id/reject', authenticate, rejectTeamRequest);

/**
 * @route   DELETE /api/team-requests/:id
 * @desc    Cancel a team request (Requester only)
 * @access  Private (Requester)
 */
router.delete('/:id', authenticate, cancelTeamRequest);

export default router;
