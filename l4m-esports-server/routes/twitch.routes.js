import express from 'express';
import axios from 'axios';
import env from '../config/env.js';

const router = express.Router();

const TWITCH_TOKEN_URL = 'https://id.twitch.tv/oauth2/token';
const TWITCH_HELIX = 'https://api.twitch.tv/helix';

async function getAppToken() {
  if (!env.TWITCH_CLIENT_ID || !env.TWITCH_CLIENT_SECRET) {
    throw new Error('TWITCH_CLIENT_ID or TWITCH_CLIENT_SECRET missing');
  }
  const params = new URLSearchParams({
    client_id: env.TWITCH_CLIENT_ID,
    client_secret: env.TWITCH_CLIENT_SECRET,
    grant_type: 'client_credentials'
  });
  const { data } = await axios.post(TWITCH_TOKEN_URL, params);
  return data.access_token;
}

async function helixGet(token, path, params = {}) {
  const { data } = await axios.get(`${TWITCH_HELIX}${path}`, {
    params,
    headers: {
      Authorization: `Bearer ${token}`,
      'Client-Id': env.TWITCH_CLIENT_ID
    }
  });
  return data;
}

// GET /api/twitch/user/:username
router.get('/user/:username', async (req, res) => {
  try {
    const username = req.params.username;
    const token = await getAppToken();

    // User info
    const userData = await helixGet(token, '/users', { login: username });
    const user = userData.data?.[0];
    if (!user) {
      return res.status(404).json({ success: false, message: 'Utilisateur Twitch introuvable' });
    }

    // Stream info (is live)
    const streamData = await helixGet(token, '/streams', { user_id: user.id });
    const isLive = (streamData.data?.length ?? 0) > 0;

    // Followers count
    let followers = undefined;
    try {
      const follows = await helixGet(token, '/users/follows', { to_id: user.id });
      followers = follows.total;
    } catch (err) {
      // ignore followers fetch errors
    }

    const result = {
      display_name: user.display_name,
      description: user.description,
      profile_image_url: user.profile_image_url,
      view_count: user.view_count,
      followers,
      is_live: isLive
    };

    return res.json({ success: true, data: result });
  } catch (error) {
    console.error('Twitch error', error.message);
    return res.status(500).json({ success: false, message: 'Twitch API non disponible' });
  }
});

export default router;

