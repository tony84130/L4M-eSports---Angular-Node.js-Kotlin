import Notification from '../models/notification.model.js';
import Team from '../models/team.model.js';
import { NotFoundError, ForbiddenError } from '../utils/errors.js';

/**
 * Create a notification
 */
export const createNotification = async (notificationData) => {
  const notification = await Notification.create(notificationData);
  return notification;
};

/**
 * Get all notifications for a user with filters
 */
export const getAllNotifications = async (userId, filters = {}) => {
  const { read, type, limit = 50, offset = 0 } = filters;
  
  const query = { user: userId };
  
  if (read !== undefined) {
    query.read = read === 'true' || read === true;
  }
  
  if (type) {
    query.type = type;
  }
  
  const notifications = await Notification.find(query)
    .sort({ createdAt: -1 })
    .limit(parseInt(limit))
    .skip(parseInt(offset));
  
  return notifications;
};

/**
 * Get notification by ID
 */
export const getNotificationById = async (id, userId) => {
  const notification = await Notification.findById(id);
  
  if (!notification) {
    throw new NotFoundError('Notification not found');
  }
  
  // Check if user owns this notification
  if (notification.user.toString() !== userId.toString()) {
    throw new ForbiddenError('You do not have permission to access this notification');
  }
  
  return notification;
};

/**
 * Get unread notifications count
 */
export const getUnreadCount = async (userId) => {
  const count = await Notification.countDocuments({
    user: userId,
    read: false
  });
  
  return count;
};

/**
 * Mark notification as read
 */
export const markAsRead = async (id, userId) => {
  const notification = await Notification.findById(id);
  
  if (!notification) {
    throw new NotFoundError('Notification not found');
  }
  
  // Check if user owns this notification
  if (notification.user.toString() !== userId.toString()) {
    throw new ForbiddenError('You do not have permission to modify this notification');
  }
  
  notification.read = true;
  notification.readAt = new Date();
  await notification.save();
  
  return notification;
};

/**
 * Mark all notifications as read
 */
export const markAllAsRead = async (userId) => {
  const result = await Notification.updateMany(
    { user: userId, read: false },
    { 
      read: true,
      readAt: new Date()
    }
  );
  
  return result;
};

/**
 * Delete a notification
 */
export const deleteNotification = async (id, userId) => {
  const notification = await Notification.findById(id);
  
  if (!notification) {
    throw new NotFoundError('Notification not found');
  }
  
  // Check if user owns this notification
  if (notification.user.toString() !== userId.toString()) {
    throw new ForbiddenError('You do not have permission to delete this notification');
  }
  
  await Notification.findByIdAndDelete(id);
  return true;
};

/**
 * Send notification to all members of a team (including captain)
 * @param {string} teamId - Team ID
 * @param {object} notificationData - Notification data (type, title, message, relatedEntity)
 */
export const notifyTeamMembers = async (teamId, notificationData) => {
  try {
    const team = await Team.findById(teamId)
      .populate('members', '_id firstName lastName gamertag')
      .populate('captain', '_id firstName lastName gamertag');
    
    if (!team) {
      console.warn(`[notifyTeamMembers] Team ${teamId} not found`);
      return;
    }
    
    // Combine captain and members, removing duplicates
    const allTeamMembers = [team.captain, ...team.members];
    const uniqueMembers = Array.from(
      new Map(allTeamMembers.map(m => [m._id.toString(), m])).values()
    );
    
    // Send notification to each member
    const notificationPromises = uniqueMembers.map(member => 
      createNotification({
        ...notificationData,
        user: member._id
      })
    );
    
    await Promise.all(notificationPromises);
  } catch (error) {
    // Don't fail the operation if notifications fail
    console.warn(`[notifyTeamMembers] Error sending notifications to team ${teamId}:`, error.message);
  }
};

