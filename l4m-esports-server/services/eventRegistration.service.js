import EventRegistration from '../models/eventRegistration.model.js';
import Event from '../models/event.model.js';
import Team from '../models/team.model.js';
import { NotFoundError, BadRequestError, ForbiddenError } from '../utils/errors.js';
import { createNotification } from './notification.service.js';

/**
 * Convertit le format d'événement en nombre de joueurs requis
 * Ex: "5v5" -> 5, "3v3" -> 3, "BATTLE_ROYALE" -> 100 (ou autre selon le besoin)
 */
const getRequiredPlayersFromFormat = (format) => {
  const formatMap = {
    '1v1': 1,
    '2v2': 2,
    '3v3': 3,
    '4v4': 4,
    '5v5': 5,
    'BATTLE_ROYALE': 100 // À ajuster selon vos besoins
  };
  return formatMap[format] || 5; // Par défaut 5
};

/**
 * Inscrire une équipe
 * @param {string} userId - ID du capitaine qui inscrit
 * @param {string} teamId - ID de l'équipe
 * @param {string} eventId - ID de l'événement
 * @param {string[]} participatingMemberIds - IDs des membres participants (le capitaine est inclus automatiquement)
 */
export const createRegistration = async (userId, teamId, eventId, participatingMemberIds = []) => {
  // 1. Vérifier l'événement
  const event = await Event.findById(eventId);
  if (!event) throw new NotFoundError('Événement introuvable');

  // Vérifier les dates
  if (event.registrationStartDate && new Date() < new Date(event.registrationStartDate)) {
      throw new BadRequestError('Les inscriptions ne sont pas encore ouvertes');
  }
  if (event.registrationEndDate && new Date() > new Date(event.registrationEndDate)) {
      throw new BadRequestError('Les inscriptions sont fermées');
  }

  // 2. Vérifier l'équipe
  const team = await Team.findById(teamId).populate('members', '_id');
  if (!team) throw new NotFoundError('Équipe introuvable');

  // Vérifier que c'est bien le capitaine qui inscrit
  if (team.captain && team.captain.toString() !== userId.toString()) {
    throw new ForbiddenError('Seul le capitaine de l\'équipe peut inscrire l\'équipe');
  }

  // 3. Vérifier doublon
  const existing = await EventRegistration.findOne({ event: eventId, team: teamId });
  if (existing) throw new BadRequestError('L\'équipe est déjà inscrite à cet événement');

  // 4. Valider les membres participants selon le format de l'événement
  const requiredPlayers = getRequiredPlayersFromFormat(event.format);
  
  // Le capitaine est toujours inclus
  const allParticipatingMembers = [userId, ...participatingMemberIds];
  
  // Vérifier qu'on a le bon nombre de membres
  if (allParticipatingMembers.length !== requiredPlayers) {
    throw new BadRequestError(
      `Le format ${event.format} nécessite exactement ${requiredPlayers} joueurs. ` +
      `Vous avez sélectionné ${allParticipatingMembers.length} joueur(s) (le capitaine est inclus automatiquement).`
    );
  }

  // Vérifier que tous les membres sélectionnés font partie de l'équipe
  const teamMemberIds = team.members.map(m => m._id.toString());
  const captainId = team.captain.toString();
  
  for (const memberId of participatingMemberIds) {
    if (memberId === captainId) {
      throw new BadRequestError('Le capitaine est automatiquement inclus, ne le sélectionnez pas à nouveau');
    }
    if (!teamMemberIds.includes(memberId)) {
      throw new BadRequestError(`Le membre ${memberId} ne fait pas partie de cette équipe`);
    }
  }

  // Vérifier qu'il n'y a pas de doublons
  const uniqueMembers = [...new Set(allParticipatingMembers)];
  if (uniqueMembers.length !== allParticipatingMembers.length) {
    throw new BadRequestError('Des membres en double ont été sélectionnés');
  }

  // 5. Créer l'inscription
  const registration = await EventRegistration.create({
    event: eventId,
    team: teamId,
    registeredBy: userId,
    status: 'PENDING',
    participatingMembers: allParticipatingMembers
  });

  // Peupler les champs pour retourner l'objet complet
  const populatedRegistration = await EventRegistration.findById(registration._id)
    .populate({
      path: 'event',
      select: 'name startDate endDate game status format',
      populate: {
        path: 'game',
        select: 'name logo formats'
      }
    })
    .populate('team', 'name logo captain members')
    .populate('registeredBy', 'firstName lastName email gamertag')
    .populate('participatingMembers', 'firstName lastName email gamertag');

  // Envoyer des notifications à tous les membres de l'équipe
  try {
    const teamWithMembers = await Team.findById(teamId).populate('members', '_id firstName lastName gamertag');
    const captain = await teamWithMembers.populate('captain', 'firstName lastName gamertag');
    
    // Créer une notification pour chaque membre de l'équipe (y compris le capitaine)
    const allTeamMembers = [captain.captain, ...teamWithMembers.members];
    const uniqueMembers = Array.from(new Map(allTeamMembers.map(m => [m._id.toString(), m])).values());
    
    for (const member of uniqueMembers) {
      await createNotification({
        user: member._id,
        type: 'event_registration_created',
        title: 'Inscription à un événement',
        message: `Votre équipe "${teamWithMembers.name}" a été inscrite à l'événement "${populatedRegistration.event.name}"`,
        relatedEntity: {
          entityType: 'event',
          entityId: eventId
        }
      });
    }
  } catch (notificationError) {
    // Ne pas faire échouer l'inscription si les notifications échouent
    console.warn('⚠️  Erreur lors de la création des notifications d\'inscription:', notificationError.message);
  }

  return populatedRegistration;
};

