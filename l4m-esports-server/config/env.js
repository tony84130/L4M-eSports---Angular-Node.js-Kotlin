import 'dotenv/config';

export default {
  PORT: process.env.PORT,
  NODE_ENV: process.env.NODE_ENV,
  MONGODB_URI: process.env.MONGODB_URI,
  REDIS_URL: process.env.REDIS_URL,
  JWT_SECRET: process.env.JWT_SECRET,
  JWT_EXPIRES_IN: process.env.JWT_EXPIRES_IN,
  TWITCH_CLIENT_ID: process.env.TWITCH_CLIENT_ID,
  TWITCH_CLIENT_SECRET: process.env.TWITCH_CLIENT_SECRET,
  OPENAI_API_KEY: process.env.OPENAI_API_KEY,
  // Admin user configuration
  ADMIN_EMAIL: process.env.ADMIN_EMAIL,
  ADMIN_PASSWORD: process.env.ADMIN_PASSWORD,
  ADMIN_FIRST_NAME: process.env.ADMIN_FIRST_NAME,
  ADMIN_LAST_NAME: process.env.ADMIN_LAST_NAME,
  ADMIN_GAMERTAG: process.env.ADMIN_GAMERTAG
};
