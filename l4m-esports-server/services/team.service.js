import mongoose from 'mongoose';
import Team from '../models/team.model.js';
import User from '../models/user.model.js';
import Game from '../models/game.model.js';
import TeamRequest from '../models/teamRequest.model.js';
import { NotFoundError, BadRequestError, ForbiddenError } from '../utils/errors.js';
import { createNotification } from './notification.service.js';

/**
 * Get all teams with filters
 */
export const getAllTeams = async (filters = {}) => {
  const { game, status, search } = filters;
  
  const query = {};
  
  if (game) {
    query.game = game;
  }
  
  if (status) {
    query.status = status;
  }
  
  if (search) {
    query.$or = [
      { name: { $regex: search, $options: 'i' } },
      { description: { $regex: search, $options: 'i' } }
    ];
  }
  
  const teams = await Team.find(query)
    .populate('game', 'name logo')
    .populate('captain', 'firstName lastName email avatar gamertag')
    .populate('members', 'firstName lastName email avatar gamertag')
    .sort({ createdAt: -1 });
  
  return teams;
};

/**
 * Get team by ID
 */
export const getTeamById = async (id) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(id)) {
    throw new BadRequestError(`ID d'équipe invalide : ${id}`);
  }
  
  const team = await Team.findById(id)
    .populate('game', 'name logo')
    .populate('captain', 'firstName lastName email avatar gamertag')
    .populate('members', 'firstName lastName email avatar gamertag');
  
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  return team;
};

/**
 * Create a new team
 */
export const createTeam = async (teamData, captainId) => {
  // Get the user who wants to create the team
  const user = await User.findById(captainId);
  if (!user) {
    throw new NotFoundError('Utilisateur introuvable');
  }
  
  // Only members and captains can create teams
  if (user.role !== 'member' && user.role !== 'captain') {
    throw new ForbiddenError('Seuls les membres et les capitaines peuvent créer des équipes');
  }
  
  // Verify game exists
  const game = await Game.findById(teamData.game);
  if (!game) {
    throw new NotFoundError('Jeu introuvable');
  }
  
  if (!game.isActive) {
    throw new BadRequestError('Impossible de créer une équipe pour un jeu inactif');
  }
  
  // Check if user is already captain of another active team for this specific game
  const existingTeamAsCaptain = await Team.findOne({
    game: teamData.game,
    captain: captainId,
    status: 'active'
  });
  
  if (existingTeamAsCaptain) {
    throw new BadRequestError('Vous êtes déjà capitaine d\'une équipe active pour ce jeu. Vous ne pouvez être capitaine que d\'une équipe par jeu.');
  }
  
  // Check if user is already a member of another active team for this specific game
  const isAlreadyMember = await isUserMemberOfGameTeam(captainId, teamData.game);
  if (isAlreadyMember) {
    throw new BadRequestError('Vous êtes déjà membre d\'une équipe active pour ce jeu. Vous ne pouvez être membre que d\'une équipe par jeu.');
  }
  
  // Create the team
  const team = await Team.create({
    ...teamData,
    captain: captainId,
    members: [captainId] // Captain is automatically added to members
  });
  
  // If user is a member, promote them to captain role
  if (user.role === 'member') {
    user.role = 'captain';
    await user.save();
  }
  
  return await getTeamById(team._id);
};

/**
 * Update team
 */
export const updateTeam = async (id, updateData, userId) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(id)) {
    throw new BadRequestError(`ID d'équipe invalide : ${id}`);
  }
  
  const team = await Team.findById(id);
  
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  // Check if user is the captain
  if (team.captain.toString() !== userId.toString()) {
    throw new ForbiddenError('Seul le capitaine de l\'équipe peut modifier l\'équipe');
  }
  
  // If game is being updated, verify it exists
  if (updateData.game) {
    const game = await Game.findById(updateData.game);
    if (!game) {
      throw new NotFoundError('Jeu introuvable');
    }
  }
  
  const previousName = team.name;
  const previousLogo = team.logo;
  const previousDescription = team.description;
  
  const updatedTeam = await Team.findByIdAndUpdate(
    id,
    { $set: updateData },
    { new: true, runValidators: true }
  );
  
  // Envoyer des notifications si des changements importants ont été faits
  try {
    const changes = [];
    if (updateData.name && updateData.name !== previousName) {
      changes.push(`nom: "${previousName}" → "${updateData.name}"`);
    }
    if (updateData.logo && updateData.logo !== previousLogo) {
      changes.push('logo mis à jour');
    }
    if (updateData.description && updateData.description !== previousDescription) {
      changes.push('description mise à jour');
    }
    
    if (changes.length > 0) {
      const { notifyTeamMembers } = await import('./notification.service.js');
      await notifyTeamMembers(id, {
        type: 'team_updated',
        title: 'Équipe mise à jour',
        message: `Votre équipe "${updateData.name || previousName}" a été mise à jour : ${changes.join(', ')}`,
        relatedEntity: {
          entityType: 'team',
          entityId: id
        }
      });
    }
  } catch (notificationError) {
    console.warn('⚠️  Erreur lors de la création des notifications de mise à jour d\'équipe:', notificationError.message);
  }
  
  return await getTeamById(updatedTeam._id);
};