/**
 * Récupérer les inscriptions avec filtres
 */
export const getRegistrations = async (filters = {}) => {
  const query = {};
  if (filters.event) query.event = filters.event;
  if (filters.team) query.team = filters.team;

  return await EventRegistration.find(query)
    .populate('team', 'name logo')
    .populate('event', 'name startDate format')
    .populate('registeredBy', 'firstName lastName')
    .populate('participatingMembers', 'firstName lastName email gamertag')
    .sort({ createdAt: -1 });
};

/**
 * Supprimer (Annuler) une inscription
 */
export const deleteRegistration = async (id, userId, isAdmin) => {
  const registration = await EventRegistration.findById(id)
    .populate('team', 'name members captain')
    .populate('event', 'name status');
  
  if (!registration) throw new NotFoundError('Inscription introuvable');

  // Seul l'admin ou celui qui a inscrit l'équipe (le capitaine) peut annuler
  if (!isAdmin && registration.registeredBy.toString() !== userId.toString()) {
    throw new ForbiddenError('Non autorisé à annuler cette inscription');
  }
  
  // Empêcher l'annulation si l'événement est en cours
  if (registration.event.status === 'in_progress') {
    throw new BadRequestError('Impossible d\'annuler une inscription pour un événement en cours');
  }

  // Envoyer des notifications à tous les membres de l'équipe avant de supprimer
  try {
    const team = await Team.findById(registration.team._id || registration.team)
      .populate('members', '_id firstName lastName gamertag')
      .populate('captain', 'firstName lastName gamertag');
    
    if (team) {
      const allTeamMembers = [team.captain, ...team.members];
      const uniqueMembers = Array.from(new Map(allTeamMembers.map(m => [m._id.toString(), m])).values());
      
      for (const member of uniqueMembers) {
        await createNotification({
          user: member._id,
          type: 'event_registration_cancelled',
          title: 'Inscription annulée',
          message: `L'inscription de votre équipe "${team.name}" à l'événement "${registration.event.name}" a été annulée`,
          relatedEntity: {
            entityType: 'event',
            entityId: registration.event._id || registration.event
          }
        });
      }
    }
  } catch (notificationError) {
    // Ne pas faire échouer la suppression si les notifications échouent
    console.warn('⚠️  Erreur lors de la création des notifications d\'annulation:', notificationError.message);
  }

  await EventRegistration.findByIdAndDelete(id);
  return true;
};

/**
 * Get registration by ID
 */
export const getRegistrationById = async (id) => {
  const registration = await EventRegistration.findById(id)
    .populate('team', 'name logo captain members')
    .populate('event', 'name startDate endDate game status format')
    .populate('registeredBy', 'firstName lastName email gamertag')
    .populate('participatingMembers', 'firstName lastName email gamertag');

  if (!registration) throw new NotFoundError('Inscription introuvable');
  return registration;
};

/**
 * Update registration (change status)
 * Permet à un admin d'accepter/rejeter une inscription
 * Permet à un captain de modifier certaines informations
 */
