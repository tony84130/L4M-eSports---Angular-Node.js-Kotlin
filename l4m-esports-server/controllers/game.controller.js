import * as gameService from '../services/game.service.js';
import { emitToAll } from '../services/socket.service.js';

/**
 * Get all games
 */
export const getAllGames = async (req, res, next) => {
  try {
    const games = await gameService.getAllGames(req.query);
    res.status(200).json({
      success: true,
      data: games
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Get game by ID
 */
export const getGameById = async (req, res, next) => {
  try {
    const { id } = req.params;
    const game = await gameService.getGameById(id);
    res.status(200).json({
      success: true,
      data: game
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Create a new game (Admin only)
 */
export const createGame = async (req, res, next) => {
  try {
    const game = await gameService.createGame(req.body, req.user._id);
    // Emit socket event for real-time sync
    emitToAll('game:created', { game });
    res.status(201).json({
      success: true,
      message: 'Game created successfully',
      data: game
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update a game (Admin only)
 */
export const updateGame = async (req, res, next) => {
  try {
    const { id } = req.params;
    const game = await gameService.updateGame(id, req.body);
    // Emit socket event for real-time sync
    emitToAll('game:updated', { gameId: id, game });
    res.status(200).json({
      success: true,
      message: 'Game updated successfully',
      data: game
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Delete a game (Admin only)
 */
export const deleteGame = async (req, res, next) => {
  try {
    const { id } = req.params;
    await gameService.deleteGame(id);
    // Emit socket event for real-time sync
    emitToAll('game:deleted', { gameId: id });
    res.status(200).json({
      success: true,
      message: 'Game deleted successfully'
    });
  } catch (error) {
    next(error);
  }
};

