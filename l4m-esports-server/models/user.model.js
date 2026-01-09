import mongoose from 'mongoose';
import bcrypt from 'bcryptjs';

const userSchema = new mongoose.Schema({
  email: {
    type: String,
    required: [true, 'Email is required'],
    unique: true,
    lowercase: true,
    trim: true,
    match: [/^\S+@\S+\.\S+$/, 'Please provide a valid email']
  },
  password: {
    type: String,
    required: [true, 'Password is required'],
    minlength: 6,
    select: false // Don't return password by default
  },
  firstName: {
    type: String,
    required: [true, 'First name is required'],
    trim: true
  },
  lastName: {
    type: String,
    required: [true, 'Last name is required'],
    trim: true
  },
  gamertag: {
    type: String,
    required: [true, 'Gamertag is required'],
    unique: true,
    trim: true,
    minlength: [2, 'Gamertag must be at least 2 characters long'],
    maxlength: [30, 'Gamertag must not exceed 30 characters']
  },
  role: {
    type: String,
    enum: ['member', 'captain', 'admin'],
    default: 'member'
  },
  twitchUsername: {
    type: String,
    trim: true,
    sparse: true // Allows multiple null values but unique for non-null
  },
  preferences: {
    favoriteGames: [{
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Game'
    }],
    notificationSettings: {
      matchReminders: {
        type: Boolean,
        default: true
      },
      eventNearby: {
        type: Boolean,
        default: true
      }
    }
  },
  location: {
    latitude: {
      type: Number,
      min: -90,
      max: 90
    },
    longitude: {
      type: Number,
      min: -180,
      max: 180
    }
  },
  avatar: {
    type: String
  }
}, {
  timestamps: true
});

// Hash password before saving
userSchema.pre('save', async function(next) {
  if (!this.isModified('password')) return next();
  
  const salt = await bcrypt.genSalt(10);
  this.password = await bcrypt.hash(this.password, salt);
  next();
});

// Method to compare password
userSchema.methods.comparePassword = async function(candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.password);
};

export default mongoose.model('User', userSchema);
