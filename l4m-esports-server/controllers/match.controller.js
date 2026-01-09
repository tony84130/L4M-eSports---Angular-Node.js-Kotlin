import * as matchService from '../services/match.service.js';

/**
 * Get all matches with filters
 */
export const getAllMatches = async (req, res, next) => {
  try {
    const matches = await matchService.getAllMatches(req.query);
    res.status(200).json({ success: true, data: { matches } });
  } catch (error) {
    next(error);
  }
};

/**
 * Get match by ID
 */
export const getMatchById = async (req, res, next) => {
  try {
    const match = await matchService.getMatchById(req.params.id);
    res.status(200).json({ success: true, data: { match } });
  } catch (error) {
    next(error);
  }
};

/**
 * Get matches by event ID
 */
export const getMatchesByEvent = async (req, res, next) => {
  try {
    const matches = await matchService.getMatchesByEvent(req.params.eventId);
    res.status(200).json({ success: true, data: { matches } });
  } catch (error) {
    next(error);
  }
};

/**
 * Get matches by team ID
 */
export const getMatchesByTeam = async (req, res, next) => {
  try {
    const matches = await matchService.getMatchesByTeam(req.params.teamId);
    res.status(200).json({ success: true, data: { matches } });
  } catch (error) {
    next(error);
  }
};

/**
 * Update match status
 */
export const updateMatchStatus = async (req, res, next) => {
  try {
    const { status } = req.body;
    if (!status) {
      return res.status(400).json({
        success: false,
        error: { message: 'Le statut est requis' }
      });
    }

    const match = await matchService.updateMatchStatus(
      req.params.id,
      status,
      req.user._id
    );
    res.status(200).json({
      success: true,
      message: 'Statut du match mis à jour avec succès',
      data: { match }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update match score
 */
export const updateMatchScore = async (req, res, next) => {
  try {
    const { score } = req.body;
    if (!score || typeof score.team1 !== 'number' || typeof score.team2 !== 'number') {
      return res.status(400).json({
        success: false,
        error: { message: 'Le score est requis et doit contenir team1 et team2 (nombres)' }
      });
    }

    const match = await matchService.updateMatchScore(
      req.params.id,
      score,
      req.user._id
    );
    res.status(200).json({
      success: true,
      message: 'Score du match mis à jour avec succès',
      data: { match }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Validate match result
 */
export const validateMatchResult = async (req, res, next) => {
  try {
    const match = await matchService.validateMatchResult(
      req.params.id,
      req.user._id
    );
    res.status(200).json({
      success: true,
      message: 'Résultat du match validé avec succès',
      data: { match }
    });
  } catch (error) {
    next(error);
  }
};

/**
 * Update match (e.g., scheduled time)
 */
export const updateMatch = async (req, res, next) => {
  try {
    const match = await matchService.updateMatch(
      req.params.id,
      req.body,
      req.user._id
    );
    res.status(200).json({
      success: true,
      message: 'Match mis à jour avec succès',
      data: { match }
    });
  } catch (error) {
    next(error);
  }
};

