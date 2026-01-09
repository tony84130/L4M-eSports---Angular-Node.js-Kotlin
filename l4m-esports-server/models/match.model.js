import mongoose from 'mongoose';

const matchSchema = new mongoose.Schema({
  event: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Event',
    required: [true, 'Event is required']
  },
  teams: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Team',
    required: true
  }],
  scheduledTime: {
    type: Date,
    required: [true, 'Scheduled time is required']
  },
  actualStartTime: {
    type: Date
  },
  actualEndTime: {
    type: Date
  },
  status: {
    type: String,
    enum: ['upcoming', 'in_progress', 'finished', 'pending_validation', 'cancelled'],
    default: 'upcoming'
  },
  score: {
    team1: {
      type: Number,
      default: 0
    },
    team2: {
      type: Number,
      default: 0
    }
  },
  bracketPosition: {
    round: {
      type: Number,
      required: true
      // Round number (1 = final, 2 = semi-final, etc.)
    },
    matchNumber: {
      type: Number,
      required: true
      // Match number within the round
    },
    bracketSide: {
      type: String,
      enum: ['upper', 'lower', 'single'],
      default: 'single'
    }
  },
  winner: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Team'
  },
  validatedBy: [{
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    },
    validatedAt: {
      type: Date,
      default: Date.now
    }
  }],
  notes: {
    type: String,
    trim: true
  }
}, {
  timestamps: true
});

// Indexes for faster queries
matchSchema.index({ event: 1 });
matchSchema.index({ status: 1 });
matchSchema.index({ scheduledTime: 1 });
matchSchema.index({ 'bracketPosition.round': 1, 'bracketPosition.matchNumber': 1 });
matchSchema.index({ teams: 1 });

export default mongoose.model('Match', matchSchema);

