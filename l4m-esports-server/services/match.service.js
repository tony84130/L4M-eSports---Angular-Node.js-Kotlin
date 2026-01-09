import Match from '../models/match.model.js';
import Team from '../models/team.model.js';
import Event from '../models/event.model.js';
import { NotFoundError, BadRequestError, ForbiddenError } from '../utils/errors.js';
import { createNotification, notifyTeamMembers } from './notification.service.js';

/**
 * Get all matches with filters
 */
export const getAllMatches = async (filters = {}) => {
  const { event, status, team } = filters;
  const query = {};

  if (event) query.event = event;
  if (status) query.status = status;
  if (team) query.teams = team;

  const matches = await Match.find(query)
    .populate('event', 'name startDate endDate status')
    .populate('teams', 'name logo')
    .populate('winner', 'name logo')
    .sort({ scheduledTime: 1 });

  return matches;
};

/**
 * Get match by ID
 */
export const getMatchById = async (id) => {
  const match = await Match.findById(id)
    .populate('event', 'name startDate endDate status format')
    .populate('teams', 'name logo captain members')
    .populate('winner', 'name logo')
    .populate('validatedBy.user', 'firstName lastName gamertag');

  if (!match) throw new NotFoundError('Match introuvable');
  return match;
};

/**
 * Get matches by event ID
 */
export const getMatchesByEvent = async (eventId) => {
  const event = await Event.findById(eventId);
  if (!event) throw new NotFoundError('Événement introuvable');

  const matches = await Match.find({ event: eventId })
    .populate('teams', 'name logo')
    .populate('winner', 'name logo')
    .sort({ 'bracketPosition.round': -1, 'bracketPosition.matchNumber': 1 });

  return matches;
};

/**
 * Get matches by team ID
 */
export const getMatchesByTeam = async (teamId) => {
  const team = await Team.findById(teamId);
  if (!team) throw new NotFoundError('Équipe introuvable');

  const matches = await Match.find({ teams: teamId })
    .populate('event', 'name startDate endDate status')
    .populate('teams', 'name logo')
    .populate('winner', 'name logo')
    .sort({ scheduledTime: -1 });

  return matches;
};

/**
 * Update match status
 * @param {string} id - Match ID
 * @param {string} status - New status
 * @param {string} userId - User ID (must be admin)
 */
export const updateMatchStatus = async (id, status, userId) => {
  const match = await Match.findById(id).populate('teams', 'captain');
  if (!match) throw new NotFoundError('Match introuvable');

  // Vérifier que le statut est valide
  const validStatuses = ['upcoming', 'in_progress', 'finished', 'pending_validation', 'cancelled'];
  if (!validStatuses.includes(status)) {
    throw new BadRequestError(`Statut invalide. Valeurs autorisées : ${validStatuses.join(', ')}`);
  }

  // Vérifier les permissions : admin uniquement
  const User = (await import('../models/user.model.js')).default;
  const user = await User.findById(userId);
  if (!user) throw new NotFoundError('Utilisateur introuvable');

  const isAdmin = user.role === 'admin';

  if (!isAdmin) {
    throw new ForbiddenError('Seuls les admins peuvent modifier le statut d\'un match');
  }

  const previousStatus = match.status;
  
  // Mettre à jour le statut
  match.status = status;

  // Mettre à jour les heures si nécessaire
  if (status === 'in_progress' && !match.actualStartTime) {
    match.actualStartTime = new Date();
  } else if (status === 'finished' && !match.actualEndTime) {
    match.actualEndTime = new Date();
  }

  await match.save();

  // Envoyer des notifications selon le changement de statut
  try {
    const event = await Event.findById(match.event).populate('game', 'name');
    const populatedMatch = await Match.findById(id)
      .populate('teams', 'name logo captain members')
      .populate('winner', 'name');

    // Notification : Match commence
    if (previousStatus !== 'in_progress' && status === 'in_progress') {
      for (const team of populatedMatch.teams) {
        await notifyTeamMembers(team._id, {
          type: 'match_status_changed',
          title: 'Match en cours',
          message: `Votre match contre ${populatedMatch.teams.find(t => t._id.toString() !== team._id.toString())?.name || 'l\'adversaire'} dans l'événement "${event.name}" a commencé !`,
          relatedEntity: {
            entityType: 'match',
            entityId: id
          }
        });
      }
    }

    // Notification : Match terminé
    if (previousStatus !== 'finished' && status === 'finished' && populatedMatch.winner) {
      const winnerTeam = populatedMatch.teams.find(t => t._id.toString() === populatedMatch.winner._id.toString());
      const loserTeam = populatedMatch.teams.find(t => t._id.toString() !== populatedMatch.winner._id.toString());

      if (winnerTeam) {
        await notifyTeamMembers(winnerTeam._id, {
          type: 'match_won',
          title: 'Victoire !',
          message: `Félicitations ! Votre équipe "${winnerTeam.name}" a remporté le match contre "${loserTeam?.name || 'l\'adversaire'}" dans l'événement "${event.name}" !`,
          relatedEntity: {
            entityType: 'match',
            entityId: id
          }
        });
      }

      if (loserTeam) {
        await notifyTeamMembers(loserTeam._id, {
          type: 'match_lost',
          title: 'Match terminé',
          message: `Votre équipe "${loserTeam.name}" a été éliminée par "${winnerTeam?.name || 'l\'adversaire'}" dans l'événement "${event.name}".`,
          relatedEntity: {
            entityType: 'match',
            entityId: id
          }
        });
      }
    }

    // Notification : Match annulé
    if (previousStatus !== 'cancelled' && status === 'cancelled') {
      for (const team of populatedMatch.teams) {
        await notifyTeamMembers(team._id, {
          type: 'match_cancelled',
          title: 'Match annulé',
          message: `Le match contre ${populatedMatch.teams.find(t => t._id.toString() !== team._id.toString())?.name || 'l\'adversaire'} dans l'événement "${event.name}" a été annulé.`,
          relatedEntity: {
            entityType: 'match',
            entityId: id
          }
        });
      }
    }
  } catch (notificationError) {
    // Ne pas faire échouer la mise à jour si les notifications échouent
    console.warn('⚠️  Erreur lors de la création des notifications de changement de statut:', notificationError.message);
  }

  return await getMatchById(id);
};

