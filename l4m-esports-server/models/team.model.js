import mongoose from 'mongoose';

const teamSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Team name is required'],
    trim: true
  },
  logo: {
    type: String, // URL to logo image
    trim: true
  },
  game: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Game',
    required: [true, 'Game is required']
  },
  description: {
    type: String,
    trim: true
  },
  captain: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: [true, 'Team must have a captain']
  },
  members: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  }],
  status: {
    type: String,
    enum: ['active', 'inactive', 'archived'],
    default: 'active'
  },
  maxMembers: {
    type: Number,
    default: 10
  }
}, {
  timestamps: true
});

// Ensure captain is in members array
teamSchema.pre('save', function(next) {
  if (this.captain && !this.members.includes(this.captain)) {
    this.members.push(this.captain);
  }
  next();
});

// Index for faster queries
teamSchema.index({ game: 1 });
teamSchema.index({ captain: 1 });
teamSchema.index({ status: 1 });

export default mongoose.model('Team', teamSchema);
