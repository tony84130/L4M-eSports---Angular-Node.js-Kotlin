import Event from '../models/event.model.js';
import EventRegistration from '../models/eventRegistration.model.js';
import Team from '../models/team.model.js';
import Game from '../models/game.model.js';
import { NotFoundError, BadRequestError } from '../utils/errors.js';
import { createNotification, notifyTeamMembers } from './notification.service.js';

/**
 * Get all events with filters
 */
/**
 * Check if all matches in an event are finished
 */
const areAllMatchesFinished = async (eventId) => {
  const Match = (await import('../models/match.model.js')).default;
  const allMatches = await Match.find({ event: eventId });
  
  if (allMatches.length === 0) {
    return false; // Pas de matchs, donc pas terminé
  }
  
  const finishedMatches = allMatches.filter(m => m.status === 'finished');
  return finishedMatches.length === allMatches.length;
};

/**
 * Update event status based on dates and match completion (called automatically)
 */
const updateEventStatusByDate = async (event) => {
  const now = new Date();
  const registrationStartDate = new Date(event.registrationStartDate);
  const registrationEndDate = new Date(event.registrationEndDate);
  const startDate = new Date(event.startDate);
  const endDate = new Date(event.endDate);
  
  let statusChanged = false;
  let newStatus = event.status;

  // PRIORITÉ 1: Si tous les matchs sont terminés, l'événement est terminé (peu importe la date)
  if (event.bracket && event.status !== 'completed' && event.status !== 'cancelled') {
    const allFinished = await areAllMatchesFinished(event._id.toString());
    if (allFinished) {
      newStatus = 'completed';
      statusChanged = true;
    }
  }

  // PRIORITÉ 2: Si l'événement est terminé par date (endDate < maintenant) et n'est pas déjà "completed" ou "cancelled"
  if (!statusChanged && endDate < now && event.status !== 'completed' && event.status !== 'cancelled') {
    newStatus = 'completed';
    statusChanged = true;
  }
  // PRIORITÉ 3: Si l'événement a commencé (startDate <= maintenant) et n'est pas terminé
  else if (!statusChanged && startDate <= now && endDate >= now && event.status !== 'in_progress' && event.status !== 'completed' && event.status !== 'cancelled') {
    newStatus = 'in_progress';
    statusChanged = true;
  }
  // PRIORITÉ 4: Si les inscriptions sont fermées (registrationEndDate < maintenant) et l'événement n'a pas encore commencé
  else if (!statusChanged && registrationEndDate < now && startDate > now && event.status === 'open') {
    newStatus = 'registration_closed';
    statusChanged = true;
  }
  // PRIORITÉ 5: Si les inscriptions sont ouvertes (registrationStartDate <= maintenant <= registrationEndDate)
  else if (!statusChanged && registrationStartDate <= now && registrationEndDate >= now && event.status === 'draft') {
    newStatus = 'open';
    statusChanged = true;
  }

  if (statusChanged) {
    event.status = newStatus;
    await event.save();
    // Emit socket event for real-time sync
    try {
      const { emitToAll } = await import('./socket.service.js');
      const populatedEvent = await Event.findById(event._id)
        .populate('game', 'name logo formats')
        .populate('createdBy', 'firstName lastName');
      emitToAll('event:updated', { eventId: event._id.toString(), event: populatedEvent });
    } catch (socketError) {
      console.warn('⚠️  Erreur lors de l\'émission Socket.io:', socketError.message);
    }
  }

  return event;
};

