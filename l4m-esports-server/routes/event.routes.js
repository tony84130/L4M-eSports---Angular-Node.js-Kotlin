import express from 'express';
import { authenticate as protect, authorize } from '../middlewares/auth.middleware.js'; 
import * as eventController from '../controllers/event.controller.js';

const router = express.Router();

// === ROUTES PUBLIQUES ===

/**
 * @route   GET /api/events
 * @desc    Get all events
 * @access  Public
 */
router.get('/', eventController.getAllEvents);

/**
 * @route   GET /api/events/nearby
 * @desc    Get events near user (Doit être AVANT /:id)
 * @access  Public
 */
router.get('/nearby', protect, eventController.getEventsNearby);

/**
 * @route   GET /api/events/:id
 * @desc    Get event details
 * @access  Public
 */
router.get('/:id', eventController.getEventById);

/**
 * @route   GET /api/events/:id/bracket
 * @desc    Get event bracket
 * @access  Public
 */
router.get('/:id/bracket', eventController.getEventBracket);


// === ROUTES ADMIN ===

/**
 * @route   POST /api/events
 * @desc    Create event
 * @access  Private (Admin)
 */
router.post('/', protect, authorize('admin'), eventController.createEvent);

/**
 * @route   PUT /api/events/:id
 * @desc    Update event
 * @access  Private (Admin)
 */
router.put('/:id', protect, authorize('admin'), eventController.updateEvent);

/**
 * @route   DELETE /api/events/:id
 * @desc    Delete event
 * @access  Private (Admin)
 */
router.delete('/:id', protect, authorize('admin'), eventController.deleteEvent);

/**
 * @route   POST /api/events/:id/generate-bracket
 * @desc    Generate tournament bracket
 * @access  Private (Admin)
 */
router.post('/:id/generate-bracket', protect, authorize('admin'), eventController.generateBracket);

export default router;