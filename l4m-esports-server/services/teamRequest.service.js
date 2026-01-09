import mongoose from 'mongoose';
import TeamRequest from '../models/teamRequest.model.js';
import Team from '../models/team.model.js';
import User from '../models/user.model.js';
import { NotFoundError, BadRequestError, ForbiddenError } from '../utils/errors.js';
import { createNotification } from './notification.service.js';
import { isUserMemberOfGameTeam } from './team.service.js';

/**
 * Get all team requests with filters
 */
export const getAllTeamRequests = async (filters = {}, userId) => {
  const { team, user, status } = filters;
  
  const query = {};
  
  if (team) {
    query.team = team;
  }
  
  if (user) {
    query.user = user;
  } else {
    // If no user filter, show requests for user's teams (as captain) or user's requests
    const userTeams = await Team.find({ captain: userId }).select('_id');
    const teamIds = userTeams.map(t => t._id);
    
    query.$or = [
      { team: { $in: teamIds } }, // Requests for teams where user is captain
      { user: userId } // Requests made by user
    ];
  }
  
  if (status) {
    query.status = status;
  }
  
  const requests = await TeamRequest.find(query)
    .populate('team', 'name game logo')
    .populate('user', 'firstName lastName email avatar')
    .populate('reviewedBy', 'firstName lastName email')
    .sort({ createdAt: -1 });
  
  return requests;
};

/**
 * Get team requests for a specific team (Captain only)
 */
export const getTeamRequestsByTeam = async (teamId, captainId) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(teamId)) {
    throw new BadRequestError(`ID d'équipe invalide : ${teamId}`);
  }
  
  const team = await Team.findById(teamId);
  
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  if (team.captain.toString() !== captainId.toString()) {
    throw new ForbiddenError('Seul le capitaine de l\'équipe peut voir les demandes d\'équipe');
  }
  
  const requests = await TeamRequest.find({ team: teamId })
    .populate('user', 'firstName lastName email avatar gamertag')
    .populate('reviewedBy', 'firstName lastName email gamertag')
    .sort({ createdAt: -1 });
  
  return requests;
};

/**
 * Get team request by ID
 */
export const getTeamRequestById = async (id) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(id)) {
    throw new BadRequestError(`ID de demande d'équipe invalide : ${id}`);
  }
  
  const request = await TeamRequest.findById(id)
    .populate('team', 'name game logo captain')
    .populate('user', 'firstName lastName email avatar gamertag')
    .populate('reviewedBy', 'firstName lastName email gamertag');
  
  if (!request) {
    throw new NotFoundError('Demande d\'équipe introuvable');
  }
  
  return request;
};

/**
 * Create a team request
 */
export const createTeamRequest = async (requestData, userId) => {
  const { team: teamId, message } = requestData;
  
  // Get user to check role
  const User = (await import('../models/user.model.js')).default;
  const user = await User.findById(userId);
  if (!user) {
    throw new NotFoundError('Utilisateur introuvable');
  }
  
  // Admins cannot join teams
  if (user.role === 'admin') {
    throw new ForbiddenError('Les administrateurs ne peuvent pas rejoindre d\'équipes');
  }
  
  // Verify team exists
  const team = await Team.findById(teamId);
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  // Check if team is active
  if (team.status !== 'active') {
    throw new BadRequestError('Impossible de demander à rejoindre une équipe inactive');
  }
  
  // Check if user is already a member
  if (team.members.some(member => member.toString() === userId.toString())) {
    throw new BadRequestError('Vous êtes déjà membre de cette équipe');
  }
  
  // Check if team has reached max members
  if (team.members.length >= team.maxMembers) {
    throw new BadRequestError('L\'équipe a atteint le nombre maximum de membres');
  }
  
  // Check if user is already a member of another active team for this game
  const isAlreadyMember = await isUserMemberOfGameTeam(userId, team.game.toString(), teamId);
  if (isAlreadyMember) {
    throw new BadRequestError('Vous êtes déjà membre d\'une autre équipe active pour ce jeu. Vous ne pouvez être membre que d\'une équipe par jeu.');
  }
  
  // Check if there's already a pending request
  const existingRequest = await TeamRequest.findOne({
    team: teamId,
    user: userId,
    status: 'pending'
  });
  
  if (existingRequest) {
    throw new BadRequestError('Vous avez déjà une demande en attente pour cette équipe');
  }
  
  const request = await TeamRequest.create({
    team: teamId,
    user: userId,
    message
  });
  
  // Get team details for notification (populate captain)
  const teamPopulated = await Team.findById(teamId).populate('captain', 'firstName lastName gamertag');
  const requester = await User.findById(userId);
  
  // Create notification for the team captain
  await createNotification({
    user: teamPopulated.captain._id,
    type: 'team_request',
    title: 'Nouvelle demande d\'équipe',
    message: `${requester.firstName} ${requester.lastName} (${requester.gamertag}) souhaite rejoindre votre équipe "${teamPopulated.name}"${message ? ` : "${message}"` : ''}`,
    relatedEntity: {
      entityType: 'team_request',
      entityId: request._id
    }
  });
  
  return await getTeamRequestById(request._id);
};

/**
 * Accept a team request
 */
