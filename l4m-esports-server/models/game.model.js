import mongoose from 'mongoose';

const gameSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Game name is required'],
    unique: true,
    trim: true
  },
  description: {
    type: String,
    trim: true
  },
  logo: {
    type: String, // URL to logo image
    trim: true
  },
  rules: {
    type: String,
    trim: true
  },
  formats: [{
    type: String,
    enum: ['1v1', '2v2', '3v3', '4v4', '5v5', 'BATTLE_ROYALE'],
    required: true
  }],
  isActive: {
    type: Boolean,
    default: true
  },
  createdBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  }
}, {
  timestamps: true
});

export default mongoose.model('Game', gameSchema);