export const getAllEvents = async (filters = {}) => {
  const { game, status, upcoming } = filters;
  const query = {};

  if (game) query.game = game;
  if (status) query.status = status;
  
  // Filtre "upcoming" : événements futurs ou en cours (non terminés)
  if (upcoming === 'true') {
    // Inclure les événements qui n'ont pas encore fini (endDate >= maintenant)
    query.endDate = { $gte: new Date() };
    // Si on veut seulement les 'UPCOMING' par défaut, inclure aussi 'draft', 'open', 'registration_closed' et 'in_progress'
    if (!status) query.status = { $in: ['draft', 'open', 'registration_closed', 'in_progress'] }; // Adapté à ton enum
  }

  const events = await Event.find(query)
    .populate('game', 'name logo formats')
    .populate('createdBy', 'firstName lastName')
    .sort({ startDate: 1 }); // Les plus proches en premier

  // Mettre à jour automatiquement les statuts basés sur les dates
  const updatedEvents = await Promise.all(
    events.map(event => updateEventStatusByDate(event))
  );

  return updatedEvents;
};

/**
 * Get events nearby (Géolocalisation)
 * Format attendu: long,lat (ex: -71.9,45.4)
 */
export const getEventsNearby = async (longitude, latitude, distanceInKm = 50) => {
  if (!longitude || !latitude) {
    throw new BadRequestError('La longitude et la latitude sont requises');
  }

  const events = await Event.find({
    'location.type': 'physical', // Correction: correspond à l'enum du modèle
    'location.coordinates': {    // Correction: on cible le champ GeoJSON
      $near: {
        $geometry: {
          type: 'Point',
          coordinates: [parseFloat(longitude), parseFloat(latitude)]
        },
        $maxDistance: distanceInKm * 1000 // Conversion en mètres
      }
    }
  }).populate('game', 'name formats');

  return events;
};

/**
 * Get event by ID with bracket
 */
export const getEventById = async (id) => {
  const event = await Event.findById(id)
    .populate('game', 'name logo formats')
    .populate('createdBy', 'firstName lastName')
    .populate({
      path: 'bracket.rounds.matches.team1',
      select: 'name logo'
    })
    .populate({
      path: 'bracket.rounds.matches.team2',
      select: 'name logo'
    })
    .populate({
      path: 'bracket.rounds.matches.winner',
      select: 'name logo'
    });

  if (!event) throw new NotFoundError('Événement introuvable');
  
  // Mettre à jour automatiquement le statut basé sur les dates et les matchs
  const updatedEvent = await updateEventStatusByDate(event);
  
  return updatedEvent;
};

/**
 * Create event
 */
export const createEvent = async (eventData, userId) => {
  // Validation basique des dates
  if (new Date(eventData.startDate) < new Date()) {
    throw new BadRequestError('La date de début doit être dans le futur');
  }

  // Vérifier le jeu et ses formats
  const game = await Game.findById(eventData.game);
  if (!game) {
    throw new NotFoundError('Jeu introuvable');
  }

  // Si le jeu n'a qu'un seul format, l'utiliser automatiquement
  if (game.formats && game.formats.length === 1) {
    eventData.format = game.formats[0];
  } else if (game.formats && game.formats.length > 1) {
    // Si plusieurs formats, valider que le format fourni est valide
    if (!eventData.format) {
      throw new BadRequestError(`Le format est obligatoire. Formats disponibles : ${game.formats.join(', ')}`);
    }
    if (!game.formats.includes(eventData.format)) {
      throw new BadRequestError(`Format invalide. Formats disponibles pour ce jeu : ${game.formats.join(', ')}`);
    }
  } else {
    // Si aucun format défini pour le jeu, utiliser le format fourni (rétrocompatibilité)
    if (!eventData.format) {
      throw new BadRequestError('Le format est obligatoire');
    }
  }

  // Correction: Conversion auto des coordonnées si fournies séparément
  // (Le front-end envoie souvent lat/long à plat, Mongo veut du GeoJSON)
  if (eventData.location && eventData.location.coordinates && eventData.location.coordinates.latitude) {
     const { latitude, longitude } = eventData.location.coordinates;
     eventData.location.coordinates = {
         type: 'Point',
         coordinates: [longitude, latitude] // Note: MongoDB attend [Long, Lat]
     };
  }

  // Définir le statut à 'open' si la date de début est dans le futur et qu'aucun statut n'est fourni
  // Cela permet aux événements créés d'être visibles dans la liste des événements à venir
  if (!eventData.status) {
    const startDate = new Date(eventData.startDate);
    const now = new Date();
    if (startDate >= now) {
      eventData.status = 'open';
    }
  }

  const event = await Event.create({
    ...eventData,
    createdBy: userId
  });

  // Peupler le champ game pour retourner l'objet complet
  return await Event.findById(event._id)
    .populate('game', 'name logo formats')
    .populate('createdBy', 'firstName lastName gamertag');
};

