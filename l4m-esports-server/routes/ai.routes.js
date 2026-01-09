import express from 'express';
import { assist } from '../controllers/ai.controller.js';

const router = express.Router();

/**
 * @route   POST /api/ai/assist
 * @desc    Proxy vers le fournisseur d'IA (OpenAI) pour de l'aide contextuelle
 * @access  Public (clé serveur requise)
 */
router.post('/assist', assist);

export default router;
