import React from 'react';
import { CheckCircle2, AlertCircle, LogOut, ArrowRight, ShieldCheck, Music2, Video } from 'lucide-react';

export default function AuthCard({ authStatus, onConnectSpotify, onConnectGoogle, onDisconnect, loading }) {
  const spotifyUser = authStatus?.spotifyUser;
  const googleUser = authStatus?.googleUser;
  const spotifyConnected = authStatus?.spotifyConnected;
  const googleConnected = authStatus?.googleConnected;

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 w-full">
      {/* Spotify Connection Card */}
      <div className={`relative rounded-2xl p-6 transition-all duration-300 border ${
        spotifyConnected
          ? 'bg-slate-900/70 border-emerald-500/30 shadow-lg shadow-emerald-950/20'
          : 'bg-slate-900/40 border-slate-800 hover:border-slate-700'
      }`}>
        <div className="flex items-start justify-between">
          <div className="flex items-center space-x-3.5">
            <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
              <Music2 className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h3 className="text-base font-bold text-white">Spotify Source</h3>
                {spotifyConnected ? (
                  <span className="flex items-center space-x-1 text-[11px] font-semibold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                    <CheckCircle2 className="w-3 h-3" />
                    <span>Connected</span>
                  </span>
                ) : (
                  <span className="flex items-center space-x-1 text-[11px] font-semibold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                    <AlertCircle className="w-3 h-3" />
                    <span>Disconnected</span>
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-400 mt-0.5">Read your playlists and track catalogs</p>
            </div>
          </div>
        </div>

        {/* Profile Info or Connect Action */}
        <div className="mt-5 pt-4 border-t border-slate-800/80">
          {spotifyConnected && spotifyUser ? (
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <img
                  src={spotifyUser.avatarUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100"}
                  alt={spotifyUser.displayName}
                  className="w-9 h-9 rounded-full object-cover ring-2 ring-emerald-500/30"
                />
                <div>
                  <p className="text-sm font-semibold text-white leading-tight">{spotifyUser.displayName}</p>
                  <p className="text-xs text-slate-400 leading-tight">{spotifyUser.email}</p>
                </div>
              </div>
              <button
                onClick={() => onDisconnect('spotify')}
                disabled={loading}
                className="p-2 text-xs text-slate-400 hover:text-red-400 hover:bg-slate-800/80 rounded-lg transition"
                title="Disconnect Spotify"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <button
              onClick={onConnectSpotify}
              disabled={loading}
              className="w-full flex items-center justify-center space-x-2 py-2.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-semibold shadow-md shadow-emerald-950/40 transition active:scale-[0.99] disabled:opacity-50"
            >
              <span>Connect Spotify Account</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>

      {/* Google / YouTube Connection Card */}
      <div className={`relative rounded-2xl p-6 transition-all duration-300 border ${
        googleConnected
          ? 'bg-slate-900/70 border-red-500/30 shadow-lg shadow-red-950/20'
          : 'bg-slate-900/40 border-slate-800 hover:border-slate-700'
      }`}>
        <div className="flex items-start justify-between">
          <div className="flex items-center space-x-3.5">
            <div className="w-12 h-12 rounded-xl bg-red-500/10 border border-red-500/20 flex items-center justify-center text-red-500">
              <Video className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h3 className="text-base font-bold text-white">YouTube Destination</h3>
                {googleConnected ? (
                  <span className="flex items-center space-x-1 text-[11px] font-semibold px-2 py-0.5 rounded-full bg-red-500/10 text-red-400 border border-red-500/20">
                    <CheckCircle2 className="w-3 h-3" />
                    <span>Connected</span>
                  </span>
                ) : (
                  <span className="flex items-center space-x-1 text-[11px] font-semibold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                    <AlertCircle className="w-3 h-3" />
                    <span>Disconnected</span>
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-400 mt-0.5">Create playlists and add video tracks</p>
            </div>
          </div>
        </div>

        {/* Profile Info or Connect Action */}
        <div className="mt-5 pt-4 border-t border-slate-800/80">
          {googleConnected && googleUser ? (
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <img
                  src={googleUser.avatarUrl || "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100"}
                  alt={googleUser.displayName}
                  className="w-9 h-9 rounded-full object-cover ring-2 ring-red-500/30"
                />
                <div>
                  <p className="text-sm font-semibold text-white leading-tight">{googleUser.displayName}</p>
                  <p className="text-xs text-slate-400 leading-tight">{googleUser.email}</p>
                </div>
              </div>
              <button
                onClick={() => onDisconnect('google')}
                disabled={loading}
                className="p-2 text-xs text-slate-400 hover:text-red-400 hover:bg-slate-800/80 rounded-lg transition"
                title="Disconnect Google"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <button
              onClick={onConnectGoogle}
              disabled={loading}
              className="w-full flex items-center justify-center space-x-2 py-2.5 px-4 rounded-xl bg-red-600 hover:bg-red-500 text-white text-sm font-semibold shadow-md shadow-red-950/40 transition active:scale-[0.99] disabled:opacity-50"
            >
              <span>Connect YouTube Account</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
