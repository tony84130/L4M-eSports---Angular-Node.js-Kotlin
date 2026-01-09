import mongoose from 'mongoose';

const eventRegistrationSchema = new mongoose.Schema({
  event: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Event',
    required: [true, 'Event is required']
  },
  team: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Team',
    required: [true, 'Team is required']
  },
  status: {
    type: String,
    // J'ai unifié les statuts pour qu'ils soient clairs pour le code
    enum: ['PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED'], 
    default: 'PENDING'
  },
  registeredBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  // Membres participants à l'événement (le capitaine est inclus automatiquement)
  participatingMembers: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  }]
}, {
  timestamps: true // Crée automatiquement createdAt (date d'inscription) et updatedAt
});

// Empêcher les doublons : une équipe ne peut pas s'inscrire 2 fois au même event
eventRegistrationSchema.index({ event: 1, team: 1 }, { unique: true });

export default mongoose.model('EventRegistration', eventRegistrationSchema);