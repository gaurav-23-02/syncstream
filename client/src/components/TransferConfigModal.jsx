import React, { useState, useEffect } from 'react';
import { ArrowRight, Lock, Eye, Globe, Zap, Disc, Sparkles } from 'lucide-react';

export default function TransferConfigModal({
  selectedPlaylist,
  isTransferring,
  onStartTransfer,
  onCancel,
}) {
  const [targetTitle, setTargetTitle] = useState('');
  const [privacyStatus, setPrivacyStatus] = useState('private');

  useEffect(() => {
    if (selectedPlaylist) {
      setTargetTitle(`Spotify - ${selectedPlaylist.title}`);
    }
  }, [selectedPlaylist]);

  if (!selectedPlaylist) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    onStartTransfer({
      spotifyPlaylistId: selectedPlaylist.id,
      targetPlaylistName: targetTitle || selectedPlaylist.title,
      targetPlaylistDescription: `Transferred from Spotify playlist "${selectedPlaylist.title}" with SyncStream engine.`,
      privacyStatus: privacyStatus,
    });
  };

  return (
    <div className="glass-panel rounded-2xl p-6 border border-slate-800 bg-slate-900/60 shadow-xl">
      <div className="flex items-center space-x-3 pb-4 border-b border-slate-800">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-red-500 flex items-center justify-center p-[1px]">
          <div className="w-full h-full bg-slate-950 rounded-[11px] flex items-center justify-center">
            <Zap className="w-5 h-5 text-emerald-400" />
          </div>
        </div>
        <div>
          <h2 className="text-lg font-bold text-white">Configure Transfer Target</h2>
          <p className="text-xs text-slate-400">Set destination YouTube playlist properties</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="mt-5 space-y-5">
        {/* Source Summary */}
        <div className="flex items-center space-x-4 p-3.5 rounded-xl bg-slate-950/70 border border-slate-800">
          <img
            src={selectedPlaylist.imageUrl || "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=100"}
            alt={selectedPlaylist.title}
            className="w-14 h-14 rounded-lg object-cover shadow"
          />
          <div className="flex-1 min-w-0">
            <span className="text-[10px] font-semibold text-emerald-400 uppercase tracking-wider">Source Spotify Playlist</span>
            <h4 className="text-sm font-bold text-white truncate">{selectedPlaylist.title}</h4>
            <p className="text-xs text-slate-400">{selectedPlaylist.totalTracks} tracks ready for sync</p>
          </div>
        </div>

        {/* Target Title Input */}
        <div>
          <label className="block text-xs font-semibold text-slate-300 mb-1.5">
            Destination YouTube Playlist Title
          </label>
          <input
            type="text"
            required
            value={targetTitle}
            onChange={(e) => setTargetTitle(e.target.value)}
            placeholder="Enter YouTube playlist name..."
            className="w-full px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:border-emerald-500/60 focus:ring-1 focus:ring-emerald-500/30 transition"
          />
        </div>

        {/* Privacy Selector */}
        <div>
          <label className="block text-xs font-semibold text-slate-300 mb-1.5">
            YouTube Privacy Mode
          </label>
          <div className="grid grid-cols-3 gap-3">
            {[
              { id: 'private', label: 'Private', icon: Lock, desc: 'Only visible to you' },
              { id: 'unlisted', label: 'Unlisted', icon: Eye, desc: 'Visible with link' },
              { id: 'public', label: 'Public', icon: Globe, desc: 'Searchable by anyone' },
            ].map((option) => {
              const Icon = option.icon;
              const isSelected = privacyStatus === option.id;

              return (
                <button
                  key={option.id}
                  type="button"
                  onClick={() => setPrivacyStatus(option.id)}
                  className={`p-3 rounded-xl text-left border transition-all ${
                    isSelected
                      ? 'bg-slate-900 border-emerald-500/70 shadow-sm shadow-emerald-500/10'
                      : 'bg-slate-950/60 border-slate-800 hover:border-slate-700'
                  }`}
                >
                  <div className="flex items-center space-x-1.5">
                    <Icon className={`w-3.5 h-3.5 ${isSelected ? 'text-emerald-400' : 'text-slate-400'}`} />
                    <span className={`text-xs font-bold ${isSelected ? 'text-white' : 'text-slate-300'}`}>{option.label}</span>
                  </div>
                  <p className="text-[10px] text-slate-500 mt-1">{option.desc}</p>
                </button>
              );
            })}
          </div>
        </div>

        {/* Action Submit */}
        <div className="flex items-center justify-end space-x-3 pt-2">
          <button
            type="button"
            onClick={onCancel}
            disabled={isTransferring}
            className="px-4 py-2 text-xs font-semibold text-slate-400 hover:text-white rounded-xl transition"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isTransferring || !targetTitle}
            className="flex items-center space-x-2 px-6 py-2.5 rounded-xl bg-gradient-to-r from-emerald-500 via-teal-500 to-cyan-500 hover:from-emerald-400 hover:to-cyan-400 text-slate-950 text-xs font-extrabold shadow-lg shadow-emerald-500/20 active:scale-[0.98] transition disabled:opacity-50"
          >
            <Sparkles className="w-4 h-4 text-slate-950" />
            <span>{isTransferring ? 'Starting Transfer...' : 'Initiate Transfer Engine'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </form>
    </div>
  );
}