/**
 * Delete team
 */
export const deleteTeam = async (id, userId) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(id)) {
    throw new BadRequestError(`ID d'équipe invalide : ${id}`);
  }
  
  const team = await Team.findById(id);
  
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  // Get user to check role
  const user = await User.findById(userId);
  if (!user) {
    throw new NotFoundError('Utilisateur introuvable');
  }
  
  // Check if user is the captain OR an admin
  const isCaptain = team.captain.toString() === userId.toString();
  const isAdmin = user.role === 'admin';
  
  if (!isCaptain && !isAdmin) {
    throw new ForbiddenError('Only the team captain or an admin can delete the team');
  }
  
  // Get the captain user before deleting the team (for role update if needed)
  const captainId = team.captain;
  const captain = await User.findById(captainId);
  if (!captain) {
    throw new NotFoundError('Captain user not found');
  }
  
  // Delete the team
  await Team.findByIdAndDelete(id);
  
  if (isAdmin) {
    // If admin deleted the team, update roles for all members
    // Get all members of the deleted team (including captain)
    const memberIds = team.members.map(member => member.toString());
    
    // Process each member
    for (const memberId of memberIds) {
      const member = await User.findById(memberId);
      if (!member) continue;
      
      // Skip if user is already a member (no need to change)
      if (member.role === 'member') continue;
      
      // Check if this member is still captain of any other active team
      const otherTeamsAsCaptain = await Team.countDocuments({
        captain: memberId,
        status: 'active'
      });
      
      // If user is no longer captain of any team and their role is captain, change to member
      if (otherTeamsAsCaptain === 0 && member.role === 'captain') {
        member.role = 'member';
        await member.save();
      }
    }
  } else if (isCaptain) {
    // If captain deleted their own team, only update captain's role
    // Check if user is still captain of any other active teams
    const otherTeamsAsCaptain = await Team.countDocuments({
      captain: userId,
      status: 'active'
    });
    
    // If user is no longer captain of any team and their role is captain, change to member
    if (otherTeamsAsCaptain === 0 && captain.role === 'captain') {
      captain.role = 'member';
      await captain.save();
    }
  }
  
  return true;
};

/**
 * Invite a user to join the team
 */
export const inviteUserToTeam = async (teamId, userId, invitedUserId) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(teamId)) {
    throw new BadRequestError(`Invalid team ID: ${teamId}`);
  }
  
  const team = await Team.findById(teamId);
  
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  // Check if user is the captain
  if (team.captain.toString() !== userId.toString()) {
    throw new ForbiddenError('Only the team captain can invite members');
  }
  
  // Check if user exists
  const invitedUser = await User.findById(invitedUserId);
  if (!invitedUser) {
    throw new NotFoundError('Utilisateur introuvable');
  }
  
  // Admins cannot be invited to teams
  if (invitedUser.role === 'admin') {
    throw new BadRequestError('Admins cannot be invited to join teams');
  }
  
  // Check if trying to invite the captain
  if (team.captain.toString() === invitedUserId) {
    throw new BadRequestError('Cannot invite the team captain (already a member)');
  }
  
  // Check if user is already a member
  if (team.members.some(member => member.toString() === invitedUserId)) {
    throw new BadRequestError('User is already a member of this team');
  }
  
  // Check if team has reached max members
  if (team.members.length >= team.maxMembers) {
    throw new BadRequestError('Team has reached maximum number of members');
  }
  
  // Check if invited user is already a member of another active team for this game
  const isAlreadyMember = await isUserMemberOfGameTeam(invitedUserId, team.game.toString(), teamId);
  if (isAlreadyMember) {
    throw new BadRequestError('This user is already a member of another active team for this game. A user can only be a member of one team per game.');
  }
  
  // Cancel any pending requests from this user for this team
  await TeamRequest.updateMany(
    { team: teamId, user: invitedUserId, status: 'pending' },
    { 
      status: 'rejected', 
      reviewedBy: userId, 
      reviewedAt: new Date() 
    }
  );
  
  // Add user to members
  team.members.push(invitedUserId);
  await team.save();
  
  // Get team details for notification
  const teamPopulated = await Team.findById(teamId).populate('captain', 'firstName lastName gamertag');
  const captain = await User.findById(userId);
  
  // Create notification for the invited user (invitation acceptée automatiquement)
  await createNotification({
    user: invitedUserId,
    type: 'team_invitation_accepted',
    title: 'Invitation acceptée',
    message: `Vous avez rejoint l'équipe "${teamPopulated.name}" sur invitation de ${captain.firstName} ${captain.lastName} (${captain.gamertag})`,
    relatedEntity: {
      entityType: 'team',
      entityId: teamId
    }
  });
  
  // Notifier tous les autres membres de l'équipe qu'un nouveau membre a rejoint
  try {
    const { notifyTeamMembers } = await import('./notification.service.js');
    // Exclure le nouveau membre de la notification
    const otherMembers = [team.captain, ...team.members.filter(m => m.toString() !== invitedUserId.toString())];
    const uniqueOtherMembers = Array.from(new Map(otherMembers.map(m => [m.toString(), m])).values());
    
    for (const memberId of uniqueOtherMembers) {
      await createNotification({
        user: memberId,
        type: 'team_member_joined',
        title: 'Nouveau membre rejoint l\'équipe',
        message: `${invitedUser.firstName} ${invitedUser.lastName} (${invitedUser.gamertag}) a rejoint votre équipe "${teamPopulated.name}"`,
        relatedEntity: {
          entityType: 'team',
          entityId: teamId
        }
      });
    }
  } catch (notificationError) {
    console.warn('⚠️  Erreur lors de la création des notifications de nouveau membre:', notificationError.message);
  }
  
  return await getTeamById(teamId);
};

