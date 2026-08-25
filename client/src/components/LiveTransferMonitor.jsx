import React from 'react';
import {
  CheckCircle2,
  AlertCircle,
  ExternalLink,
  Zap,
  Disc,
  Clock,
  Radio,
  Sparkles,
  Check,
  RotateCcw,
  Youtube,
  Database
} from 'lucide-react';

export default function LiveTransferMonitor({
  activeJob,
  progressData,
  onReset,
}) {
  if (!activeJob && !progressData) return null;

  const status = progressData?.status || activeJob?.status || 'IN_PROGRESS';
  const progress = progressData?.progress ?? activeJob?.progressPercentage ?? 0;
  const isCompleted = status === 'COMPLETED';
  const isFailed = status === 'FAILED';
  const youtubeUrl = progressData?.youtubePlaylistUrl || activeJob?.youtubePlaylistUrl;

  const currentTrackTitle = progressData?.currentTrackTitle || 'Preparing transfer queue...';
  const currentArtist = progressData?.currentArtist || 'SyncStream Virtual Threads Engine';
  const currentAlbum = progressData?.currentAlbum || '';
  const matchedVideoId = progressData?.matchedVideoId;
  const isFromCache = progressData?.fromCache;
  const currentIdx = progressData?.currentTrackIndex || 0;
  const totalTracks = progressData?.totalTracks || activeJob?.totalTracks || 0;

  const recentTracks = progressData?.recentTracks || activeJob?.tracks || [];

  return (
    <div className="glass-panel rounded-2xl p-6 border border-slate-800 space-y-6 shadow-2xl">
      {/* Header & Status Indicator */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
        <div className="flex items-center space-x-3">
          <div className="relative">
            <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${
              isCompleted
                ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                : isFailed
                ? 'bg-red-500/20 text-red-400 border border-red-500/30'
                : 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
            }`}>
              {isCompleted ? (
                <CheckCircle2 className="w-5 h-5" />
              ) : isFailed ? (
                <AlertCircle className="w-5 h-5" />
              ) : (
                <Radio className="w-5 h-5 animate-pulse text-cyan-400" />
              )}
            </div>
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="text-lg font-bold text-white">
                {isCompleted
                  ? 'Transfer Complete!'
                  : isFailed
                  ? 'Transfer Encountered Issue'
                  : 'Transferring Playlist in Real-Time...'}
              </h2>
              <span className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full border ${
                isCompleted
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                  : isFailed
                  ? 'bg-red-500/10 text-red-400 border-red-500/30'
                  : 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30 animate-pulse'
              }`}>
                {status}
              </span>
            </div>
            <p className="text-xs text-slate-400">
              {progressData?.message || 'Processing tracks and matching audio streams...'}
            </p>
          </div>
        </div>

        {/* Action button if completed */}
        {isCompleted && (
          <button
            onClick={onReset}
            className="flex items-center space-x-1.5 px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>Transfer Another Playlist</span>
          </button>
        )}
      </div>

      {/* Dynamic Progress Bar */}
      <div className="space-y-2">
        <div className="flex justify-between text-xs font-semibold">
          <span className="text-slate-400">
            {isCompleted
              ? `All ${totalTracks} tracks processed`
              : `Processing Track ${currentIdx} of ${totalTracks}`}
          </span>
          <span className="text-emerald-400 font-mono font-bold text-sm">{progress}%</span>
        </div>
        <div className="h-3 w-full bg-slate-900 rounded-full overflow-hidden p-[1.5px] border border-slate-800">
          <div
            className="h-full rounded-full bg-gradient-to-r from-emerald-500 via-teal-400 to-cyan-400 transition-all duration-300 relative shadow-lg shadow-emerald-500/40"
            style={{ width: `${progress}%` }}
          >
            <div className="absolute right-0 top-0 bottom-0 w-2 bg-white rounded-full opacity-75 animate-pulse" />
          </div>
        </div>
      </div>

      {/* Currently Processing Track Card */}
      {!isCompleted && !isFailed && (
        <div className="p-4 rounded-xl bg-slate-900/90 border border-cyan-500/30 shadow-lg relative overflow-hidden">
          <div className="absolute top-0 right-0 px-3 py-1 bg-cyan-500/10 border-b border-l border-cyan-500/20 rounded-bl-lg text-[10px] font-bold text-cyan-400 flex items-center space-x-1">
            <Radio className="w-3 h-3 animate-ping" />
            <span>LIVE SYNCING</span>
          </div>

          <div className="flex items-center space-x-4">
            <div className="w-12 h-12 rounded-lg bg-slate-800 flex items-center justify-center text-cyan-400 shadow-inner">
              <Disc className="w-6 h-6 animate-spin-slow" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs text-slate-400 uppercase font-semibold tracking-wider">Current Match</p>
              <h3 className="text-sm font-bold text-white truncate">{currentTrackTitle}</h3>
              <p className="text-xs text-slate-400 truncate">{currentArtist} {currentAlbum ? `• ${currentAlbum}` : ''}</p>
            </div>
            <div className="text-right hidden sm:block">
              {isFromCache ? (
                <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 text-[10px] font-semibold border border-emerald-500/20">
                  <Database className="w-3 h-3" />
                  <span>Cache Hit (0 Quota)</span>
                </span>
              ) : matchedVideoId ? (
                <span className="inline-flex items-center space-x-1 px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 text-[10px] font-semibold border border-cyan-500/20">
                  <Zap className="w-3 h-3" />
                  <span>Matched: {matchedVideoId}</span>
                </span>
              ) : null}
            </div>
          </div>
        </div>
      )}

      {/* Completion Banner */}
      {isCompleted && youtubeUrl && (
        <div className="p-6 rounded-2xl bg-gradient-to-br from-emerald-950/80 via-slate-900 to-slate-950 border border-emerald-500/40 shadow-xl glow-emerald space-y-4">
          <div className="flex items-center space-x-3 text-emerald-400">
            <Sparkles className="w-6 h-6 animate-bounce" />
            <h3 className="text-base font-extrabold text-white">
              Successfully Created YouTube Playlist
            </h3>
          </div>
          <p className="text-xs text-slate-300">
            Your playlist has been assembled and is ready to play directly on YouTube / YouTube Music.
          </p>
          <div className="pt-2 flex flex-wrap items-center gap-3">
            <a
              href={youtubeUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-red-600 hover:bg-red-500 text-white text-xs font-bold shadow-lg shadow-red-950/40 transition active:scale-95"
            >
              <Youtube className="w-4 h-4" />
              <span>Open in YouTube / YouTube Music</span>
              <ExternalLink className="w-3.5 h-3.5 ml-1" />
            </a>
            <span className="text-xs font-mono text-slate-400 break-all">
              {youtubeUrl}
            </span>
          </div>
        </div>
      )}

      {/* Track List Preview / Progress Table */}
      {recentTracks.length > 0 && (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span className="font-semibold text-slate-300">Playlist Queue & Synchronized Items</span>
            <span>{recentTracks.length} items</span>
          </div>
          <div className="max-h-60 overflow-y-auto rounded-xl border border-slate-800 bg-slate-950/80 divide-y divide-slate-850">
            {recentTracks.map((track, idx) => {
              const isDone = track.status === 'INSERTED' || track.status === 'MATCHED';
              const isCurrent = !isCompleted && idx === currentIdx - 1;

              return (
                <div
                  key={track.id || idx}
                  className={`flex items-center justify-between px-3.5 py-2 text-xs transition ${
                    isCurrent
                      ? 'bg-cyan-950/30 text-cyan-200'
                      : isDone
                      ? 'text-slate-300 hover:bg-slate-900/50'
                      : 'text-slate-500 opacity-60'
                  }`}
                >
                  <div className="flex items-center space-x-3 min-w-0">
                    <span className="font-mono text-[11px] w-5 text-slate-500">{idx + 1}</span>
                    {track.imageUrl && (
                      <img
                        src={track.imageUrl}
                        alt={track.title}
                        className="w-7 h-7 rounded object-cover flex-shrink-0"
                      />
                    )}
                    <div className="truncate">
                      <p className="font-semibold text-white truncate">{track.title}</p>
                      <p className="text-[11px] text-slate-400 truncate">{track.mainArtist}</p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2 flex-shrink-0 ml-3">
                    {isDone ? (
                      <span className="flex items-center space-x-1 text-[10px] text-emerald-400 font-semibold px-2 py-0.5 rounded bg-emerald-500/10 border border-emerald-500/20">
                        <Check className="w-3 h-3" />
                        <span>Synced</span>
                      </span>
                    ) : isCurrent ? (
                      <span className="flex items-center space-x-1 text-[10px] text-cyan-400 font-semibold px-2 py-0.5 rounded bg-cyan-500/10 border border-cyan-500/20 animate-pulse">
                        <Radio className="w-3 h-3" />
                        <span>Matching</span>
                      </span>
                    ) : (
                      <span className="text-[10px] text-slate-500 px-2 py-0.5 rounded bg-slate-900 border border-slate-800">
                        Pending
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
