import Event from '../models/event.model.js';
import Game from '../models/game.model.js';
import Team from '../models/team.model.js';
import EventRegistration from '../models/eventRegistration.model.js';
import { initDefaultAdmin } from './initDefaultAdmin.js';
import * as eventService from '../services/event.service.js';

/**
 * Default events to create on server startup
 * Ces événements ont différents statuts pour faciliter les tests
 */
const DEFAULT_EVENTS = [
  // 1. Événement avec inscriptions OUVERTES (inscriptions en cours)
  {
    name: 'Tournoi Valorant - Inscriptions Ouvertes',
    gameName: 'Valorant',
    format: '5v5',
    description: 'Tournoi Valorant avec inscriptions actuellement ouvertes. Rejoignez-nous !',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    status: 'open',
    maxTeams: 16,
    locationType: 'online',
    // Inscriptions : commencées il y a 2 jours, finissent dans 3 jours
    registrationStartDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 2);
      date.setHours(10, 0, 0, 0);
      return date;
    },
    registrationEndDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 3);
      date.setHours(18, 0, 0, 0);
      return date;
    },
    // Événement : commence dans 5 jours, finit dans 6 jours
    startDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 5);
      date.setHours(14, 0, 0, 0);
      return date;
    },
    endDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 6);
      date.setHours(20, 0, 0, 0);
      return date;
    }
  },

  // 2. Événement avec inscriptions qui COMMENCENT PLUS TARD (pas encore ouvert)
  {
    name: 'Championnat Rocket League 3v3 - Inscriptions Bientôt',
    gameName: 'Rocket League',
    format: '3v3',
    description: 'Championnat Rocket League en format 3v3. Les inscriptions commenceront bientôt !',
    rules: 'Règles compétitives standard - 3 joueurs par équipe',
    status: 'draft',
    maxTeams: 8,
    locationType: 'online',
    // Inscriptions : commencent dans 2 jours, finissent dans 7 jours
    registrationStartDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 2);
      date.setHours(9, 0, 0, 0);
      return date;
    },
    registrationEndDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 7);
      date.setHours(18, 0, 0, 0);
      return date;
    },
    // Événement : commence dans 10 jours, finit dans 11 jours
    startDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 10);
      date.setHours(15, 0, 0, 0);
      return date;
    },
    endDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 11);
      date.setHours(22, 0, 0, 0);
      return date;
    }
  },

  // 3. Événement avec inscriptions FERMÉES mais événement pas encore commencé
  {
    name: 'Ligue Rainbow Six Siege - Inscriptions Fermées',
    gameName: 'Rainbow Six Siege',
    format: '5v5',
    description: 'Ligue Rainbow Six Siege. Les inscriptions sont fermées mais l\'événement n\'a pas encore commencé.',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    status: 'registration_closed',
    maxTeams: 12,
    locationType: 'physical',
    address: 'Centre de compétition esports, Montréal',
    // Inscriptions : ont commencé il y a 10 jours, ont fini il y a 2 jours
    registrationStartDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 10);
      date.setHours(10, 0, 0, 0);
      return date;
    },
    registrationEndDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 2);
      date.setHours(18, 0, 0, 0);
      return date;
    },
    // Événement : commence dans 3 jours, finit dans 4 jours
    startDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 3);
      date.setHours(16, 0, 0, 0);
      return date;
    },
    endDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 4);
      date.setHours(21, 0, 0, 0);
      return date;
    }
  },

  // 4. Événement EN COURS (in_progress) avec 4 équipes inscrites (maxTeams = 4)
  {
    name: 'Tournoi Valorant - En Cours (4 équipes)',
    gameName: 'Valorant',
    format: '5v5',
    description: 'Tournoi Valorant actuellement en cours avec 4 équipes inscrites. Suivez les matchs en direct !',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    status: 'in_progress',
    maxTeams: 4,
    locationType: 'online',
    // Inscriptions : ont commencé il y a 15 jours, ont fini il y a 5 jours
    registrationStartDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 15);
      date.setHours(10, 0, 0, 0);
      return date;
    },
    registrationEndDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 5);
      date.setHours(18, 0, 0, 0);
      return date;
    },
    // Événement : commence aujourd'hui, finit demain (toujours à jour pour la démo)
    startDate: () => {
      const date = new Date();
      date.setHours(14, 0, 0, 0);
      return date;
    },
    endDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 1);
      date.setHours(20, 0, 0, 0);
      return date;
    },
    // Indicateur pour inscrire automatiquement 4 équipes
    autoRegisterTeams: true,
    numberOfTeamsToRegister: 4,
    // Générer automatiquement le bracket après l'inscription des équipes
    autoGenerateBracket: true
  },

  // 5. Événement TERMINÉ (completed)
  {
    name: 'Championnat Overwatch 2 - Terminé',
    gameName: 'Overwatch 2',
    format: '5v5',
    description: 'Championnat Overwatch 2 qui s\'est terminé récemment. Consultez les résultats !',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    status: 'completed',
    maxTeams: 8,
    locationType: 'online',
    // Inscriptions : ont commencé il y a 30 jours, ont fini il y a 20 jours
    registrationStartDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 30);
      date.setHours(10, 0, 0, 0);
      return date;
    },
    registrationEndDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 20);
      date.setHours(18, 0, 0, 0);
      return date;
    },
    // Événement : a commencé il y a 10 jours, s'est terminé il y a 5 jours
    startDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 10);
      date.setHours(14, 0, 0, 0);
      return date;
    },
    endDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 5);
      date.setHours(20, 0, 0, 0);
      return date;
    }
  },

  // 6. Événement en DRAFT (brouillon)
  {
    name: 'Tournoi Rocket League 1v1 - Brouillon',
    gameName: 'Rocket League',
    format: '1v1',
    description: 'Tournoi Rocket League en format 1v1. Événement en préparation.',
    rules: 'Règles compétitives standard - 1 joueur par équipe',
    status: 'draft',
    maxTeams: 32,
    locationType: 'online',
    // Inscriptions : commencent dans 20 jours, finissent dans 25 jours
    registrationStartDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 20);
      date.setHours(9, 0, 0, 0);
      return date;
    },
    registrationEndDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 25);
      date.setHours(18, 0, 0, 0);
      return date;
    },
    // Événement : commence dans 30 jours, finit dans 31 jours
    startDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 30);
      date.setHours(15, 0, 0, 0);
      return date;
    },
    endDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 31);
      date.setHours(22, 0, 0, 0);
      return date;
    }
  },

  // 7. Événement avec inscriptions qui se terminent DEMAIN
  {
    name: 'Dernière Chance Valorant - Inscriptions se terminent demain',
    gameName: 'Valorant',
    format: '5v5',
    description: 'Dernière chance pour s\'inscrire ! Les inscriptions se terminent demain.',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    status: 'open',
    maxTeams: 16,
    locationType: 'online',
    // Inscriptions : ont commencé il y a 5 jours, se terminent demain
    registrationStartDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 5);
      date.setHours(10, 0, 0, 0);
      return date;
    },
    registrationEndDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 1);
      date.setHours(23, 59, 59, 999);
      return date;
    },
    // Événement : commence dans 4 jours, finit dans 5 jours
    startDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 4);
      date.setHours(14, 0, 0, 0);
      return date;
    },
    endDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 5);
      date.setHours(20, 0, 0, 0);
      return date;
    }
  },

  // 8. Événement en PRÉSENTIEL avec coordonnées géographiques (pour tester la géolocalisation)
  {
    name: 'Tournoi League of Legends - Montréal',
    gameName: 'League of Legends',
    format: '5v5',
    description: 'Tournoi League of Legends en présentiel à Montréal. Événement physique avec coordonnées GPS pour tester la géolocalisation.',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    status: 'open',
    maxTeams: 16,
    locationType: 'physical',
    address: 'Centre Bell, 1909 Avenue des Canadiens-de-Montréal, Montréal, QC H3B 5E8',
    // Coordonnées de Montréal (Centre Bell) : longitude, latitude
    coordinates: [-73.5673, 45.4960],
    // Inscriptions : ont commencé il y a 3 jours, finissent dans 7 jours
    registrationStartDate: () => {
      const date = new Date();
      date.setDate(date.getDate() - 3);
      date.setHours(10, 0, 0, 0);
      return date;
    },
    registrationEndDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 7);
      date.setHours(18, 0, 0, 0);
      return date;
    },
    // Événement : commence dans 10 jours, finit dans 11 jours
    startDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 10);
      date.setHours(14, 0, 0, 0);
      return date;
    },
    endDate: () => {
      const date = new Date();
      date.setDate(date.getDate() + 11);
      date.setHours(22, 0, 0, 0);
      return date;
    }
  }
];