/**
 * Remove a member from the team
 */
export const removeMemberFromTeam = async (teamId, memberId, userId) => {
  // Validate ObjectId format
  if (!mongoose.Types.ObjectId.isValid(teamId)) {
    throw new BadRequestError(`Invalid team ID: ${teamId}`);
  }
  
  const team = await Team.findById(teamId);
  
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  // Check if user is the captain
  if (team.captain.toString() !== userId.toString()) {
    throw new ForbiddenError('Only the team captain can remove members');
  }
  
  // Check if member exists in team
  if (!team.members.some(member => member.toString() === memberId)) {
    throw new NotFoundError('Member not found in this team');
  }
  
  // Cannot remove the captain
  if (team.captain.toString() === memberId) {
    throw new BadRequestError('Cannot remove the team captain');
  }
  
  // Vérifier si le membre participe à un événement en cours
  const EventRegistration = (await import('../models/eventRegistration.model.js')).default;
  const Event = (await import('../models/event.model.js')).default;
  
  const activeRegistrations = await EventRegistration.find({
    team: teamId,
    participatingMembers: memberId,
    status: { $in: ['ACCEPTED', 'PENDING'] }
  }).populate('event', 'status');
  
  const inProgressEvent = activeRegistrations.find(reg => reg.event.status === 'in_progress');
  if (inProgressEvent) {
    throw new BadRequestError('Impossible de retirer un membre qui participe à un événement en cours');
  }
  
  // Get member and team details for notification
  const removedMember = await User.findById(memberId);
  const captain = await User.findById(userId);
  const teamPopulated = await Team.findById(teamId);
  
  // Remove member
  team.members = team.members.filter(
    member => member.toString() !== memberId
  );
  await team.save();
  
  // Create notification for the removed member first
  await createNotification({
    user: memberId,
    type: 'team_member_removed',
    title: 'Retrait d\'une équipe',
    message: `Vous avez été retiré de l'équipe "${teamPopulated.name}" par ${captain.firstName} ${captain.lastName} (${captain.gamertag})`,
    relatedEntity: {
      entityType: 'team',
      entityId: teamId
    }
  });
  
  // Get all remaining members (including captain) to notify them
  const allMembersToNotify = [team.captain, ...team.members.map(m => m.toString())];
  
  // Create notifications for all remaining team members (captain + remaining members)
  const notificationPromises = allMembersToNotify.map(remainingMemberId => 
    createNotification({
      user: remainingMemberId,
      type: 'team_member_removed',
      title: 'Un membre a été retiré de l\'équipe',
      message: `${removedMember.firstName} ${removedMember.lastName} (${removedMember.gamertag}) a été retiré de l'équipe "${teamPopulated.name}" par ${captain.firstName} ${captain.lastName} (${captain.gamertag})`,
      relatedEntity: {
        entityType: 'team',
        entityId: teamId
      }
    })
  );
  
  await Promise.all(notificationPromises);
  
  return await getTeamById(teamId);
};

/**
 * Check if user is captain of team
 */
export const isTeamCaptain = async (teamId, userId) => {
  const team = await Team.findById(teamId);
  if (!team) {
    return false;
  }
  return team.captain.toString() === userId.toString();
};

/**
 * Transfer captain role to another member
 */