/**
 * Update event
 */
export const updateEvent = async (id, updateData) => {
  const event = await Event.findById(id).populate('game');
  if (!event) throw new NotFoundError('Événement introuvable');
  
  // Empêcher la modification si l'événement est en cours
  if (event.status === 'in_progress') {
    throw new BadRequestError('Impossible de modifier un événement en cours');
  }

  // Si le format est modifié, valider qu'il est dans les formats du jeu
  if (updateData.format && event.game && event.game.formats && event.game.formats.length > 0) {
    if (!event.game.formats.includes(updateData.format)) {
      throw new BadRequestError(`Format invalide. Formats disponibles pour ce jeu : ${event.game.formats.join(', ')}`);
    }
  }

  // Si le format n'est pas fourni et que le jeu n'a qu'un seul format, l'utiliser
  if (!updateData.format && event.game && event.game.formats && event.game.formats.length === 1) {
    updateData.format = event.game.formats[0];
  }

  const previousStatus = event.status;
  const previousRegistrationStartDate = event.registrationStartDate;
  const previousRegistrationEndDate = event.registrationEndDate;
  
  Object.assign(event, updateData);
  await event.save();

  // Envoyer des notifications pour les changements d'événement
  try {
    // Si le statut passe à 'cancelled', notifier toutes les équipes inscrites
    if (previousStatus !== 'cancelled' && updateData.status === 'cancelled') {
      const registrations = await EventRegistration.find({
        event: event._id,
        status: { $in: ['ACCEPTED', 'PENDING'] }
      }).populate('team', 'name members captain');

      for (const registration of registrations) {
        const team = await Team.findById(registration.team._id || registration.team)
          .populate('members', '_id firstName lastName gamertag')
          .populate('captain', '_id firstName lastName gamertag');
        
        if (team) {
          const allTeamMembers = [team.captain, ...team.members];
          const uniqueMembers = Array.from(new Map(allTeamMembers.map(m => [m._id.toString(), m])).values());
          
          for (const member of uniqueMembers) {
            await createNotification({
              user: member._id,
              type: 'event_cancelled',
              title: 'Événement annulé',
              message: `L'événement "${event.name}" auquel votre équipe "${team.name}" était inscrite a été annulé.`,
              relatedEntity: {
                entityType: 'event',
                entityId: event._id
              }
            });
          }
        }
      }
    }

    // Si l'événement est modifié (changements importants), notifier les équipes inscrites
    const hasImportantChanges = 
      (updateData.startDate && new Date(updateData.startDate).getTime() !== new Date(event.startDate).getTime()) ||
      (updateData.endDate && new Date(updateData.endDate).getTime() !== new Date(event.endDate).getTime()) ||
      (updateData.location && JSON.stringify(updateData.location) !== JSON.stringify(event.location)) ||
      (updateData.format && updateData.format !== event.format);

    if (hasImportantChanges && previousStatus !== 'cancelled') {
      const registrations = await EventRegistration.find({
        event: event._id,
        status: 'ACCEPTED'
      }).populate('team', 'name members captain');

      const changes = [];
      if (updateData.startDate) changes.push(`date de début: ${new Date(updateData.startDate).toLocaleDateString('fr-FR')}`);
      if (updateData.endDate) changes.push(`date de fin: ${new Date(updateData.endDate).toLocaleDateString('fr-FR')}`);
      if (updateData.location?.address) changes.push(`lieu: ${updateData.location.address}`);
      if (updateData.format) changes.push(`format: ${updateData.format}`);

      for (const registration of registrations) {
        const team = await Team.findById(registration.team._id || registration.team)
          .populate('members', '_id firstName lastName gamertag')
          .populate('captain', '_id firstName lastName gamertag');
        
        if (team) {
          const allTeamMembers = [team.captain, ...team.members];
          const uniqueMembers = Array.from(new Map(allTeamMembers.map(m => [m._id.toString(), m])).values());
          
          for (const member of uniqueMembers) {
            await createNotification({
              user: member._id,
              type: 'event_updated',
              title: 'Événement modifié',
              message: `L'événement "${event.name}" a été modifié. Changements : ${changes.join(', ')}.`,
              relatedEntity: {
                entityType: 'event',
                entityId: event._id
              }
            });
          }
        }
      }
    }

    // Si les inscriptions s'ouvrent (registrationStartDate change et est maintenant dans le passé ou présent)
    if (updateData.registrationStartDate && 
        new Date(updateData.registrationStartDate).getTime() !== new Date(previousRegistrationStartDate).getTime() &&
        new Date(updateData.registrationStartDate) <= new Date()) {
      // Notifier les utilisateurs intéressés (optionnel - pourrait être filtré par préférences de jeu)
      // Pour l'instant, on notifie seulement les équipes qui suivent ce jeu
      // Cette logique peut être améliorée avec un système de préférences utilisateur
    }

    // Si les inscriptions se ferment (registrationEndDate change et est maintenant dans le passé)
    if (updateData.registrationEndDate && 
        new Date(updateData.registrationEndDate).getTime() !== new Date(previousRegistrationEndDate).getTime() &&
        new Date(updateData.registrationEndDate) < new Date()) {
      // Notifier les équipes en attente (PENDING)
      const pendingRegistrations = await EventRegistration.find({
        event: event._id,
        status: 'PENDING'
      }).populate('team', 'name members captain');

      for (const registration of pendingRegistrations) {
        const team = await Team.findById(registration.team._id || registration.team)
          .populate('members', '_id firstName lastName gamertag')
          .populate('captain', '_id firstName lastName gamertag');
        
        if (team) {
          const allTeamMembers = [team.captain, ...team.members];
          const uniqueMembers = Array.from(new Map(allTeamMembers.map(m => [m._id.toString(), m])).values());
          
          for (const member of uniqueMembers) {
            await createNotification({
              user: member._id,
              type: 'event_registration_closed',
              title: 'Inscriptions fermées',
              message: `Les inscriptions pour l'événement "${event.name}" sont maintenant fermées. Votre inscription est toujours en attente de validation.`,
              relatedEntity: {
                entityType: 'event',
                entityId: event._id
              }
            });
          }
        }
      }
    }
  } catch (notificationError) {
    console.warn('⚠️  Erreur lors de la création des notifications de modification d\'événement:', notificationError.message);
  }

  // Si le statut passe à 'in_progress', envoyer des notifications à tous les membres des équipes inscrites
  if (previousStatus !== 'in_progress' && updateData.status === 'in_progress') {
    try {
      // Récupérer toutes les inscriptions acceptées pour cet événement
      const registrations = await EventRegistration.find({
        event: event._id,
        status: 'ACCEPTED'
      }).populate('team', 'name members captain');

      // Pour chaque inscription, envoyer une notification à tous les membres de l'équipe
      for (const registration of registrations) {
        const team = await Team.findById(registration.team._id || registration.team)
          .populate('members', '_id firstName lastName gamertag')
          .populate('captain', 'firstName lastName gamertag');
        
        if (team) {
          const allTeamMembers = [team.captain, ...team.members];
          const uniqueMembers = Array.from(new Map(allTeamMembers.map(m => [m._id.toString(), m])).values());
          
          for (const member of uniqueMembers) {
            await createNotification({
              user: member._id,
              type: 'event_started',
              title: 'Événement commencé',
              message: `L'événement "${event.name}" auquel votre équipe "${team.name}" participe a commencé !`,
              relatedEntity: {
                entityType: 'event',
                entityId: event._id
              }
            });
          }
        }
      }
    } catch (notificationError) {
      // Ne pas faire échouer la mise à jour si les notifications échouent
      console.warn('⚠️  Erreur lors de la création des notifications de début d\'événement:', notificationError.message);
    }
  }

  // Peupler les champs game et createdBy pour retourner l'objet complet
  return await Event.findById(event._id)
    .populate('game', 'name logo formats')
    .populate('createdBy', 'firstName lastName gamertag');
};

