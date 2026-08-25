function getApiBase() {
  const envBase = import.meta.env.VITE_API_BASE;
  if (envBase) {
    return envBase.endsWith('/api') ? envBase : `${envBase.replace(/\/$/, '')}/api`;
  }
  if (typeof window !== 'undefined' && window.location.hostname === 'localhost' && window.location.port === '5173') {
    return 'http://localhost:8080/api';
  }
  return '/api';
}

const API_BASE = getApiBase();

export const api = {
  // Auth endpoints
  async getAuthStatus() {
    const res = await fetch(`${API_BASE}/auth/status`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to fetch authentication status');
    return res.json();
  },

  async getSpotifyLoginUrl() {
    const res = await fetch(`${API_BASE}/auth/spotify/login`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to get Spotify login URL');
    return res.json();
  },

  async getGoogleLoginUrl() {
    const res = await fetch(`${API_BASE}/auth/google/login`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to get Google login URL');
    return res.json();
  },

  async triggerDemoLogin() {
    const res = await fetch(`${API_BASE}/auth/demo-login`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
    });
    if (!res.ok) throw new Error('Failed to trigger sandbox login');
    return res.json();
  },

  async disconnect(provider) {
    const res = await fetch(`${API_BASE}/auth/disconnect/${provider}`, {
      method: 'POST',
      credentials: 'include',
    });
    if (!res.ok) throw new Error(`Failed to disconnect ${provider}`);
    return res.json();
  },

  // Spotify endpoints
  async getSpotifyPlaylists() {
    const res = await fetch(`${API_BASE}/spotify/playlists`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to fetch Spotify playlists');
    return res.json();
  },

  async getPlaylistTracks(playlistId) {
    const res = await fetch(`${API_BASE}/spotify/playlists/${playlistId}/tracks`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to fetch playlist tracks');
    return res.json();
  },

  // Transfer endpoints
  async startTransfer(requestData) {
    const res = await fetch(`${API_BASE}/transfer/start`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(requestData),
    });
    if (!res.ok) throw new Error('Failed to initiate transfer job');
    return res.json();
  },

  async getTransferJobs() {
    const res = await fetch(`${API_BASE}/transfer/jobs`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to fetch transfer history');
    return res.json();
  },

  async getTransferJob(jobId) {
    const res = await fetch(`${API_BASE}/transfer/jobs/${jobId}`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to fetch transfer job');
    return res.json();
  }
};