/**
 * Update match score
 * @param {string} id - Match ID
 * @param {object} score - Score object { team1: number, team2: number }
 * @param {string} userId - User ID (must be admin)
 */
export const updateMatchScore = async (id, score, userId) => {
  const match = await Match.findById(id).populate('teams', 'captain');
  if (!match) throw new NotFoundError('Match introuvable');

  // Vérifier que le match a deux équipes
  if (!match.teams || match.teams.length < 2) {
    throw new BadRequestError('Un match doit avoir deux équipes');
  }

  // Vérifier les permissions : admin uniquement
  const User = (await import('../models/user.model.js')).default;
  const user = await User.findById(userId);
  if (!user) throw new NotFoundError('Utilisateur introuvable');

  const isAdmin = user.role === 'admin';

  if (!isAdmin) {
    throw new ForbiddenError('Seuls les admins peuvent modifier le score d\'un match');
  }

  // Vérifier que les scores sont valides
  if (typeof score.team1 !== 'number' || typeof score.team2 !== 'number') {
    throw new BadRequestError('Les scores doivent être des nombres');
  }

  if (score.team1 < 0 || score.team2 < 0) {
    throw new BadRequestError('Les scores ne peuvent pas être négatifs');
  }

  // Vérifier que le match est en cours (seuls les matchs en cours peuvent avoir leur score modifié)
  if (match.status !== 'in_progress') {
    throw new BadRequestError('Le score ne peut être modifié que lorsque le match est en cours (statut: in_progress)');
  }

  const previousScore = { team1: match.score.team1, team2: match.score.team2 };
  
  // Mettre à jour le score
  match.score.team1 = score.team1;
  match.score.team2 = score.team2;

  // Déterminer le gagnant si les scores sont différents
  if (score.team1 > score.team2) {
    match.winner = match.teams[0]._id;
  } else if (score.team2 > score.team1) {
    match.winner = match.teams[1]._id;
  } else {
    // Match nul, pas de gagnant
    match.winner = null;
  }

  // Si le match est en cours et qu'un gagnant est déterminé, passer en pending_validation
  if (match.status === 'in_progress' && match.winner) {
    match.status = 'pending_validation';
    match.actualEndTime = new Date();
  }

  await match.save();

  // Envoyer des notifications de mise à jour du score
  try {
    const event = await Event.findById(match.event).populate('game', 'name');
    const populatedMatch = await Match.findById(id)
      .populate('teams', 'name logo captain members');

    // Notifier seulement si le score a changé
    if (previousScore.team1 !== score.team1 || previousScore.team2 !== score.team2) {
      for (const team of populatedMatch.teams) {
        await notifyTeamMembers(team._id, {
          type: 'match_score_updated',
          title: 'Score mis à jour',
          message: `Le score du match dans l'événement "${event.name}" a été mis à jour : ${score.team1} - ${score.team2}`,
          relatedEntity: {
            entityType: 'match',
            entityId: id
          }
        });
      }
    }
  } catch (notificationError) {
    // Ne pas faire échouer la mise à jour si les notifications échouent
    console.warn('⚠️  Erreur lors de la création des notifications de mise à jour du score:', notificationError.message);
  }

  return await getMatchById(id);
};

/**
 * Validate match result
 * @param {string} id - Match ID
 * @param {string} userId - User ID (must be admin)
 */
