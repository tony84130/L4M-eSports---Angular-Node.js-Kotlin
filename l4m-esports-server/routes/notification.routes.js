import express from 'express';
import { authenticate } from '../middlewares/auth.middleware.js';
import {
  getAllNotifications,
  getNotificationById,
  getUnreadCount,
  markAsRead,
  markAllAsRead,
  deleteNotification
} from '../controllers/notification.controller.js';

const router = express.Router();

/**
 * @route   GET /api/notifications
 * @desc    Get all notifications for the authenticated user
 * @access  Private
 */
router.get('/', authenticate, getAllNotifications);

/**
 * @route   GET /api/notifications/unread-count
 * @desc    Get unread notifications count for the authenticated user
 * @access  Private
 */
router.get('/unread-count', authenticate, getUnreadCount);

/**
 * @route   GET /api/notifications/:id
 * @desc    Get a specific notification by ID
 * @access  Private
 */
router.get('/:id', authenticate, getNotificationById);

/**
 * @route   PUT /api/notifications/:id/read
 * @desc    Mark a notification as read
 * @access  Private
 */
router.put('/:id/read', authenticate, markAsRead);

/**
 * @route   PUT /api/notifications/read-all
 * @desc    Mark all notifications as read
 * @access  Private
 */
router.put('/read-all', authenticate, markAllAsRead);

/**
 * @route   DELETE /api/notifications/:id
 * @desc    Delete a notification
 * @access  Private
 */
router.delete('/:id', authenticate, deleteNotification);

export default router;

