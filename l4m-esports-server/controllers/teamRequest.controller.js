import * as teamRequestService from '../services/teamRequest.service.js';

/**
 * Get all team requests
 */
export const getAllTeamRequests = async (req, res, next) => {
  try {
    const requests = await teamRequestService.getAllTeamRequests(req.query, req.user._id);
    res.status(200).json({
      success: true,
      data: requests
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get team requests for a specific team (Captain only)
 */
export const getTeamRequestsByTeam = async (req, res, next) => {
  try {
    const { teamId } = req.params;
    const requests = await teamRequestService.getTeamRequestsByTeam(teamId, req.user._id);
    res.status(200).json({
      success: true,
      data: requests
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get team request by ID
 */
export const getTeamRequestById = async (req, res, next) => {
  try {
    const { id } = req.params;
    const request = await teamRequestService.getTeamRequestById(id);
    res.status(200).json({
      success: true,
      data: request
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Create a team request
 */
export const createTeamRequest = async (req, res, next) => {
  try {
    const request = await teamRequestService.createTeamRequest(req.body, req.user._id);
    res.status(201).json({
      success: true,
      message: 'Team request created successfully',
      data: request
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Accept a team request (Captain only)
 */
export const acceptTeamRequest = async (req, res, next) => {
  try {
    const { id } = req.params;
    const request = await teamRequestService.acceptTeamRequest(id, req.user._id);
    res.status(200).json({
      success: true,
      message: 'Team request accepted successfully',
      data: request
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Reject a team request (Captain only)
 */
export const rejectTeamRequest = async (req, res, next) => {
  try {
    const { id } = req.params;
    const request = await teamRequestService.rejectTeamRequest(id, req.user._id);
    res.status(200).json({
      success: true,
      message: 'Team request rejected successfully',
      data: request
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Cancel a team request (Requester only)
 */
export const cancelTeamRequest = async (req, res, next) => {
  try {
    const { id } = req.params;
    await teamRequestService.cancelTeamRequest(id, req.user._id);
    res.status(200).json({
      success: true,
      message: 'Team request cancelled successfully'
    });
  } catch (error) {
    next(error);
  }
};

