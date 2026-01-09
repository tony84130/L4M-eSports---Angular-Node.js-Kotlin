import User from '../models/user.model.js';
import Team from '../models/team.model.js';
import Game from '../models/game.model.js';

/**
 * Initialize default users for testing
 * Creates users with Marvel-themed names and different roles
 * All users have password: "admin123"
 * For captains, automatically creates a team with members and a game
 * Note: Database connection should already be established before calling this function
 */
export const initDefaultUsers = async () => {
  try {
    // Récupérer tous les jeux actifs disponibles
    const availableGames = await Game.find({ isActive: true });
    if (availableGames.length === 0) {
      console.warn('⚠️  No active games found. Teams cannot be created for captains.');
      return;
    }

    const defaultUsers = [
      // Admin users
      {
        email: 'tony.stark@marvel.com',
        password: 'admin123',
        firstName: 'Tony',
        lastName: 'Stark',
        gamertag: 'IronMan',
        role: 'admin'
      },
      {
        email: 'steve.rogers@marvel.com',
        password: 'admin123',
        firstName: 'Steve',
        lastName: 'Rogers',
        gamertag: 'CaptainAmerica',
        role: 'admin'
      },
      
      // Captain users (avec jeu assigné et membres)
      {
        email: 'peter.parker@marvel.com',
        password: 'admin123',
        firstName: 'Peter',
        lastName: 'Parker',
        gamertag: 'SpiderMan',
        role: 'captain',
        twitchUsername: 'spiderman_streams',
        teamName: 'Spider Squad',
        gameName: 'Valorant', // Assigner un jeu spécifique
        memberGamertags: ['Hulk', 'Hawkeye', 'TheVision', 'Falcon'] // 4 membres + capitaine = 5 total
      },
      {
        email: 'natasha.romanoff@marvel.com',
        password: 'admin123',
        firstName: 'Natasha',
        lastName: 'Romanoff',
        gamertag: 'BlackWidow',
        role: 'captain',
        twitchUsername: 'blackwidow_gaming',
        teamName: 'Widow\'s Web',
        gameName: 'Rainbow Six Siege',
        memberGamertags: ['ScarletWitch', 'AntMan', 'WarMachine', 'WinterSoldier'] // 4 membres + capitaine = 5 total
      },
      {
        email: 'thor.odinson@marvel.com',
        password: 'admin123',
        firstName: 'Thor',
        lastName: 'Odinson',
        gamertag: 'GodOfThunder',
        role: 'captain',
        teamName: 'Thunder Force',
        gameName: 'League of Legends',
        memberGamertags: ['Falcon', 'WinterSoldier', 'DrStrange', 'BlackPanther']
      },
      
      // Member users
      {
        email: 'bruce.banner@marvel.com',
        password: 'admin123',
        firstName: 'Bruce',
        lastName: 'Banner',
        gamertag: 'Hulk',
        role: 'member',
        twitchUsername: 'hulk_smash'
      },
      {
        email: 'clint.barton@marvel.com',
        password: 'admin123',
        firstName: 'Clint',
        lastName: 'Barton',
        gamertag: 'Hawkeye',
        role: 'member'
      },
      {
        email: 'wanda.maximoff@marvel.com',
        password: 'admin123',
        firstName: 'Wanda',
        lastName: 'Maximoff',
        gamertag: 'ScarletWitch',
        role: 'member',
        twitchUsername: 'scarletwitch_magic'
      },
      {
        email: 'vision@marvel.com',
        password: 'admin123',
        firstName: 'Vision',
        lastName: 'Android',
        gamertag: 'TheVision',
        role: 'member'
      },
      {
        email: 'scott.lang@marvel.com',
        password: 'admin123',
        firstName: 'Scott',
        lastName: 'Lang',
        gamertag: 'AntMan',
        role: 'member',
        twitchUsername: 'antman_tiny'
      },
      {
        email: 'james.rhodes@marvel.com',
        password: 'admin123',
        firstName: 'James',
        lastName: 'Rhodes',
        gamertag: 'WarMachine',
        role: 'member'
      },
      {
        email: 'sam.wilson@marvel.com',
        password: 'admin123',
        firstName: 'Sam',
        lastName: 'Wilson',
        gamertag: 'Falcon',
        role: 'member',
        twitchUsername: 'falcon_fly'
      },
      {
        email: 'bucky.barnes@marvel.com',
        password: 'admin123',
        firstName: 'Bucky',
        lastName: 'Barnes',
        gamertag: 'WinterSoldier',
        role: 'member'
      },
      {
        email: 'stephen.strange@marvel.com',
        password: 'admin123',
        firstName: 'Stephen',
        lastName: 'Strange',
        gamertag: 'DrStrange',
        role: 'member',
        twitchUsername: 'drstrange_mystic'
      },
      {
        email: 'tchalla@marvel.com',
        password: 'admin123',
        firstName: 'T\'Challa',
        lastName: 'Udaku',
        gamertag: 'BlackPanther',
        role: 'member'
      },
      
      // Ajout de plus d'utilisateurs membres pour avoir assez de personnes pour créer 4 équipes de 5 membres
      {
        email: 'loki.odinson@marvel.com',
        password: 'admin123',
        firstName: 'Loki',
        lastName: 'Odinson',
        gamertag: 'LokiTrickster',
        role: 'member'
      },
      {
        email: 'gamora.zen@marvel.com',
        password: 'admin123',
        firstName: 'Gamora',
        lastName: 'Zen-Whoberi',
        gamertag: 'GamoraWarrior',
        role: 'member'
      },
      {
        email: 'drax.destroyer@marvel.com',
        password: 'admin123',
        firstName: 'Drax',
        lastName: 'The Destroyer',
        gamertag: 'DraxDestroyer',
        role: 'member'
      },
      {
        email: 'rocket.raccoon@marvel.com',
        password: 'admin123',
        firstName: 'Rocket',
        lastName: 'Raccoon',
        gamertag: 'RocketRaccoon',
        role: 'member'
      },
      {
        email: 'groot@marvel.com',
        password: 'admin123',
        firstName: 'Groot',
        lastName: 'Flora Colossus',
        gamertag: 'IAmGroot',
        role: 'member'
      },
      {
        email: 'mantis@marvel.com',
        password: 'admin123',
        firstName: 'Mantis',
        lastName: 'Empath',
        gamertag: 'MantisEmpath',
        role: 'member'
      },
      {
        email: 'nebula@marvel.com',
        password: 'admin123',
        firstName: 'Nebula',
        lastName: 'Luphomoid',
        gamertag: 'NebulaCyborg',
        role: 'member'
      },
      {
        email: 'yondu.udonta@marvel.com',
        password: 'admin123',
        firstName: 'Yondu',
        lastName: 'Udonta',
        gamertag: 'YonduArrow',
        role: 'member'
      },
      {
        email: 'kraglin.obfonteri@marvel.com',
        password: 'admin123',
        firstName: 'Kraglin',
        lastName: 'Obfonteri',
        gamertag: 'KraglinRavager',
        role: 'member'
      },
      {
        email: 'cosmo.spacedog@marvel.com',
        password: 'admin123',
        firstName: 'Cosmo',
        lastName: 'The Spacedog',
        gamertag: 'CosmoDog',
        role: 'member'
      },
      {
        email: 'adam.warlock@marvel.com',
        password: 'admin123',
        firstName: 'Adam',
        lastName: 'Warlock',
        gamertag: 'AdamWarlock',
        role: 'member'
      },
      {
        email: 'phyla.vell@marvel.com',
        password: 'admin123',
        firstName: 'Phyla',
        lastName: 'Vell',
        gamertag: 'PhylaQuasar',
        role: 'member'
      },
      {
        email: 'moondragon@marvel.com',
        password: 'admin123',
        firstName: 'Heather',
        lastName: 'Douglas',
        gamertag: 'Moondragon',
        role: 'member'
      },
      {
        email: 'richard.rider@marvel.com',
        password: 'admin123',
        firstName: 'Richard',
        lastName: 'Rider',
        gamertag: 'NovaPrime',
        role: 'member'
      },
      {
        email: 'sam.alexander@marvel.com',
        password: 'admin123',
        firstName: 'Sam',
        lastName: 'Alexander',
        gamertag: 'NovaSam',
        role: 'member'
      },
      {
        email: 'carol.danvers@marvel.com',
        password: 'admin123',
        firstName: 'Carol',
        lastName: 'Danvers',
        gamertag: 'CaptainMarvel',
        role: 'member'
      },
      {
        email: 'monica.rambeau@marvel.com',
        password: 'admin123',
        firstName: 'Monica',
        lastName: 'Rambeau',
        gamertag: 'Photon',
        role: 'member'
      },
      {
        email: 'kamala.khan@marvel.com',
        password: 'admin123',
        firstName: 'Kamala',
        lastName: 'Khan',
        gamertag: 'MsMarvel',
        role: 'member'
      },
      {
        email: 'jennifer.walters@marvel.com',
        password: 'admin123',
        firstName: 'Jennifer',
        lastName: 'Walters',
        gamertag: 'SheHulk',
        role: 'member'
      },
      {
        email: 'jessica.jones@marvel.com',
        password: 'admin123',
        firstName: 'Jessica',
        lastName: 'Jones',
        gamertag: 'JessicaJones',
        role: 'member'
      },
      {
        email: 'luke.cage@marvel.com',
        password: 'admin123',
        firstName: 'Luke',
        lastName: 'Cage',
        gamertag: 'PowerMan',
        role: 'member'
      },
      {
        email: 'danny.rand@marvel.com',
        password: 'admin123',
        firstName: 'Danny',
        lastName: 'Rand',
        gamertag: 'IronFist',
        role: 'member'
      },
      {
        email: 'matt.murdock@marvel.com',
        password: 'admin123',
        firstName: 'Matt',
        lastName: 'Murdock',
        gamertag: 'Daredevil',
        role: 'member'
      },
      {
        email: 'frank.castle@marvel.com',
        password: 'admin123',
        firstName: 'Frank',
        lastName: 'Castle',
        gamertag: 'Punisher',
        role: 'member'
      }
    ];

    let createdCount = 0;
    let existingCount = 0;
    const createdUsers = new Map(); // Stocker les utilisateurs créés pour créer les équipes
    const captains = []; // Stocker les capitaines pour créer leurs équipes après

    // Étape 1: Créer tous les utilisateurs
    for (const userData of defaultUsers) {
      try {
        // Check if user already exists by email
        const existingUser = await User.findOne({ email: userData.email.toLowerCase() });
        
        if (existingUser) {
          existingCount++;
          createdUsers.set(userData.gamertag, existingUser);
          if (userData.role === 'captain') {
            captains.push({ user: existingUser, userData });
          }
          continue;
        }

        // Create new user
        const newUser = await User.create({
          email: userData.email.toLowerCase(),
          password: userData.password,
          firstName: userData.firstName,
          lastName: userData.lastName,
          gamertag: userData.gamertag,
          role: userData.role,
          twitchUsername: userData.twitchUsername || undefined
        });

        createdCount++;
        createdUsers.set(userData.gamertag, newUser);
        
        // Si c'est un capitaine, le stocker pour créer son équipe après
        if (userData.role === 'captain') {
          captains.push({ user: newUser, userData });
        }
      } catch (error) {
        // Handle duplicate gamertag or other errors
        if (error.code === 11000) {
          existingCount++;
          // Essayer de récupérer l'utilisateur existant
          const existingUser = await User.findOne({ gamertag: userData.gamertag });
          if (existingUser) {
            createdUsers.set(userData.gamertag, existingUser);
            if (userData.role === 'captain') {
              captains.push({ user: existingUser, userData });
            }
          }
        } else {
          console.error(`❌ Error creating user ${userData.gamertag}:`, error.message);
        }
      }
    }

    // Étape 2: Créer les équipes pour chaque capitaine
    let teamsCreated = 0;
    let teamsSkipped = 0;

    for (const { user: captain, userData } of captains) {
      try {
        // Vérifier si le capitaine a déjà une équipe active pour ce jeu
        const game = await Game.findOne({ name: userData.gameName, isActive: true });
        if (!game) {
          console.warn(`⚠️  Game "${userData.gameName}" not found for captain ${captain.gamertag}. Skipping team creation.`);
          continue;
        }

        // Vérifier si une équipe existe déjà pour ce capitaine et ce jeu
        const existingTeam = await Team.findOne({
          captain: captain._id,
          game: game._id,
          status: 'active'
        });

        if (existingTeam) {
          teamsSkipped++;
          continue;
        }

        // Récupérer les membres à ajouter à l'équipe
        const memberUsers = [];
        if (userData.memberGamertags && userData.memberGamertags.length > 0) {
          for (const gamertag of userData.memberGamertags) {
            const member = createdUsers.get(gamertag);
            if (member && member.role === 'member') {
              // Vérifier que le membre n'est pas déjà dans une équipe active pour ce jeu
              const isInTeam = await Team.findOne({
                game: game._id,
                members: member._id,
                status: 'active'
              });
              if (!isInTeam) {
                memberUsers.push(member._id);
              }
            }
          }
        }

        // Créer l'équipe avec le capitaine et les membres
        const team = await Team.create({
          name: userData.teamName || `${captain.gamertag}'s Team`,
          game: game._id,
          captain: captain._id,
          members: [captain._id, ...memberUsers], // Le capitaine est automatiquement ajouté
          status: 'active',
          description: `Équipe créée automatiquement pour ${captain.firstName} ${captain.lastName}`
        });

        teamsCreated++;
      } catch (error) {
        console.error(`❌ Error creating team for captain ${captain.gamertag}:`, error.message);
      }
    }

    if (createdCount > 0 || existingCount > 0) {
      console.log(`👥 Users: ${createdCount} created, ${existingCount} already exist`);
    }
    if (teamsCreated > 0 || teamsSkipped > 0) {
      console.log(`⚽ Teams: ${teamsCreated} created for captains, ${teamsSkipped} already exist`);
    }
  } catch (error) {
    console.error('❌ Error initializing default users:', error);
  }
};


