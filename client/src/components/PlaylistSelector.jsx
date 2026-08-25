import React, { useState } from 'react';
import { Search, ListMusic, Check, Lock, Globe, Clock, ChevronRight, Sparkles } from 'lucide-react';

export default function PlaylistSelector({ playlists, selectedPlaylist, onSelectPlaylist, loading }) {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredPlaylists = playlists.filter(p =>
    p.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.owner?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="glass-panel rounded-2xl p-6 border border-slate-800">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-5 border-b border-slate-800">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center space-x-2">
            <ListMusic className="w-5 h-5 text-emerald-400" />
            <span>Select Spotify Playlist</span>
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Choose a playlist to transfer to your YouTube / YouTube Music library
          </p>
        </div>

        {/* Search Filter */}
        <div className="relative w-full sm:w-64">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search playlists..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 rounded-xl bg-slate-900 border border-slate-800 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500/50 transition"
          />
        </div>
      </div>

      {/* Playlist Grid */}
      <div className="mt-5 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {filteredPlaylists.map((playlist) => {
          const isSelected = selectedPlaylist?.id === playlist.id;

          return (
            <div
              key={playlist.id}
              onClick={() => onSelectPlaylist(playlist)}
              className={`group relative rounded-xl p-3.5 cursor-pointer transition-all duration-200 border ${
                isSelected
                  ? 'bg-slate-900 border-emerald-500 glow-emerald scale-[1.02]'
                  : 'bg-slate-900/40 border-slate-800/80 hover:bg-slate-900/80 hover:border-slate-700'
              }`}
            >
              {/* Cover Image */}
              <div className="relative aspect-square rounded-lg overflow-hidden bg-slate-950 mb-3 shadow-inner">
                <img
                  src={playlist.imageUrl || "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=300"}
                  alt={playlist.title}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
                
                {/* Selection Checkmark Badge */}
                {isSelected && (
                  <div className="absolute top-2 right-2 w-6 h-6 rounded-full bg-emerald-500 text-slate-950 flex items-center justify-center shadow-lg font-bold">
                    <Check className="w-4 h-4 stroke-[3]" />
                  </div>
                )}

                {/* Track Count Pill */}
                <div className="absolute bottom-2 left-2 px-2 py-0.5 rounded-md bg-slate-950/80 backdrop-blur-md text-[10px] font-semibold text-slate-300 border border-white/10">
                  {playlist.totalTracks} tracks
                </div>
              </div>

              {/* Info */}
              <h3 className="text-sm font-bold text-white line-clamp-1 group-hover:text-emerald-300 transition">
                {playlist.title}
              </h3>
              <p className="text-xs text-slate-400 line-clamp-1 mt-0.5">
                by {playlist.owner}
              </p>

              {/* Tags */}
              <div className="mt-2.5 flex items-center justify-between text-[11px] text-slate-500 pt-2 border-t border-slate-800/60">
                <span className="flex items-center space-x-1">
                  {playlist.isPublic ? <Globe className="w-3 h-3 text-emerald-400" /> : <Lock className="w-3 h-3 text-amber-400" />}
                  <span>{playlist.isPublic ? 'Public' : 'Private'}</span>
                </span>
                <span className="text-slate-400 group-hover:text-emerald-400 transition flex items-center space-x-0.5">
                  <span>{isSelected ? 'Selected' : 'Select'}</span>
                  <ChevronRight className="w-3 h-3" />
                </span>
              </div>
            </div>
          );
        })}

        {filteredPlaylists.length === 0 && (
          <div className="col-span-full py-12 text-center text-slate-500">
            <ListMusic className="w-8 h-8 mx-auto mb-2 opacity-40" />
            <p className="text-sm">No playlists found matching "{searchTerm}"</p>
          </div>
        )}
      </div>
    </div>
  );
}
