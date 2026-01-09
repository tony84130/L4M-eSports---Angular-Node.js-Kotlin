export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface AuthUser {
  _id: string;
  email: string;
  firstName: string;
  lastName: string;
  gamertag: string;
  role: 'member' | 'captain' | 'admin';
  twitchUsername?: string;
}

export interface AuthSession {
  user: AuthUser;
  token: string;
}

export interface SignInPayload {
  email: string;
  password: string;
}

export interface SignUpPayload {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  gamertag: string;
  twitchUsername?: string;
}

export interface Game {
  _id: string;
  name: string;
  description?: string;
  logo?: string;
  rules?: string;
  formats?: string[];
  isActive?: boolean;
}

export interface Team {
  _id: string;
  name: string;
  logo?: string;
  game: string | Game;
  description?: string;
  captain: string | AuthUser;
  members: (string | AuthUser)[];
  status: 'active' | 'inactive';
  maxMembers?: number;
}

export interface Event {
  _id: string;
  name: string;
  game: string | Game;
  startDate: string;
  endDate: string;
  registrationStartDate: string;
  registrationEndDate: string;
  format: string;
  status:
    | 'draft'
    | 'open'
    | 'registration_closed'
    | 'in_progress'
    | 'completed'
    | 'cancelled';
  description?: string;
  rules?: string;
  maxTeams?: number;
  location?: {
    type: 'online' | 'physical';
    address?: string;
    coordinates?: {
      type: 'Point';
      coordinates: [number, number]; // [lng, lat]
    };
  };
}

export interface Match {
  _id: string;
  event: string | Event;
  teams: (string | Team)[];
  scheduledTime: string;
  status: 'upcoming' | 'in_progress' | 'finished' | 'pending_validation' | 'cancelled';
  score?: {
    team1: number;
    team2: number;
  };
  winner?: string | Team;
}

export interface Notification {
  _id: string;
  type: string;
  title?: string;
  message: string;
  isRead?: boolean;
  createdAt: string;
}

export interface AiRequestContext {
  page?: string;
  role?: string;
  extra?: Record<string, unknown>;
}

export interface AiAnswer {
  answer: string;
}
