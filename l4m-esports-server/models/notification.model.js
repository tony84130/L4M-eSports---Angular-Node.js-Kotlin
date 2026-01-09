import mongoose from 'mongoose';

const notificationSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User is required']
  },
  type: {
    type: String,
    enum: [
      'match_starting_soon',
      'match_status_changed',
      'match_created',
      'match_finished',
      'match_cancelled',
      'match_won',
      'match_lost',
      'match_rescheduled',
      'team_request',
      'team_request_accepted',
      'team_request_rejected',
      'team_invitation',
      'team_invitation_accepted',
      'team_invitation_rejected',
      'team_member_removed',
      'team_member_left',
      'team_member_joined',
      'team_captain_transferred',
      'team_updated',
      'event_nearby',
      'event_registration_open',
      'event_registration_closed',
      'event_registration_created',
      'event_registration_accepted',
      'event_registration_rejected',
      'event_registration_cancelled',
      'event_started',
      'event_completed',
      'event_updated',
      'event_cancelled',
      'match_score_updated',
      'bracket_updated',
      'next_round_created'
    ],
    required: [true, 'Notification type is required']
  },
  title: {
    type: String,
    required: [true, 'Notification title is required'],
    trim: true
  },
  message: {
    type: String,
    required: [true, 'Notification message is required'],
    trim: true
  },
  read: {
    type: Boolean,
    default: false
  },
  readAt: {
    type: Date
  },
  relatedEntity: {
    entityType: {
      type: String,
      enum: ['match', 'event', 'team', 'team_request']
    },
    entityId: {
      type: mongoose.Schema.Types.ObjectId
    }
  },
  scheduledFor: {
    type: Date
    // For scheduled notifications (e.g., "15 minutes before match")
  },
  sent: {
    type: Boolean,
    default: false
  },
  sentAt: {
    type: Date
  }
}, {
  timestamps: true
});

// Indexes for faster queries
notificationSchema.index({ user: 1, read: 1 });
notificationSchema.index({ user: 1, createdAt: -1 });
notificationSchema.index({ scheduledFor: 1, sent: 1 });
notificationSchema.index({ type: 1 });

// Index pour le nettoyage automatique (read + createdAt)
notificationSchema.index({ read: 1, createdAt: 1 });

export default mongoose.model('Notification', notificationSchema);