/**
 * Initialize default events on server startup
 */
export const initDefaultEvents = async () => {
  try {
    // Get or create admin user
    const admin = await initDefaultAdmin();

    if (!admin) {
      return;
    }

    let createdCount = 0;
    let skippedCount = 0;

    for (const eventData of DEFAULT_EVENTS) {
      // Check if event already exists by name
      const existingEvent = await Event.findOne({ name: eventData.name });

      if (existingEvent) {
        skippedCount++;
        continue;
      }

      // Find the game by name
      const game = await Game.findOne({ name: eventData.gameName, isActive: true });

      if (!game) {
        console.warn(`⚠️  Game "${eventData.gameName}" not found, skipping event "${eventData.name}"`);
        skippedCount++;
        continue;
      }

      // Calculate dates using the functions
      const registrationStartDate = eventData.registrationStartDate();
      const registrationEndDate = eventData.registrationEndDate();
      const startDate = eventData.startDate();
      const endDate = eventData.endDate();

      // Create the event
      const event = await Event.create({
        name: eventData.name,
        game: game._id,
        format: eventData.format,
        description: eventData.description,
        rules: eventData.rules,
        status: eventData.status,
        maxTeams: eventData.maxTeams,
        location: {
          type: eventData.locationType,
          address: eventData.address || undefined,
          coordinates: {
            type: 'Point',
            // Utiliser les coordonnées fournies si disponibles, sinon [0, 0]
            coordinates: eventData.coordinates || [0, 0]
          }
        },
        registrationStartDate,
        registrationEndDate,
        startDate,
        endDate,
        createdBy: admin._id
      });

      // Si autoRegisterTeams est activé, inscrire automatiquement des équipes
      if (eventData.autoRegisterTeams && eventData.numberOfTeamsToRegister) {
        try {
          const User = (await import('../models/user.model.js')).default;
          
          // Trouver les équipes actives pour ce jeu
          let teams = await Team.find({
            game: game._id,
            status: 'active'
          }).populate('captain', '_id').populate('members', '_id');

          console.log(`  [${eventData.name}] Found ${teams.length} existing teams for ${game.name}`);

          // Si on n'a pas assez d'équipes, créer des équipes temporaires avec des membres disponibles
          if (teams.length < eventData.numberOfTeamsToRegister) {
            const teamsNeeded = eventData.numberOfTeamsToRegister - teams.length;
            console.log(`  [${eventData.name}] Need to create ${teamsNeeded} additional teams`);
            
            // Trouver tous les utilisateurs disponibles (members et captains qui ne sont pas déjà dans une équipe active pour ce jeu)
            const allUsers = await User.find({ 
              role: { $in: ['member', 'captain'] } 
            });
            
            const teamsForGame = await Team.find({ game: game._id, status: 'active' }).populate('members', '_id').populate('captain', '_id');
            const usersInTeams = new Set();
            teamsForGame.forEach(team => {
              if (team.captain && team.captain._id) {
                usersInTeams.add(team.captain._id.toString());
              }
              team.members.forEach(member => {
                if (member && member._id) {
                  usersInTeams.add(member._id.toString());
                }
              });
            });

            const availableUsers = allUsers.filter(user => 
              !usersInTeams.has(user._id.toString())
            );

            console.log(`  [${eventData.name}] Found ${availableUsers.length} available users`);

            // Créer des équipes temporaires avec des membres disponibles
            // Le nombre de membres requis dépend du format de l'événement
            const getRequiredPlayersFromFormat = (format) => {
              const formatMap = {
                '1v1': 1,
                '2v2': 2,
                '3v3': 3,
                '4v4': 4,
                '5v5': 5,
                'BATTLE_ROYALE': 100
              };
              return formatMap[format] || 5;
            };
            
            const requiredPlayers = getRequiredPlayersFromFormat(eventData.format);
            const usersPerTeam = requiredPlayers; // 1 capitaine + (requiredPlayers - 1) membres
            
            // Calculer combien d'équipes on peut créer avec les utilisateurs disponibles
            const maxTeamsPossible = Math.floor(availableUsers.length / usersPerTeam);
            const teamsToCreate = Math.min(teamsNeeded, maxTeamsPossible);
            
            console.log(`  [${eventData.name}] Format: ${eventData.format}, requires ${requiredPlayers} players per team`);
            console.log(`  [${eventData.name}] Can create up to ${maxTeamsPossible} teams with ${availableUsers.length} users (need ${teamsNeeded})`);
            
            if (teamsToCreate < teamsNeeded) {
              console.warn(`  [${eventData.name}] Warning: Cannot create ${teamsNeeded} teams. Only ${teamsToCreate} teams can be created with ${availableUsers.length} users for format ${eventData.format}`);
            }
            
            let userIndex = 0;
            for (let i = 0; i < teamsToCreate && userIndex + usersPerTeam <= availableUsers.length; i++) {
              const captain = availableUsers[userIndex];
              const remainingUsers = availableUsers.length - userIndex - 1; // -1 pour le capitaine
              const membersNeeded = requiredPlayers - 1; // -1 car le capitaine compte déjà
              
              // On doit avoir assez de membres pour le format requis
              if (captain && remainingUsers >= membersNeeded) {
                // Prendre exactement le nombre de membres requis pour le format
                const teamMembers = availableUsers.slice(userIndex + 1, userIndex + 1 + membersNeeded);
                
                // Changer le rôle du capitaine si nécessaire
                if (captain.role === 'member') {
                  captain.role = 'captain';
                  await captain.save();
                }
                
                const tempTeam = await Team.create({
                  name: `Équipe Test ${teams.length + i + 1} - ${game.name}`,
                  game: game._id,
                  captain: captain._id,
                  members: teamMembers.map(m => m._id),
                  status: 'active',
                  description: `Équipe créée automatiquement pour l'événement "${eventData.name}"`
                });
                
                const populatedTeam = await Team.findById(tempTeam._id).populate('captain', '_id').populate('members', '_id');
                teams.push(populatedTeam);
                console.log(`  [${eventData.name}] Created team: ${tempTeam.name} with ${requiredPlayers} members (1 captain + ${membersNeeded} members) for format ${eventData.format}`);
                
                // Avancer l'index : capitaine (1) + membres requis
                userIndex += usersPerTeam;
              } else {
                console.warn(`  [${eventData.name}] Not enough users to create team ${i + 1}. Need ${usersPerTeam} users (1 captain + ${membersNeeded} members), have ${availableUsers.length - userIndex}`);
                break;
              }
            }
          }

          // S'assurer qu'on a exactement le nombre d'équipes requis
          if (teams.length < eventData.numberOfTeamsToRegister) {
            console.warn(`  [${eventData.name}] Warning: Only ${teams.length} teams available, but ${eventData.numberOfTeamsToRegister} are needed`);
          }

          let registrationsCreated = 0;
          const teamsToRegister = teams.slice(0, eventData.numberOfTeamsToRegister);
          console.log(`  [${eventData.name}] Registering ${teamsToRegister.length} teams`);
          
          for (const team of teamsToRegister) {
            // Vérifier si l'équipe n'est pas déjà inscrite
            const existingRegistration = await EventRegistration.findOne({
              event: event._id,
              team: team._id
            });

            if (existingRegistration) {
              continue;
            }

            // Calculer le nombre de membres participants nécessaires selon le format
            const getRequiredPlayersFromFormat = (format) => {
              const formatMap = {
                '1v1': 1,
                '2v2': 2,
                '3v3': 3,
                '4v4': 4,
                '5v5': 5,
                'BATTLE_ROYALE': 100
              };
              return formatMap[format] || 5;
            };

            const requiredPlayers = getRequiredPlayersFromFormat(eventData.format);
            const captainId = team.captain._id.toString();
            const allMembers = team.members.map(m => m._id.toString());
            
            // Sélectionner les membres participants (capitaine + autres membres jusqu'à atteindre le nombre requis)
            const participatingMemberIds = [];
            for (const memberId of allMembers) {
              if (memberId !== captainId && participatingMemberIds.length < requiredPlayers - 1) {
                participatingMemberIds.push(memberId);
              }
            }

            // Créer l'inscription
            await EventRegistration.create({
              event: event._id,
              team: team._id,
              registeredBy: captainId,
              status: 'ACCEPTED', // Accepter automatiquement les inscriptions
              participatingMembers: [captainId, ...participatingMemberIds]
            });

            registrationsCreated++;
          }

          if (registrationsCreated > 0) {
            console.log(`  ✓ ${registrationsCreated} équipe(s) inscrite(s) automatiquement à "${eventData.name}"`);
            
            // Vérifier le nombre d'inscriptions avant de générer le bracket
            const finalRegistrations = await EventRegistration.find({
              event: event._id,
              status: { $in: ['ACCEPTED', 'PENDING'] }
            });
            
            console.log(`  [${eventData.name}] Total registrations before bracket generation: ${finalRegistrations.length}`);
            
            // Si l'événement a le flag autoGenerateBracket, générer le bracket automatiquement
            if (eventData.autoGenerateBracket && finalRegistrations.length >= 2) {
              try {
                await eventService.generateBracket(event._id);
                console.log(`  ✓ Bracket et matchs générés automatiquement pour "${eventData.name}" avec ${finalRegistrations.length} équipes`);
              } catch (bracketError) {
                console.warn(`⚠️  Erreur lors de la génération automatique du bracket pour "${eventData.name}":`, bracketError.message);
              }
            } else if (eventData.autoGenerateBracket && finalRegistrations.length < 2) {
              console.warn(`  [${eventData.name}] Cannot generate bracket: need at least 2 teams, have ${finalRegistrations.length}`);
            }
          } else {
            console.warn(`  [${eventData.name}] No teams were registered`);
          }
        } catch (registrationError) {
          console.warn(`⚠️  Erreur lors de l'inscription automatique des équipes pour "${eventData.name}":`, registrationError.message);
        }
      }

      createdCount++;
    }

    if (createdCount > 0 || skippedCount > 0) {
      console.log(`📅 Events: ${createdCount} created, ${skippedCount} already exist or skipped`);
    }
  } catch (error) {
    console.error('❌ Error initializing default events:', error);
    // Don't throw - allow server to start even if events initialization fails
  }
};

