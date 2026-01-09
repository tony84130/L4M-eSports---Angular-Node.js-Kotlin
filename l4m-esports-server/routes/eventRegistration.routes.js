import express from 'express';
import { authenticate as protect } from '../middlewares/auth.middleware.js';
import * as registrationController from '../controllers/eventRegistration.controller.js';

const router = express.Router();

// IMPORTANT : Les routes spécifiques (comme /event/:eventId) doivent être AVANT les routes dynamiques (/:id)

/**
 * @route   GET /api/event-registrations
 * @desc    Liste des inscriptions (filtres: event, team)
 */
router.get('/', protect, registrationController.getRegistrations);

/**
 * @route   GET /api/event-registrations/event/:eventId
 * @desc    Inscriptions d'un événement
 */
router.get('/event/:eventId', protect, registrationController.getRegistrationsByEvent);

/**
 * @route   GET /api/event-registrations/team/:teamId
 * @desc    Inscriptions d'une équipe
 */
router.get('/team/:teamId', protect, registrationController.getRegistrationsByTeam);

/**
 * @route   GET /api/event-registrations/:id
 * @desc    Détails d'une inscription
 */
router.get('/:id', protect, registrationController.getRegistrationById);

/**
 * @route   POST /api/event-registrations
 * @desc    Inscrire une équipe à un événement (Captain)
 */
router.post('/', protect, registrationController.createRegistration);

/**
 * @route   PUT /api/event-registrations/:id
 * @desc    Modifier inscription (Accept/Reject pour Admin, ou modif pour Captain)
 */
router.put('/:id', protect, registrationController.updateRegistration);

/**
 * @route   DELETE /api/event-registrations/:id
 * @desc    Annuler inscription (Captain)
 */
router.delete('/:id', protect, registrationController.deleteRegistration);

export default router;