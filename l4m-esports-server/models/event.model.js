import mongoose from 'mongoose';

const eventSchema = new mongoose.Schema({
  name: {
    type: String,
    required: [true, 'Event name is required'],
    trim: true
  },
  game: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Game',
    required: [true, 'Game is required']
  },
  startDate: {
    type: Date,
    required: [true, 'Start date is required']
  },
  endDate: {
    type: Date,
    required: [true, 'End date is required']
  },
  registrationStartDate: {
    type: Date,
    required: [true, 'Registration start date is required']
  },
  registrationEndDate: {
    type: Date,
    required: [true, 'Registration end date is required']
  },
  format: {
    type: String,
    required: [true, 'Event format is required'],
    trim: true,
    enum: ['1v1', '2v2', '3v3', '4v4', '5v5', 'BATTLE_ROYALE']
  },
  rules: {
    type: String,
    trim: true
  },
  description: {
    type: String,
    trim: true
  },
  status: {
    type: String,
    enum: ['draft', 'open', 'registration_closed', 'in_progress', 'completed', 'cancelled'],
    default: 'draft'
  },
  location: {
    type: {
      type: String,
      enum: ['online', 'physical'],
      default: 'online'
    },
    address: {
      type: String,
      trim: true
    },
    // FORMAT GEOJSON STANDARD (Vital pour la recherche de proximité)
    coordinates: {
      type: { type: String, enum: ['Point'], default: 'Point' },
      coordinates: { 
        type: [Number], // [longitude, latitude]
        default: [0, 0] 
      }
    }
  },
  maxTeams: {
    type: Number,
    default: 16
  },
  createdBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  bracketGenerated: {
    type: Boolean,
    default: false
  },
  // On stocke le bracket directement ici pour la partie 2
  bracket: {
    rounds: [{
      roundNumber: Number,
      matches: [{
        team1: { type: mongoose.Schema.Types.ObjectId, ref: 'Team' },
        team2: { type: mongoose.Schema.Types.ObjectId, ref: 'Team' },
        winner: { type: mongoose.Schema.Types.ObjectId, ref: 'Team' }
      }]
    }]
  }
}, {
  timestamps: true
});

// Index Geospatial pour la recherche rapide
eventSchema.index({ 'location.coordinates': '2dsphere' });
eventSchema.index({ status: 1 });
eventSchema.index({ game: 1 });
eventSchema.index({ startDate: 1 });

export default mongoose.model('Event', eventSchema);