/**
 * Delete event
 */
export const deleteEvent = async (id) => {
  const event = await Event.findById(id);
  if (!event) throw new NotFoundError('Événement introuvable');
  
  // Supprimer toutes les inscriptions associées à cet événement
  const EventRegistration = (await import('../models/eventRegistration.model.js')).default;
  await EventRegistration.deleteMany({ event: id });
  
  // Supprimer l'événement
  await Event.findByIdAndDelete(id);
  return true;
};

/**
 * Generate Tournament Bracket
 */
export const generateBracket = async (id) => {
  const event = await Event.findById(id);
  if (!event) throw new NotFoundError('Événement introuvable');

  // 0. Vérifier si des matchs ont déjà été joués (empêcher la régénération si c'est le cas)
  const Match = (await import('../models/match.model.js')).default;
  const existingMatches = await Match.find({ event: id });
  
  if (existingMatches.length > 0) {
    // Vérifier si au moins un match a été joué (statut différent de 'upcoming' ou scores non nuls)
    const hasPlayedMatches = existingMatches.some(match => 
      match.status !== 'upcoming' || 
      match.score.team1 > 0 || 
      match.score.team2 > 0 ||
      match.winner != null
    );
    
    if (hasPlayedMatches) {
      throw new BadRequestError('Impossible de régénérer le bracket : des matchs ont déjà été joués. La régénération supprimerait les résultats existants.');
    }
  }

  // 1. Récupérer les inscriptions validées
  const registrations = await EventRegistration.find({ 
    event: id, 
    // On accepte 'PENDING' aussi pour tester facilement si tu n'as pas fait l'admin panel encore
    status: { $in: ['ACCEPTED', 'PENDING'] } 
  }).populate('team');

  console.log(`[generateBracket] Found ${registrations.length} registrations for event ${id}`);

  if (registrations.length === 0) {
    throw new BadRequestError('Aucune équipe inscrite à cet événement');
  }

  if (registrations.length < 2) {
    throw new BadRequestError(`Pas assez d'équipes pour générer un bracket (minimum 2, actuellement ${registrations.length})`);
  }

  // 2. Filtrer les inscriptions avec des équipes valides et mélanger les équipes (Fisher-Yates shuffle)
  const validRegistrations = registrations.filter(r => r.team && r.team._id);
  if (validRegistrations.length < 2) {
    throw new BadRequestError('Pas assez d\'équipes valides pour générer un bracket (minimum 2)');
  }

  const teams = validRegistrations.map(r => r.team);
  for (let i = teams.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [teams[i], teams[j]] = [teams[j], teams[i]];
  }

  // 3. Créer les matchs du premier round
  const matches = [];
  for (let i = 0; i < teams.length; i += 2) {
    if (i + 1 < teams.length) {
      matches.push({
        team1: teams[i]._id,
        team2: teams[i + 1]._id
      });
    } else {
      // Bye (équipe passe automatiquement)
      matches.push({
        team1: teams[i]._id,
        team2: null, 
        winner: teams[i]._id 
      });
    }
  }

  // 4. Sauvegarder dans l'event
  event.bracket = {
    rounds: [{
      roundNumber: 1,
      matches: matches
    }]
  };
  
  event.status = 'in_progress'; // Correction: correspond à l'enum du modèle
  await event.save();

  // 5. Créer les documents Match à partir du bracket (Match est déjà importé plus haut)
  
  // Supprimer les anciens matchs de cet événement s'ils existent
  await Match.deleteMany({ event: id });
  
  // Créer les nouveaux matchs
  const matchDocuments = [];
  const round = event.bracket.rounds[0];
  const scheduledTime = event.startDate || new Date();
  
  for (let i = 0; i < round.matches.length; i++) {
    const bracketMatch = round.matches[i];
    
    // Ignorer les matchs avec bye (team2 null)
    if (!bracketMatch.team2) {
      continue;
    }
    
    const teamsArray = [bracketMatch.team1, bracketMatch.team2];
    
    const matchDoc = await Match.create({
      event: id,
      teams: teamsArray,
      scheduledTime: new Date(scheduledTime.getTime() + (i * 60 * 60 * 1000)), // Espacer les matchs d'1 heure
      status: 'upcoming',
      score: {
        team1: 0,
        team2: 0
      },
      bracketPosition: {
        round: round.roundNumber,
        matchNumber: i + 1,
        bracketSide: 'single'
      },
      winner: bracketMatch.winner || null
    });
    
    matchDocuments.push(matchDoc);
  }
  
  if (matchDocuments.length > 0) {
    console.log(`[generateBracket] ✓ Created ${matchDocuments.length} match document(s) for event ${id}`);
    
    // Envoyer des notifications aux équipes pour les nouveaux matchs créés
    try {
      for (const matchDoc of matchDocuments) {
        const populatedMatch = await Match.findById(matchDoc._id)
          .populate('teams', 'name logo captain members')
          .populate('event', 'name');
        
        for (const team of populatedMatch.teams) {
          const opponentTeam = populatedMatch.teams.find(t => t._id.toString() !== team._id.toString());
          await notifyTeamMembers(team._id, {
            type: 'match_created',
            title: 'Nouveau match programmé',
            message: `Un nouveau match a été créé pour votre équipe "${team.name}" contre "${opponentTeam?.name || 'l\'adversaire'}" dans l'événement "${populatedMatch.event.name}". Le match est prévu pour le ${new Date(populatedMatch.scheduledTime).toLocaleDateString('fr-FR')} à ${new Date(populatedMatch.scheduledTime).toLocaleTimeString('fr-FR')}.`,
            relatedEntity: {
              entityType: 'match',
              entityId: matchDoc._id
            }
          });
        }
      }
    } catch (notificationError) {
      console.warn('⚠️  Erreur lors de la création des notifications de match créé:', notificationError.message);
    }
  } else {
    console.log(`[generateBracket] ⚠️  No matches created (all matches have bye)`);
  }

  // 6. Retourner le bracket peuplé avec les équipes
  const populatedEvent = await Event.findById(event._id)
    .populate({
      path: 'bracket.rounds.matches.team1',
      select: 'name logo'
    })
    .populate({
      path: 'bracket.rounds.matches.team2',
      select: 'name logo'
    })
    .populate({
      path: 'bracket.rounds.matches.winner',
      select: 'name logo'
    });

  return populatedEvent.bracket;
};

