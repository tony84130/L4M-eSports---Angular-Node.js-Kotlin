import User from '../models/user.model.js';

/**
 * Get all users with pagination and filters
 */
export const getAllUsers = async (filters) => {
  const { page = 1, limit = 10, role, search } = filters;
  
  const query = {};
  
  if (role) {
    query.role = role;
  }
  
  if (search) {
    query.$or = [
      { email: { $regex: search, $options: 'i' } },
      { firstName: { $regex: search, $options: 'i' } },
      { lastName: { $regex: search, $options: 'i' } },
      { twitchUsername: { $regex: search, $options: 'i' } }
    ];
  }
  
  const skip = (parseInt(page) - 1) * parseInt(limit);
  
  const users = await User.find(query)
    .select('-password')
    .skip(skip)
    .limit(parseInt(limit))
    .sort({ createdAt: -1 });
  
  const total = await User.countDocuments(query);
  
  return {
    users,
    pagination: {
      page: parseInt(page),
      limit: parseInt(limit),
      total,
      pages: Math.ceil(total / parseInt(limit))
    }
  };
};

/**
 * Get user by ID
 */
export const getUserById = async (id) => {
  return await User.findById(id).select('-password');
};

/**
 * Check if user can access another user's profile
 */
export const canAccessUserProfile = (currentUser, targetUserId) => {
  return currentUser.role === 'admin' || currentUser._id.toString() === targetUserId;
};

/**
 * Check if email is taken by another user
 */
export const isEmailTakenByOtherUser = async (email, excludeUserId) => {
  const user = await User.findOne({ 
    email: email.toLowerCase(),
    _id: { $ne: excludeUserId }
  });
  return !!user;
};

/**
 * Check if twitchUsername is taken by another user
 */
export const isTwitchUsernameTakenByOtherUser = async (twitchUsername, excludeUserId) => {
  const user = await User.findOne({ 
    twitchUsername,
    _id: { $ne: excludeUserId }
  });
  return !!user;
};

/**
 * Build update data object from request body (filters allowed fields)
 */
export const buildUpdateData = (body, allowedFields = ['firstName', 'lastName', 'email', 'twitchUsername', 'preferences', 'location', 'avatar']) => {
  const updateData = {};
  allowedFields.forEach(field => {
    if (body[field] !== undefined) {
      updateData[field] = body[field];
    }
  });
  return updateData;
};

/**
 * Update user
 */
export const updateUser = async (userId, updateData) => {
  if (updateData.email) {
    updateData.email = updateData.email.toLowerCase();
  }
  
  const user = await User.findByIdAndUpdate(
    userId,
    { $set: updateData },
    { new: true, runValidators: true }
  ).select('-password');
  
  return user;
};

/**
 * Update user role
 */
export const updateUserRole = async (userId, role) => {
  const user = await User.findByIdAndUpdate(
    userId,
    { role },
    { new: true, runValidators: true }
  ).select('-password');
  
  return user;
};

/**
 * Delete user
 * @param {string} userId - ID de l'utilisateur à supprimer
 * @param {Object} options - Options pour la suppression
 * @param {Object} options.teamTransfers - Map des équipes où transférer le capitaine: { teamId: newCaptainId }
 */
export const deleteUser = async (userId, options = {}) => {
  const { BadRequestError } = await import('../utils/errors.js');
  
  // Vérifier si l'utilisateur est capitaine d'une équipe
  const Team = (await import('../models/team.model.js')).default;
  const teamsAsCaptain = await Team.find({ captain: userId, status: 'active' }).populate('members');
  
  if (teamsAsCaptain.length > 0) {
    const { teamTransfers = {} } = options;
    
    // Pour chaque équipe où l'utilisateur est capitaine
    for (const team of teamsAsCaptain) {
      let newCaptainId = teamTransfers[team._id.toString()];
      
      // Si aucun nouveau capitaine n'est fourni, essayer de transférer automatiquement au premier membre disponible
      if (!newCaptainId) {
        const otherMembers = team.members.filter(m => m._id.toString() !== userId);
        
        if (otherMembers.length === 0) {
          // L'équipe n'a que le capitaine, on empêche la suppression
          throw new BadRequestError(`Cannot delete user: User is captain of team "${team.name}" and it has no other members. Please delete the team first or provide a new captain.`);
        } else {
          // Transférer automatiquement au premier membre disponible
          newCaptainId = otherMembers[0]._id.toString();
        }
      }
      
      // Vérifier que le nouveau capitaine est membre de l'équipe
      const isMember = team.members.some(m => m._id.toString() === newCaptainId);
      if (!isMember) {
        throw new BadRequestError(`Cannot transfer captaincy: User ${newCaptainId} is not a member of team "${team.name}".`);
      }
      
      // Transférer le rôle de capitaine
      team.captain = newCaptainId;
      await team.save();
      
      // Mettre à jour le rôle du nouveau capitaine si nécessaire
      const newCaptain = await User.findById(newCaptainId);
      if (newCaptain && newCaptain.role === 'member') {
        // Vérifier si le nouveau capitaine n'est capitaine d'aucune autre équipe
        const otherTeamsAsCaptain = await Team.countDocuments({
          captain: newCaptainId,
          _id: { $ne: team._id }
        });
        
        if (otherTeamsAsCaptain === 0) {
          // Il n'est capitaine d'aucune autre équipe, changer son rôle en captain
          newCaptain.role = 'captain';
          await newCaptain.save();
        }
      }
    }
  }
  
  // Retirer l'utilisateur des équipes où il est membre (mais pas capitaine)
  await Team.updateMany(
    { members: userId, captain: { $ne: userId } },
    { $pull: { members: userId } }
  );
  
  // Retirer l'utilisateur des demandes d'équipe
  const TeamRequest = (await import('../models/teamRequest.model.js')).default;
  await TeamRequest.deleteMany({ requester: userId });
  
  // Supprimer l'utilisateur
  return await User.findByIdAndDelete(userId);
};

/**
 * Validate role
 */
export const isValidRole = (role) => {
  const validRoles = ['member', 'captain', 'admin'];
  return validRoles.includes(role);
};

