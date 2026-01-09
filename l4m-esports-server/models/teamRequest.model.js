import mongoose from 'mongoose';

const teamRequestSchema = new mongoose.Schema({
  team: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Team',
    required: [true, 'Team is required']
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'User is required']
  },
  status: {
    type: String,
    enum: ['pending', 'accepted', 'rejected'],
    default: 'pending'
  },
  message: {
    type: String,
    trim: true
  },
  reviewedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  },
  reviewedAt: {
    type: Date
  }
}, {
  timestamps: true
});

// Prevent duplicate pending requests
teamRequestSchema.index({ team: 1, user: 1, status: 1 }, { 
  unique: true,
  partialFilterExpression: { status: 'pending' }
});

export default mongoose.model('TeamRequest', teamRequestSchema);
