import * as teamService from '../services/team.service.js';
import { BadRequestError } from '../utils/errors.js';
import { emitToAll } from '../services/socket.service.js';

/**
 * Get all teams
 */
export const getAllTeams = async (req, res, next) => {
  try {
    const teams = await teamService.getAllTeams(req.query);
    res.status(200).json({
      success: true,
      data: teams
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get team by ID
 */
export const getTeamById = async (req, res, next) => {
  try {
    const { id } = req.params;
    const team = await teamService.getTeamById(id);
    res.status(200).json({
      success: true,
      data: team
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Create a new team
 */
export const createTeam = async (req, res, next) => {
  try {
    const team = await teamService.createTeam(req.body, req.user._id);
    // Emit socket event for real-time sync
    emitToAll('team:created', { team });
    res.status(201).json({
      success: true,
      message: 'Team created successfully',
      data: team
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update team (Captain only)
 */
export const updateTeam = async (req, res, next) => {
  try {
    const { id } = req.params;
    const team = await teamService.updateTeam(id, req.body, req.user._id);
    // Emit socket event for real-time sync
    emitToAll('team:updated', { teamId: id, team });
    res.status(200).json({
      success: true,
      message: 'Team updated successfully',
      data: team
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Delete team (Captain only)
 */
export const deleteTeam = async (req, res, next) => {
  try {
    const { id } = req.params;
    await teamService.deleteTeam(id, req.user._id);
    // Emit socket event for real-time sync
    emitToAll('team:deleted', { teamId: id });
    res.status(200).json({
      success: true,
      message: 'Team deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Invite a user to join the team (Captain only)
 */
export const inviteUser = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { userId, gamertag } = req.body;
    
    let targetUserId = userId;
    
    // If gamertag is provided, find user by gamertag
    if (gamertag) {
      const User = (await import('../models/user.model.js')).default;
      const user = await User.findOne({ gamertag: gamertag.trim() });
      if (!user) {
        return res.status(404).json({
          success: false,
          error: { message: 'User with this gamertag not found' }
        });
      }
      targetUserId = user._id.toString();
    }
    
    const team = await teamService.inviteUserToTeam(id, req.user._id, targetUserId);
    res.status(200).json({
      success: true,
      message: 'User invited successfully',
      data: team
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Remove a member from the team (Captain only)
 */
export const removeMember = async (req, res, next) => {
  try {
    const { id, userId } = req.params;
    const team = await teamService.removeMemberFromTeam(id, userId, req.user._id);
    res.status(200).json({
      success: true,
      message: 'Member removed successfully',
      data: team
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Transfer captaincy to another member (Captain only)
 */
export const transferCaptain = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { newCaptainId } = req.body;
    
    if (!newCaptainId) {
      return res.status(400).json({
        success: false,
        error: { message: 'newCaptainId is required' }
      });
    }
    
    const team = await teamService.transferCaptain(id, newCaptainId, req.user._id);
    // Emit socket event for real-time sync
    emitToAll('team:captainTransferred', { teamId: id, team, newCaptainId });
    res.status(200).json({
      success: true,
      message: 'Captaincy transferred successfully',
      data: team
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Leave a team (Member can leave, captain must transfer first)
 */
export const leaveTeam = async (req, res, next) => {
  try {
    const { id } = req.params;
    const team = await teamService.leaveTeam(id, req.user._id);
    res.status(200).json({
      success: true,
      message: 'You have left the team successfully',
      data: team
    });
  } catch (error) {
    next(error);
  }
};

