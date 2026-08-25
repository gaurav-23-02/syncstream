import React from 'react';
import { Database, CheckCircle2, Zap, Clock, Disc } from 'lucide-react';

export default function TransferStats({ progressData, activeJob }) {
  const matched = progressData?.matchedCount ?? activeJob?.matchedTracks ?? 0;
  const total = progressData?.totalTracks ?? activeJob?.totalTracks ?? 0;
  const cacheHits = progressData?.cacheHitCount ?? activeJob?.cacheHits ?? 0;
  const elapsedMillis = progressData?.elapsedMillis ?? 0;
  const elapsedSec = (elapsedMillis / 1000).toFixed(1);

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total Tracks */}
      <div className="glass-card rounded-xl p-4 border border-slate-800">
        <div className="flex items-center justify-between text-slate-400 mb-1">
          <span className="text-xs font-semibold">Total Songs</span>
          <Disc className="w-4 h-4 text-emerald-400" />
        </div>
        <div className="text-2xl font-black text-white font-mono">
          {total}
        </div>
        <p className="text-[10px] text-slate-500 mt-0.5">Spotify catalog items</p>
      </div>

      {/* Matched Tracks */}
      <div className="glass-card rounded-xl p-4 border border-slate-800">
        <div className="flex items-center justify-between text-slate-400 mb-1">
          <span className="text-xs font-semibold">Matched & Synced</span>
          <CheckCircle2 className="w-4 h-4 text-cyan-400" />
        </div>
        <div className="text-2xl font-black text-cyan-300 font-mono">
          {matched}
        </div>
        <p className="text-[10px] text-slate-500 mt-0.5">Resolved YouTube tracks</p>
      </div>

      {/* Cache Hits */}
      <div className="glass-card rounded-xl p-4 border border-slate-800">
        <div className="flex items-center justify-between text-slate-400 mb-1">
          <span className="text-xs font-semibold">Cache Hits</span>
          <Database className="w-4 h-4 text-amber-400" />
        </div>
        <div className="text-2xl font-black text-amber-300 font-mono">
          {cacheHits}
        </div>
        <p className="text-[10px] text-slate-500 mt-0.5">Saved YouTube API quota</p>
      </div>

      {/* Elapsed Time */}
      <div className="glass-card rounded-xl p-4 border border-slate-800">
        <div className="flex items-center justify-between text-slate-400 mb-1">
          <span className="text-xs font-semibold">Elapsed Time</span>
          <Clock className="w-4 h-4 text-teal-400" />
        </div>
        <div className="text-2xl font-black text-teal-300 font-mono">
          {elapsedSec}s
        </div>
        <p className="text-[10px] text-slate-500 mt-0.5">Virtual Thread execution</p>
      </div>
    </div>
  );
}