export const validateMatchResult = async (id, userId) => {
  const match = await Match.findById(id).populate('teams', 'captain');
  if (!match) throw new NotFoundError('Match introuvable');

  // Vérifier que le match est en attente de validation
  if (match.status !== 'pending_validation') {
    throw new BadRequestError('Le match doit être en attente de validation (pending_validation)');
  }

  // Vérifier les permissions : admin uniquement
  const User = (await import('../models/user.model.js')).default;
  const user = await User.findById(userId);
  if (!user) throw new NotFoundError('Utilisateur introuvable');

  const isAdmin = user.role === 'admin';

  if (!isAdmin) {
    throw new ForbiddenError('Seuls les admins peuvent valider un match');
  }

  // Ajouter la validation
  match.validatedBy.push({
    user: userId,
    validatedAt: new Date()
  });

  // Si c'est un admin qui valide, le match est directement terminé
  match.status = 'finished';
  await match.save();

  // Envoyer des notifications de match terminé
  try {
    const event = await Event.findById(match.event).populate('game', 'name');
    const populatedMatch = await Match.findById(id)
      .populate('teams', 'name logo captain members')
      .populate('winner', 'name');

    if (populatedMatch.winner) {
      const winnerTeam = populatedMatch.teams.find(t => t._id.toString() === populatedMatch.winner._id.toString());
      const loserTeam = populatedMatch.teams.find(t => t._id.toString() !== populatedMatch.winner._id.toString());

      if (winnerTeam) {
        await notifyTeamMembers(winnerTeam._id, {
          type: 'match_finished',
          title: 'Match terminé - Victoire !',
          message: `Félicitations ! Votre équipe "${winnerTeam.name}" a remporté le match contre "${loserTeam?.name || 'l\'adversaire'}" dans l'événement "${event.name}" ! Score final : ${populatedMatch.score.team1} - ${populatedMatch.score.team2}`,
          relatedEntity: {
            entityType: 'match',
            entityId: id
          }
        });
      }

      if (loserTeam) {
        await notifyTeamMembers(loserTeam._id, {
          type: 'match_finished',
          title: 'Match terminé',
          message: `Le match contre "${winnerTeam?.name || 'l\'adversaire'}" dans l'événement "${event.name}" est terminé. Score final : ${populatedMatch.score.team1} - ${populatedMatch.score.team2}`,
          relatedEntity: {
            entityType: 'match',
            entityId: id
          }
        });
      }
    }
  } catch (notificationError) {
    // Ne pas faire échouer la validation si les notifications échouent
    console.warn('⚠️  Erreur lors de la création des notifications de match terminé:', notificationError.message);
  }

  // Faire progresser le bracket si le match a un vainqueur
  if (match.winner) {
    try {
      const eventService = await import('./event.service.js');
      await eventService.advanceBracket(match.event.toString(), match._id.toString(), match.winner.toString());
    } catch (error) {
      console.error('[validateMatchResult] Error advancing bracket:', error);
      // Ne pas faire échouer la validation si l'avancement du bracket échoue
    }
  }

  return await getMatchById(id);
};

/**
 * Update match (e.g., scheduled time)
 * @param {string} id - Match ID
 * @param {object} updateData - Update data (e.g., { scheduledTime: Date })
 * @param {string} userId - User ID (must be admin)
 */
export const updateMatch = async (id, updateData, userId) => {
  const match = await Match.findById(id).populate('teams', 'captain');
  if (!match) throw new NotFoundError('Match introuvable');

  // Vérifier les permissions : admin uniquement
  const User = (await import('../models/user.model.js')).default;
  const user = await User.findById(userId);
  if (!user) throw new NotFoundError('Utilisateur introuvable');

  const isAdmin = user.role === 'admin';

  if (!isAdmin) {
    throw new ForbiddenError('Seuls les admins peuvent modifier un match');
  }

  const previousScheduledTime = match.scheduledTime;
  
  // Mettre à jour le match
  if (updateData.scheduledTime) {
    match.scheduledTime = new Date(updateData.scheduledTime);
  }
  
  await match.save();

  // Envoyer une notification si la date/heure a changé
  if (updateData.scheduledTime && 
      new Date(updateData.scheduledTime).getTime() !== new Date(previousScheduledTime).getTime()) {
    try {
      const event = await Event.findById(match.event).populate('game', 'name');
      const populatedMatch = await Match.findById(id)
        .populate('teams', 'name logo captain members');

      const newDate = new Date(updateData.scheduledTime);
      const formattedDate = newDate.toLocaleDateString('fr-FR');
      const formattedTime = newDate.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });

      for (const team of populatedMatch.teams) {
        await notifyTeamMembers(team._id, {
          type: 'match_rescheduled',
          title: 'Match reporté',
          message: `Votre match contre ${populatedMatch.teams.find(t => t._id.toString() !== team._id.toString())?.name || 'l\'adversaire'} dans l'événement "${event.name}" a été reporté au ${formattedDate} à ${formattedTime}.`,
          relatedEntity: {
            entityType: 'match',
            entityId: id
          }
        });
      }
    } catch (notificationError) {
      console.warn('⚠️  Erreur lors de la création des notifications de match reporté:', notificationError.message);
    }
  }

  return await getMatchById(id);
};

