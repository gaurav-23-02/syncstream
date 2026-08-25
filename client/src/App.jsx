import React, { useState, useEffect, useRef } from 'react';
import Header from './components/Header';
import AuthCard from './components/AuthCard';
import PlaylistSelector from './components/PlaylistSelector';
import TransferConfigModal from './components/TransferConfigModal';
import LiveTransferMonitor from './components/LiveTransferMonitor';
import TerminalLogViewer from './components/TerminalLogViewer';
import TransferStats from './components/TransferStats';
import TransferHistory from './components/TransferHistory';
import { api } from './services/api';
import { subscribeTransferProgress } from './services/sse';
import { AlertCircle, CheckCircle, RefreshCw, Zap, Sparkles } from 'lucide-react';

export default function App() {
  const [authStatus, setAuthStatus] = useState(null);
  const [playlists, setPlaylists] = useState([]);
  const [selectedPlaylist, setSelectedPlaylist] = useState(null);
  const [isTransferring, setIsTransferring] = useState(false);
  const [activeJob, setActiveJob] = useState(null);
  const [progressData, setProgressData] = useState(null);
  const [liveLogs, setLiveLogs] = useState([]);
  const [transferHistory, setTransferHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notification, setNotification] = useState(null);

  const sseCleanupRef = useRef(null);

  // Initialize data on mount
  useEffect(() => {
    loadInitialData();

    // Check for OAuth callback URL params
    const params = new URLSearchParams(window.location.search);
    if (params.get('auth')) {
      const type = params.get('auth');
      showNotification('success', `Successfully authenticated with ${type.includes('spotify') ? 'Spotify' : 'Google/YouTube'}!`);
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, []);

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  const loadInitialData = async () => {
    setLoading(true);
    try {
      const status = await api.getAuthStatus();
      setAuthStatus(status);

      if (status.spotifyConnected) {
        const list = await api.getSpotifyPlaylists();
        setPlaylists(list);
      }

      const history = await api.getTransferJobs();
      setTransferHistory(history);
    } catch (err) {
      console.error('Error initializing app state:', err);
    } finally {
      setLoading(false);
    }
  };

  // Auth Actions
  const handleConnectSpotify = async () => {
    try {
      const { url } = await api.getSpotifyLoginUrl();
      window.location.href = url;
    } catch (err) {
      showNotification('error', 'Could not open Spotify login: ' + err.message);
    }
  };

  const handleConnectGoogle = async () => {
    try {
      const { url } = await api.getGoogleLoginUrl();
      window.location.href = url;
    } catch (err) {
      showNotification('error', 'Could not open Google login: ' + err.message);
    }
  };

  const handleDemoLogin = async () => {
    setLoading(true);
    try {
      const status = await api.triggerDemoLogin();
      setAuthStatus(status);
      const list = await api.getSpotifyPlaylists();
      setPlaylists(list);
      showNotification('success', '1-Click Sandbox Demo Activated! Ready for transfer.');
    } catch (err) {
      showNotification('error', 'Failed demo login: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDisconnect = async (provider) => {
    try {
      const updated = await api.disconnect(provider);
      setAuthStatus(updated);
      if (provider === 'spotify' || provider === 'all') {
        setPlaylists([]);
        setSelectedPlaylist(null);
      }
      showNotification('info', `Disconnected ${provider}.`);
    } catch (err) {
      showNotification('error', 'Failed to disconnect: ' + err.message);
    }
  };

  // Transfer Actions
  const handleStartTransfer = async (config) => {
    setIsTransferring(true);
    setProgressData(null);
    setLiveLogs([]);

    try {
      const job = await api.startTransfer(config);
      setActiveJob(job);
      setLiveLogs(job.logs || []);

      // Clean up any existing SSE subscriber
      if (sseCleanupRef.current) {
        sseCleanupRef.current();
      }

      // Open SSE connection
      sseCleanupRef.current = subscribeTransferProgress(job.jobId, {
        onProgress: (data) => {
          setProgressData(data);
          if (data.log) {
            setLiveLogs((prev) => [
              ...prev,
              {
                id: String(Date.now() + Math.random()),
                timestamp: new Date().toISOString(),
                level: data.logLevel || 'INFO',
                message: data.log,
                details: data.currentTrackTitle ? `${data.currentArtist} - ${data.currentTrackTitle}` : undefined,
              },
            ]);
          }
        },
        onComplete: (data) => {
          setProgressData(data);
          setIsTransferring(false);
          showNotification('success', 'Transfer completed successfully!');
          api.getTransferJobs().then(setTransferHistory).catch(console.error);
        },
        onError: (err) => {
          console.warn('SSE stream closed/errored:', err);
          setIsTransferring(false);
        },
      });
    } catch (err) {
      setIsTransferring(false);
      showNotification('error', 'Failed to start transfer: ' + err.message);
    }
  };

  const handleResetTransfer = () => {
    setActiveJob(null);
    setProgressData(null);
    setLiveLogs([]);
    setSelectedPlaylist(null);
  };

  const isBothConnected = authStatus?.spotifyConnected && authStatus?.googleConnected;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col selection:bg-emerald-500/30 selection:text-emerald-300">
      {/* Top Navigation */}
      <Header
        authStatus={authStatus}
        onDemoLogin={handleDemoLogin}
        onRefresh={loadInitialData}
        loading={loading}
      />

      {/* Floating Notifications */}
      {notification && (
        <div className="fixed top-20 right-6 z-50 animate-bounce">
          <div className={`flex items-center space-x-2 px-4 py-3 rounded-xl shadow-2xl border text-xs font-bold ${
            notification.type === 'success'
              ? 'bg-emerald-950/90 text-emerald-300 border-emerald-500/50'
              : notification.type === 'error'
              ? 'bg-red-950/90 text-red-300 border-red-500/50'
              : 'bg-slate-900/90 text-slate-300 border-slate-700'
          }`}>
            {notification.type === 'success' ? (
              <CheckCircle className="w-4 h-4 text-emerald-400" />
            ) : (
              <AlertCircle className="w-4 h-4 text-red-400" />
            )}
            <span>{notification.message}</span>
          </div>
        </div>
      )}

      {/* Main Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        
        {/* Hero Section */}
        <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-slate-900 via-slate-950 to-slate-900 border border-slate-800 p-8 sm:p-10 shadow-2xl">
          <div className="relative z-10 max-w-2xl space-y-3">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold">
              <Zap className="w-3.5 h-3.5" />
              <span>Zero-Loss Audio Fingerprinting & Caffeine Caching</span>
            </div>
            <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-white leading-tight">
              Transfer your Spotify Playlists to YouTube in Seconds.
            </h2>
            <p className="text-slate-400 text-sm leading-relaxed">
              SyncStream matches tracks with high-accuracy ISRC metadata, utilizes Java 21 Virtual Threads for concurrent search resolution, and streams progress in real time via Server-Sent Events.
            </p>
          </div>
        </section>

        {/* Auth Cards Section */}
        <section>
          <AuthCard
            authStatus={authStatus}
            onConnectSpotify={handleConnectSpotify}
            onConnectGoogle={handleConnectGoogle}
            onDisconnect={handleDisconnect}
            loading={loading}
          />
        </section>

        {/* Live Transfer Live Monitor (when active or in progress) */}
        {(activeJob || progressData) && (
          <section className="space-y-6">
            <TransferStats progressData={progressData} activeJob={activeJob} />
            <LiveTransferMonitor
              activeJob={activeJob}
              progressData={progressData}
              onReset={handleResetTransfer}
            />
            <TerminalLogViewer logs={liveLogs} />
          </section>
        )}

        {/* Playlists & Transfer Configuration (when not actively transferring) */}
        {!activeJob && (
          <section className="space-y-8">
            {authStatus?.spotifyConnected ? (
              <>
                <PlaylistSelector
                  playlists={playlists}
                  selectedPlaylist={selectedPlaylist}
                  onSelectPlaylist={(p) => setSelectedPlaylist(p)}
                  loading={loading}
                />

                {selectedPlaylist && (
                  <TransferConfigModal
                    selectedPlaylist={selectedPlaylist}
                    isTransferring={isTransferring}
                    onStartTransfer={handleStartTransfer}
                    onCancel={() => setSelectedPlaylist(null)}
                  />
                )}
              </>
            ) : (
              <div className="glass-panel rounded-2xl p-10 text-center border border-slate-800 space-y-4">
                <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 flex items-center justify-center mx-auto">
                  <Sparkles className="w-6 h-6" />
                </div>
                <h3 className="text-lg font-bold text-white">Connect Spotify or Try Demo Mode</h3>
                <p className="text-xs text-slate-400 max-w-md mx-auto">
                  Connect your Spotify account above, or click <strong>1-Click Demo Login</strong> in the header to immediately test transfers with rich sample catalogs.
                </p>
                <button
                  onClick={handleDemoLogin}
                  className="inline-flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold shadow-lg shadow-emerald-950/40 transition"
                >
                  <Sparkles className="w-4 h-4 text-yellow-300" />
                  <span>Launch 1-Click Demo Sandbox</span>
                </button>
              </div>
            )}
          </section>
        )}

        {/* Transfer History Section */}
        {transferHistory.length > 0 && !activeJob && (
          <section>
            <TransferHistory history={transferHistory} />
          </section>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950 py-6 text-center text-xs text-slate-500">
        <p>SyncStream • Powered by Java 21 LTS, Spring Boot 3.3, Spotify Web API & YouTube Data API v3</p>
      </footer>
    </div>
  );
}
