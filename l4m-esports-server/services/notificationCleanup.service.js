import Notification from '../models/notification.model.js';

/**
 * Cleanup old notifications
 * - Deletes read notifications older than 30 days
 * - Deletes unread notifications older than 90 days
 */
export const cleanupOldNotifications = async () => {
  try {
    const now = new Date();
    const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    const ninetyDaysAgo = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000);

    // Delete read notifications older than 30 days
    const deletedRead = await Notification.deleteMany({
      read: true,
      createdAt: { $lt: thirtyDaysAgo }
    });

    // Delete unread notifications older than 90 days
    const deletedUnread = await Notification.deleteMany({
      read: false,
      createdAt: { $lt: ninetyDaysAgo }
    });

    const totalDeleted = deletedRead.deletedCount + deletedUnread.deletedCount;
    
    if (totalDeleted > 0) {
      console.log(`🧹 Cleaned up ${totalDeleted} old notifications (${deletedRead.deletedCount} read, ${deletedUnread.deletedCount} unread)`);
    }

    return {
      deletedRead: deletedRead.deletedCount,
      deletedUnread: deletedUnread.deletedCount,
      total: totalDeleted
    };
  } catch (error) {
    console.error('❌ Error cleaning up notifications:', error);
    throw error;
  }
};

/**
 * Start the cleanup job (runs daily at 2 AM)
 */
export const startCleanupJob = () => {
  // Run cleanup immediately on startup
  cleanupOldNotifications().catch(console.error);

  // Then run daily at 2 AM
  const runDaily = () => {
    const now = new Date();
    const tomorrow = new Date(now);
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(2, 0, 0, 0); // 2 AM

    const msUntil2AM = tomorrow.getTime() - now.getTime();

    setTimeout(() => {
      cleanupOldNotifications().catch(console.error);
      // Schedule next run (24 hours later)
      setInterval(() => {
        cleanupOldNotifications().catch(console.error);
      }, 24 * 60 * 60 * 1000); // 24 hours
    }, msUntil2AM);
  };

  runDaily();
  console.log('✅ Notification cleanup job started (runs daily at 2 AM)');
};

