import axios from 'axios';
import env from '../config/env.js';
import { BadRequestError } from '../utils/errors.js';

const OPENAI_URL = 'https://api.openai.com/v1/chat/completions';
const MODEL = 'gpt-4o-mini';

/**
 * Build a short prompt mixing context and user question.
 */
const buildMessages = (question, context = {}) => {
  const { page, role, extra } = context || {};
  const contextParts = [];
  if (page) contextParts.push(`Page: ${page}`);
  if (role) contextParts.push(`Rôle: ${role}`);
  if (extra) contextParts.push(`Contexte: ${JSON.stringify(extra)}`);

  const contextText = contextParts.length ? `Contexte: ${contextParts.join(' | ')}` : 'Contexte: N/A';

  return [
    {
      role: 'system',
      content:
        'Tu es un assistant concis pour une appli e-sport. Réponds en français, clair, en moins de 120 mots. ' +
        "Si la question sort du périmètre de l'app, réponds que tu es limité à l'aide produit."
    },
    {
      role: 'user',
      content: `${contextText}\nQuestion: ${question}`
    }
  ];
};

export const askAssistant = async (question, context) => {
  if (!question || !question.trim()) {
    throw new BadRequestError('La question est requise');
  }
  if (!env.OPENAI_API_KEY) {
    throw new BadRequestError('OPENAI_API_KEY manquant côté serveur');
  }

  const messages = buildMessages(question, context);

  try {
    const response = await axios.post(
      OPENAI_URL,
      {
        model: MODEL,
        messages,
        max_tokens: 240,
        temperature: 0.4
      },
      {
        headers: {
          Authorization: `Bearer ${env.OPENAI_API_KEY}`,
          'Content-Type': 'application/json'
        },
        timeout: 10000
      }
    );

    const answer = response.data?.choices?.[0]?.message?.content?.trim() || 'Désolé, aucune réponse disponible.';
    return answer;
  } catch (error) {
    const status = error?.response?.status;
    const providerMessage =
      error?.response?.data?.error?.message ||
      error?.response?.data?.error?.code ||
      error?.message;
    // Log côté serveur pour debug
    console.error('AI assist error:', status, providerMessage);
    throw new BadRequestError(`Assistant indisponible (${status ?? 'n/a'}): ${providerMessage}`);
  }
};