export const acceptTeamRequest = async (requestId, captainId) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(requestId)) {
    throw new BadRequestError(`ID de demande d'équipe invalide : ${requestId}`);
  }
  
  const request = await TeamRequest.findById(requestId)
    .populate('team');
  
  if (!request) {
    throw new NotFoundError('Demande d\'équipe introuvable');
  }
  
  if (request.status !== 'pending') {
    throw new BadRequestError('Cette demande a déjà été traitée');
  }
  
  const team = await Team.findById(request.team._id);
  
  // Check if user is the captain
  if (team.captain.toString() !== captainId.toString()) {
    throw new ForbiddenError('Seul le capitaine de l\'équipe peut accepter les demandes');
  }
  
  // Check if team still has space
  if (team.members.length >= team.maxMembers) {
    throw new BadRequestError('L\'équipe a atteint le nombre maximum de membres');
  }
  
  // Get the requester user to check role
  const User = (await import('../models/user.model.js')).default;
  const requester = await User.findById(request.user);
  if (!requester) {
    throw new NotFoundError('Utilisateur demandeur introuvable');
  }
  
  // Admins cannot join teams (defense in depth - even if a request exists)
  if (requester.role === 'admin') {
    throw new BadRequestError('Les administrateurs ne peuvent pas rejoindre d\'équipes');
  }
  
  // Check if user is already a member
  if (team.members.some(member => member.toString() === request.user.toString())) {
    throw new BadRequestError('L\'utilisateur est déjà membre de cette équipe');
  }
  
  // Check if user is already a member of another active team for this game
  const isAlreadyMember = await isUserMemberOfGameTeam(request.user.toString(), team.game.toString(), team._id.toString());
  if (isAlreadyMember) {
    throw new BadRequestError('Cet utilisateur est déjà membre d\'une autre équipe active pour ce jeu. Un utilisateur ne peut être membre que d\'une équipe par jeu.');
  }
  
  // Add user to team members
  team.members.push(request.user);
  await team.save();
  
  // Update request status
  request.status = 'accepted';
  request.reviewedBy = captainId;
  request.reviewedAt = new Date();
  await request.save();
  
  // Get team details for notification
  const teamPopulated = await Team.findById(request.team._id)
    .populate('captain', 'firstName lastName gamertag')
    .populate('members', '_id firstName lastName gamertag');
  
  // Create notification for the requester
  await createNotification({
    user: request.user,
    type: 'team_request_accepted',
    title: 'Demande d\'équipe acceptée',
    message: `Votre demande pour rejoindre l'équipe "${teamPopulated.name}" a été acceptée !`,
    relatedEntity: {
      entityType: 'team_request',
      entityId: requestId
    }
  });
  
  // Notifier tous les membres de l'équipe qu'un nouveau membre a rejoint
  try {
    const { notifyTeamMembers } = await import('./notification.service.js');
    await notifyTeamMembers(team._id, {
      type: 'team_member_joined',
      title: 'Nouveau membre rejoint l\'équipe',
      message: `${requester.firstName} ${requester.lastName} (${requester.gamertag}) a rejoint votre équipe "${teamPopulated.name}"`,
      relatedEntity: {
        entityType: 'team',
        entityId: team._id
      }
    });
  } catch (notificationError) {
    console.warn('⚠️  Erreur lors de la création des notifications de nouveau membre:', notificationError.message);
  }
  
  return await getTeamRequestById(requestId);
};

/**
 * Reject a team request
 */
export const rejectTeamRequest = async (requestId, captainId) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(requestId)) {
    throw new BadRequestError(`ID de demande d'équipe invalide : ${requestId}`);
  }
  
  const request = await TeamRequest.findById(requestId)
    .populate('team');
  
  if (!request) {
    throw new NotFoundError('Demande d\'équipe introuvable');
  }
  
  if (request.status !== 'pending') {
    throw new BadRequestError('Cette demande a déjà été traitée');
  }
  
  const team = await Team.findById(request.team._id);
  
  // Check if user is the captain
  if (team.captain.toString() !== captainId.toString()) {
    throw new ForbiddenError('Seul le capitaine de l\'équipe peut rejeter les demandes');
  }
  
  // Update request status
  request.status = 'rejected';
  request.reviewedBy = captainId;
  request.reviewedAt = new Date();
  await request.save();
  
  // Get team details for notification
  const teamPopulated = await Team.findById(request.team._id).populate('captain', 'firstName lastName gamertag');
  
  // Create notification for the requester
  await createNotification({
    user: request.user,
    type: 'team_request_rejected',
    title: 'Demande d\'équipe rejetée',
    message: `Votre demande pour rejoindre l'équipe "${teamPopulated.name}" a été rejetée.`,
    relatedEntity: {
      entityType: 'team_request',
      entityId: requestId
    }
  });
  
  return await getTeamRequestById(requestId);
};

/**
 * Cancel a team request (by requester)
 */
export const cancelTeamRequest = async (requestId, userId) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(requestId)) {
    throw new BadRequestError(`ID de demande d'équipe invalide : ${requestId}`);
  }
  
  const request = await TeamRequest.findById(requestId);
  
  if (!request) {
    throw new NotFoundError('Demande d\'équipe introuvable');
  }
  
  // Check if user is the requester
  if (request.user.toString() !== userId.toString()) {
    throw new ForbiddenError('Vous ne pouvez annuler que vos propres demandes');
  }
  
  if (request.status !== 'pending') {
    throw new BadRequestError('Cannot cancel a request that has already been processed');
  }
  
  await TeamRequest.findByIdAndDelete(requestId);
  return true;
};

