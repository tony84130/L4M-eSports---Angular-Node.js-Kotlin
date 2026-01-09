import { askAssistant } from '../services/ai.service.js';
import { BadRequestError } from '../utils/errors.js';

export const assist = async (req, res, next) => {
  try {
    const { question, context } = req.body || {};
    if (!question || !question.trim()) {
      throw new BadRequestError('La question est requise');
    }

    const answer = await askAssistant(question, context);

    res.status(200).json({
      success: true,
      data: { answer }
    });
  } catch (error) {
    next(error);
  }
};