export const updateRegistration = async (id, updateData, userId, isAdmin) => {
  const registration = await EventRegistration.findById(id)
    .populate('team');
  
  if (!registration) throw new NotFoundError('Inscription introuvable');

  // Si c'est un admin, il peut changer le statut (ACCEPTED, REJECTED)
  if (isAdmin) {
    if (updateData.status && ['ACCEPTED', 'REJECTED', 'PENDING'].includes(updateData.status)) {
      const previousStatus = registration.status;
      registration.status = updateData.status;
      await registration.save();
      
      const updatedRegistration = await getRegistrationById(id);
      
      // Envoyer des notifications selon le changement de statut
      try {
        const teamWithMembers = await Team.findById(registration.team._id || registration.team)
          .populate('members', '_id firstName lastName gamertag')
          .populate('captain', '_id firstName lastName gamertag');
        
        const allTeamMembers = [teamWithMembers.captain, ...teamWithMembers.members];
        const uniqueMembers = Array.from(new Map(allTeamMembers.map(m => [m._id.toString(), m])).values());
        
        if (previousStatus !== 'ACCEPTED' && updateData.status === 'ACCEPTED') {
          // Inscription acceptée
          for (const member of uniqueMembers) {
            await createNotification({
              user: member._id,
              type: 'event_registration_accepted',
              title: 'Inscription acceptée',
              message: `Votre inscription à l'événement "${updatedRegistration.event.name}" a été acceptée ! Votre équipe "${teamWithMembers.name}" participera au tournoi.`,
              relatedEntity: {
                entityType: 'event',
                entityId: updatedRegistration.event._id || updatedRegistration.event.id
              }
            });
          }
        } else if (previousStatus !== 'REJECTED' && updateData.status === 'REJECTED') {
          // Inscription rejetée
          for (const member of uniqueMembers) {
            await createNotification({
              user: member._id,
              type: 'event_registration_rejected',
              title: 'Inscription rejetée',
              message: `Votre inscription à l'événement "${updatedRegistration.event.name}" a été rejetée.`,
              relatedEntity: {
                entityType: 'event',
                entityId: updatedRegistration.event._id || updatedRegistration.event.id
              }
            });
          }
        }
      } catch (notificationError) {
        // Ne pas faire échouer la mise à jour si les notifications échouent
        console.warn('⚠️  Erreur lors de la création des notifications de changement de statut:', notificationError.message);
      }
      
      return updatedRegistration;
    } else {
      throw new BadRequestError('Statut invalide. Valeurs autorisées : ACCEPTED, REJECTED, PENDING');
    }
  }

  // Si c'est le capitaine qui a inscrit l'équipe, il peut seulement annuler (CANCELLED)
  if (registration.registeredBy.toString() === userId.toString()) {
    if (updateData.status === 'CANCELLED') {
      registration.status = 'CANCELLED';
      await registration.save();
      
      const cancelledRegistration = await getRegistrationById(id);
      
      // Envoyer des notifications à tous les membres de l'équipe
      try {
        const teamWithMembers = await Team.findById(registration.team._id || registration.team)
          .populate('members', '_id firstName lastName gamertag')
          .populate('captain', 'firstName lastName gamertag');
        
        const allTeamMembers = [teamWithMembers.captain, ...teamWithMembers.members];
        const uniqueMembers = Array.from(new Map(allTeamMembers.map(m => [m._id.toString(), m])).values());
        
        for (const member of uniqueMembers) {
          await createNotification({
            user: member._id,
            type: 'event_registration_cancelled',
            title: 'Inscription annulée',
            message: `L'inscription de votre équipe "${teamWithMembers.name}" à l'événement "${cancelledRegistration.event.name}" a été annulée`,
            relatedEntity: {
              entityType: 'event',
              entityId: cancelledRegistration.event.id || cancelledRegistration.event._id
            }
          });
        }
      } catch (notificationError) {
        // Ne pas faire échouer l'annulation si les notifications échouent
        console.warn('⚠️  Erreur lors de la création des notifications d\'annulation:', notificationError.message);
      }
      
      return cancelledRegistration;
    } else {
      throw new ForbiddenError('Vous ne pouvez annuler que votre propre inscription');
    }
  }

  throw new ForbiddenError('Non autorisé à modifier cette inscription');
};