export const transferCaptain = async (teamId, newCaptainId, currentCaptainId) => {
  const team = await Team.findById(teamId);
  
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  // Check if user is the current captain
  if (team.captain.toString() !== currentCaptainId.toString()) {
    throw new ForbiddenError('Only the current captain can transfer captaincy');
  }
  
  // Check if new captain is a member of the team
  if (!team.members.some(member => member.toString() === newCaptainId.toString())) {
    throw new BadRequestError('New captain must be a member of the team');
  }
  
  // Check if trying to transfer to the same person
  if (team.captain.toString() === newCaptainId.toString()) {
    throw new BadRequestError('Cannot transfer captaincy to yourself');
  }
  
  // Get users
  const currentCaptain = await User.findById(currentCaptainId);
  const newCaptain = await User.findById(newCaptainId);
  
  if (!currentCaptain || !newCaptain) {
    throw new NotFoundError('Utilisateur introuvable');
  }
  
  // Transfer captaincy
  team.captain = newCaptainId;
  await team.save();
  
  // Update roles if needed
  // If current captain is only captain of this team, change role to member
  const otherTeamsAsCaptain = await Team.countDocuments({
    captain: currentCaptainId,
    status: 'active'
  });
  
  if (otherTeamsAsCaptain === 0 && currentCaptain.role === 'captain') {
    currentCaptain.role = 'member';
    await currentCaptain.save();
  }
  
  // Promote new captain if they are a member
  if (newCaptain.role === 'member') {
    newCaptain.role = 'captain';
    await newCaptain.save();
  }
  
  // Create notifications
  await createNotification({
    user: newCaptainId,
    type: 'team_captain_transferred',
    title: 'Vous êtes maintenant capitaine',
    message: `Vous êtes maintenant le capitaine de l'équipe "${team.name}"`,
    relatedEntity: {
      entityType: 'team',
      entityId: teamId
    }
  });
  
  return await getTeamById(teamId);
};

/**
 * Leave a team (member can leave, captain must transfer first)
 */
export const leaveTeam = async (teamId, userId) => {
  const team = await Team.findById(teamId);
  
  if (!team) {
    throw new NotFoundError('Équipe introuvable');
  }
  
  // Check if user is a member of the team
  if (!team.members.some(member => member.toString() === userId.toString())) {
    throw new BadRequestError('You are not a member of this team');
  }
  
  // If user is the captain, they cannot leave without transferring
  if (team.captain.toString() === userId.toString()) {
    throw new BadRequestError('Captain cannot leave the team. Please transfer captaincy to another member first.');
  }
  
  // Vérifier si l'utilisateur participe à un événement en cours
  const EventRegistration = (await import('../models/eventRegistration.model.js')).default;
  const Event = (await import('../models/event.model.js')).default;
  
  const activeRegistrations = await EventRegistration.find({
    team: teamId,
    participatingMembers: userId,
    status: { $in: ['ACCEPTED', 'PENDING'] }
  }).populate('event', 'status');
  
  const inProgressEvent = activeRegistrations.find(reg => reg.event.status === 'in_progress');
  if (inProgressEvent) {
    throw new BadRequestError('Impossible de quitter l\'équipe si vous participez à un événement en cours');
  }
  
  // Get user and team details for notification
  const leavingMember = await User.findById(userId);
  const teamPopulated = await Team.findById(teamId);
  
  // Remove member from team
  team.members = team.members.filter(
    member => member.toString() !== userId
  );
  await team.save();
  
  // Get all remaining members (including captain) to notify them
  const allMembersToNotify = [team.captain, ...team.members.map(m => m.toString())];
  
  // Create notifications for all team members (captain + remaining members)
  const notificationPromises = allMembersToNotify.map(memberId => 
    createNotification({
      user: memberId,
      type: 'team_member_left',
      title: 'Un membre a quitté l\'équipe',
      message: `${leavingMember.firstName} ${leavingMember.lastName} (${leavingMember.gamertag}) a quitté l'équipe "${teamPopulated.name}"`,
      relatedEntity: {
        entityType: 'team',
        entityId: teamId
      }
    })
  );
  
  await Promise.all(notificationPromises);
  
  return await getTeamById(teamId);
};

/**
 * Check if user is already a member of an active team for a specific game
 * @param {string} userId - User ID to check
 * @param {string} gameId - Game ID to check
 * @param {string} excludeTeamId - Optional team ID to exclude from check (e.g., current team)
 * @returns {Promise<boolean>} - True if user is already a member of a team for this game
 */
export const isUserMemberOfGameTeam = async (userId, gameId, excludeTeamId = null) => {
  const query = {
    game: gameId,
    members: userId,
    status: 'active'
  };
  
  if (excludeTeamId) {
    query._id = { $ne: excludeTeamId };
  }
  
  const existingTeam = await Team.findOne(query);
  return !!existingTeam;
};

