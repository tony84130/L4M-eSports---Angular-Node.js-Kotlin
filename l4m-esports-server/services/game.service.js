import Game from '../models/game.model.js';
import { NotFoundError, BadRequestError } from '../utils/errors.js';

/**
 * Get all active games
 */
export const getAllGames = async (filters = {}) => {
  const { isActive = true, search } = filters;
  
  const query = {};
  
  if (isActive !== undefined) {
    query.isActive = isActive === 'true' || isActive === true;
  }
  
  if (search) {
    query.$or = [
      { name: { $regex: search, $options: 'i' } },
      { description: { $regex: search, $options: 'i' } }
    ];
  }
  
  const games = await Game.find(query)
    .sort({ name: 1 })
    .lean();
  
  // Convert createdBy ObjectId to string
  return games.map(game => ({
    ...game,
    createdBy: game.createdBy ? game.createdBy.toString() : null
  }));
};

/**
 * Get game by ID
 */
export const getGameById = async (id) => {
  const game = await Game.findById(id).lean();
  
  if (!game) {
    throw new NotFoundError('Jeu introuvable');
  }
  
  // Convert createdBy ObjectId to string
  return {
    ...game,
    createdBy: game.createdBy ? game.createdBy.toString() : null
  };
};

/**
 * Create a new game
 */
export const createGame = async (gameData, createdBy) => {
  // Check if game name already exists
  const existingGame = await Game.findOne({ name: gameData.name });
  if (existingGame) {
    throw new BadRequestError('Un jeu avec ce nom existe déjà');
  }
  
  const game = await Game.create({
    ...gameData,
    createdBy
  });
  
  const createdGame = await Game.findById(game._id).lean();
  
  // Convert createdBy ObjectId to string
  return {
    ...createdGame,
    createdBy: createdGame.createdBy ? createdGame.createdBy.toString() : null
  };
};

/**
 * Update a game
 */
export const updateGame = async (id, updateData) => {
  // Check if name is being updated and if it's already taken
  if (updateData.name) {
    const existingGame = await Game.findOne({ 
      name: updateData.name,
      _id: { $ne: id }
    });
    
    if (existingGame) {
      throw new BadRequestError('Un jeu avec ce nom existe déjà');
    }
  }
  
  const game = await Game.findByIdAndUpdate(
    id,
    { $set: updateData },
    { new: true, runValidators: true }
  ).lean();
  
  if (!game) {
    throw new NotFoundError('Jeu introuvable');
  }
  
  // Convert createdBy ObjectId to string
  return {
    ...game,
    createdBy: game.createdBy ? game.createdBy.toString() : null
  };
};

/**
 * Delete a game
 */
export const deleteGame = async (id) => {
  const game = await Game.findByIdAndDelete(id);
  
  if (!game) {
    throw new NotFoundError('Jeu introuvable');
  }
  
  return true;
};