/**
 * Get event bracket
 */
export const getEventBracket = async (id) => {
  const event = await Event.findById(id)
    .populate('game', 'name logo formats')
    .populate({
      path: 'bracket.rounds.matches.team1',
      select: 'name logo'
    })
    .populate({
      path: 'bracket.rounds.matches.team2',
      select: 'name logo'
    })
    .populate({
      path: 'bracket.rounds.matches.winner',
      select: 'name logo'
    });

  if (!event) throw new NotFoundError('Événement introuvable');
  
  // Retourner le bracket ou null si pas encore généré
  return event.bracket || null;
};

/**
 * Advance bracket when a match is finished
 * Updates the bracket and creates next round matches if needed
 * @param {string} eventId - Event ID
 * @param {string} matchId - Match ID that was finished
 * @param {string} winnerId - Winner team ID
 */
export const advanceBracket = async (eventId, matchId, winnerId) => {
  const Event = (await import('../models/event.model.js')).default;
  const Match = (await import('../models/match.model.js')).default;
  
  const event = await Event.findById(eventId);
  if (!event || !event.bracket) {
    console.log(`[advanceBracket] Event ${eventId} not found or has no bracket`);
    return;
  }

  // Trouver le match terminé
  const finishedMatch = await Match.findById(matchId);
  if (!finishedMatch) {
    console.log(`[advanceBracket] Match ${matchId} not found`);
    return;
  }

  const currentRound = finishedMatch.bracketPosition.round;
  const currentMatchNumber = finishedMatch.bracketPosition.matchNumber;

  // Mettre à jour le winner dans le bracket
  const round = event.bracket.rounds.find(r => r.roundNumber === currentRound);
  if (round && round.matches) {
    const bracketMatch = round.matches[currentMatchNumber - 1];
    if (bracketMatch) {
      bracketMatch.winner = winnerId;
      await event.save();
    }
  }

  // Vérifier si tous les matchs du round actuel sont terminés
  const allMatchesInRound = await Match.find({
    event: eventId,
    'bracketPosition.round': currentRound
  });

  const finishedMatchesInRound = allMatchesInRound.filter(m => m.status === 'finished');
  
  // Si tous les matchs du round sont terminés
  if (finishedMatchesInRound.length >= allMatchesInRound.length && allMatchesInRound.length > 0) {
    const winners = finishedMatchesInRound
      .filter(m => m.winner)
      .map(m => m.winner.toString());

    // Si il n'y a qu'un seul gagnant, c'est la finale et le tournoi est terminé
    // Vérifier aussi qu'il n'y avait qu'un seul match dans ce round (pour éviter les faux positifs)
    const matchesWithWinners = finishedMatchesInRound.filter(m => m.winner);
    if (winners.length === 1 && matchesWithWinners.length === 1) {
      console.log(`[advanceBracket] Tournament finished! Winner: ${winners[0]}`);
      // Mettre l'événement en statut "completed"
      event.status = 'completed';
      await event.save();
      
      // Emit socket event for real-time sync
      try {
        const { emitToAll } = await import('./socket.service.js');
        const populatedEvent = await Event.findById(eventId)
          .populate('game', 'name logo formats')
          .populate('createdBy', 'firstName lastName');
        emitToAll('event:updated', { eventId: eventId.toString(), event: populatedEvent });
      } catch (socketError) {
        console.warn('⚠️  Erreur lors de l\'émission Socket.io pour événement terminé:', socketError.message);
      }
      
      // Envoyer des notifications aux équipes participantes
      try {
        const populatedEvent = await Event.findById(eventId)
          .populate('game', 'name');
        
        const winnerTeam = await Team.findById(winners[0])
          .populate('members', '_id firstName lastName gamertag')
          .populate('captain', '_id firstName lastName gamertag');
        
        // Notification au gagnant
        if (winnerTeam) {
          await notifyTeamMembers(winnerTeam._id, {
            type: 'event_completed',
            title: '🏆 Champion du tournoi !',
            message: `Félicitations ! Votre équipe "${winnerTeam.name}" a remporté le tournoi "${populatedEvent.name}" !`,
            relatedEntity: {
              entityType: 'event',
              entityId: eventId
            }
          });
        }
        
        // Notifier toutes les équipes inscrites que le tournoi est terminé
        const registrations = await EventRegistration.find({
          event: eventId,
          status: 'ACCEPTED'
        }).populate('team', 'name members captain');
        
        for (const registration of registrations) {
          const team = await Team.findById(registration.team._id || registration.team)
            .populate('members', '_id firstName lastName gamertag')
            .populate('captain', '_id firstName lastName gamertag');
          
          if (team && team._id.toString() !== winners[0].toString()) {
            await notifyTeamMembers(team._id, {
              type: 'event_completed',
              title: 'Tournoi terminé',
              message: `Le tournoi "${populatedEvent.name}" est terminé. Le gagnant est "${winnerTeam?.name || 'l\'équipe gagnante'}" !`,
              relatedEntity: {
                entityType: 'event',
                entityId: eventId
              }
            });
          }
        }
      } catch (notificationError) {
        console.warn('⚠️  Erreur lors de la création des notifications de tournoi terminé:', notificationError.message);
      }
      
      return;
    }
    
    if (winners.length < 2) {
      console.log(`[advanceBracket] Not enough winners (${winners.length}) to create next round`);
      return;
    }

    const nextRoundNumber = currentRound + 1;

    // Vérifier si le round suivant existe déjà
    let nextRound = event.bracket.rounds.find(r => r.roundNumber === nextRoundNumber);
    
    if (!nextRound) {
      // Créer les matchs du round suivant
      const nextRoundMatches = [];
      for (let i = 0; i < winners.length; i += 2) {
        if (i + 1 < winners.length) {
          nextRoundMatches.push({
            team1: winners[i],
            team2: winners[i + 1]
          });
        } else {
          // Bye (équipe passe automatiquement)
          nextRoundMatches.push({
            team1: winners[i],
            team2: null,
            winner: winners[i]
          });
        }
      }
      
      // Si après avoir créé les matchs, il n'y a qu'un seul match réel (sans bye), 
      // le prochain round sera la finale - mais on le laisse se créer normalement
      // et il sera détecté comme finale quand il sera terminé

      // Ajouter le round au bracket
      nextRound = {
        roundNumber: nextRoundNumber,
        matches: nextRoundMatches
      };
      event.bracket.rounds.push(nextRound);
      await event.save();

      // Créer les documents Match pour le round suivant
      const scheduledTime = event.startDate || new Date();
      const baseTime = new Date(scheduledTime.getTime() + (nextRoundNumber * 24 * 60 * 60 * 1000)); // +1 jour par round

      const newMatchDocuments = [];
      for (let i = 0; i < nextRoundMatches.length; i++) {
        const bracketMatch = nextRoundMatches[i];
        
        // Ignorer les matchs avec bye (team2 null)
        if (!bracketMatch.team2) {
          continue;
        }

        const teamsArray = [bracketMatch.team1, bracketMatch.team2];
        
        const newMatch = await Match.create({
          event: eventId,
          teams: teamsArray,
          scheduledTime: new Date(baseTime.getTime() + (i * 60 * 60 * 1000)), // Espacer les matchs d'1 heure
          status: 'upcoming',
          score: {
            team1: 0,
            team2: 0
          },
          bracketPosition: {
            round: nextRoundNumber,
            matchNumber: i + 1,
            bracketSide: 'single'
          },
          winner: bracketMatch.winner || null
        });
        
        newMatchDocuments.push(newMatch);
      }

      console.log(`[advanceBracket] ✓ Created round ${nextRoundNumber} with ${nextRoundMatches.length} match(es)`);
      
      // Envoyer des notifications pour le nouveau round et les nouveaux matchs
      try {
        const populatedEvent = await Event.findById(eventId)
          .populate('game', 'name');
        
        // Notifier toutes les équipes qui avancent au round suivant
        for (const newMatchDoc of newMatchDocuments) {
          const populatedMatch = await Match.findById(newMatchDoc._id)
            .populate('teams', 'name logo captain members');
          
          for (const team of populatedMatch.teams) {
            const opponentTeam = populatedMatch.teams.find(t => t._id.toString() !== team._id.toString());
            await notifyTeamMembers(team._id, {
              type: 'next_round_created',
              title: 'Nouveau round - Match programmé',
              message: `Félicitations ! Votre équipe "${team.name}" a avancé au round ${nextRoundNumber} du tournoi "${populatedEvent.name}". Votre prochain match sera contre "${opponentTeam?.name || 'l\'adversaire'}" le ${new Date(populatedMatch.scheduledTime).toLocaleDateString('fr-FR')} à ${new Date(populatedMatch.scheduledTime).toLocaleTimeString('fr-FR')}.`,
              relatedEntity: {
                entityType: 'match',
                entityId: newMatchDoc._id
              }
            });
          }
        }
      } catch (notificationError) {
        console.warn('⚠️  Erreur lors de la création des notifications de nouveau round:', notificationError.message);
      }
    }
  }
};