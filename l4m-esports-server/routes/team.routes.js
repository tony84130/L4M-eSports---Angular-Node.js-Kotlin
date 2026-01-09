import express from 'express';
import { authenticate } from '../middlewares/auth.middleware.js';
import { validateTeam, validateTeamUpdate, validateInviteUser } from '../middlewares/validation.middleware.js';
import {
  getAllTeams,
  getTeamById,
  createTeam,
  updateTeam,
  deleteTeam,
  inviteUser,
  removeMember,
  transferCaptain,
  leaveTeam
} from '../controllers/team.controller.js';

const router = express.Router();

/**
 * @route   GET /api/teams
 * @desc    Get all teams (with optional filters: game, status)
 * @access  Public
 */
router.get('/', getAllTeams);

/**
 * @route   GET /api/teams/:id
 * @desc    Get a specific team by ID
 * @access  Public
 */
router.get('/:id', getTeamById);

/**
 * @route   POST /api/teams
 * @desc    Create a new team (user becomes captain)
 * @access  Private
 */
router.post('/', authenticate, validateTeam, createTeam);

/**
 * @route   PUT /api/teams/:id
 * @desc    Update team information (Captain only)
 * @access  Private (Captain of the team)
 */
router.put('/:id', authenticate, validateTeamUpdate, updateTeam);

/**
 * @route   DELETE /api/teams/:id
 * @desc    Delete a team (Captain only)
 * @access  Private (Captain of the team)
 */
router.delete('/:id', authenticate, deleteTeam);

/**
 * @route   POST /api/teams/:id/invite
 * @desc    Invite a user to join the team (Captain only)
 * @access  Private (Captain of the team)
 */
router.post('/:id/invite', authenticate, validateInviteUser, inviteUser);

/**
 * @route   DELETE /api/teams/:id/members/:userId
 * @desc    Remove a member from the team (Captain only)
 * @access  Private (Captain of the team)
 */
router.delete('/:id/members/:userId', authenticate, removeMember);

/**
 * @route   POST /api/teams/:id/transfer-captain
 * @desc    Transfer captaincy to another member (Captain only)
 * @access  Private (Captain of the team)
 */
router.post('/:id/transfer-captain', authenticate, transferCaptain);

/**
 * @route   POST /api/teams/:id/leave
 * @desc    Leave a team (Member can leave, captain must transfer first)
 * @access  Private
 */
router.post('/:id/leave', authenticate, leaveTeam);

export default router;
