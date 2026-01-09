import Game from '../models/game.model.js';
import { initDefaultAdmin } from './initDefaultAdmin.js';

/**
 * Default games to create on server startup
 */
const DEFAULT_GAMES = [
  {
    name: 'Rainbow Six Siege',
    description: 'Jeu de tir tactique avec des matchs compétitifs en 5v5',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    formats: ['5v5'],
    isActive: true
  },
  {
    name: 'Valorant',
    description: 'Jeu FPS tactique avec des matchs compétitifs en 5v5',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    formats: ['5v5'],
    isActive: true
  },
  {
    name: 'League of Legends',
    description: 'Jeu MOBA avec des matchs compétitifs en 5v5',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    formats: ['5v5'],
    isActive: true
  },
  {
    name: 'Rocket League',
    description: 'Football avec des voitures - supporte plusieurs formats de match',
    rules: 'Règles compétitives standard',
    formats: ['1v1', '2v2', '3v3'],
    isActive: true
  },
  {
    name: 'Overwatch 2',
    description: 'Jeu FPS en équipe avec des matchs compétitifs en 5v5',
    rules: 'Règles compétitives standard - 5 joueurs par équipe',
    formats: ['5v5'],
    isActive: true
  }
];

/**
 * Initialize default games on server startup
 */
export const initDefaultGames = async () => {
  try {
    // Get or create admin user (uses env variables if available)
    const admin = await initDefaultAdmin();
    
    if (!admin) {
      return;
    }
    
    let createdCount = 0;
    let skippedCount = 0;
    
    for (const gameData of DEFAULT_GAMES) {
      // Check if game already exists
      const existingGame = await Game.findOne({ name: gameData.name });
      
      if (existingGame) {
        skippedCount++;
        continue;
      }
      
      // Create the game
      await Game.create({
        ...gameData,
        createdBy: admin._id
      });
      
      createdCount++;
    }
    
    if (createdCount > 0 || skippedCount > 0) {
      console.log(`🎮 Games: ${createdCount} created, ${skippedCount} already exist`);
    }
  } catch (error) {
    console.error('❌ Error initializing default games:', error);
    // Don't throw - allow server to start even if games initialization fails
  }
};

