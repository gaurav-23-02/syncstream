import React from 'react';
import { History, ExternalLink, CheckCircle2, AlertCircle, Clock, Youtube } from 'lucide-react';

export default function TransferHistory({ history = [], onSelectJob }) {
  if (!history || history.length === 0) return null;

  return (
    <div className="glass-panel rounded-2xl p-6 border border-slate-800 space-y-4">
      <div className="flex items-center justify-between pb-3 border-b border-slate-800">
        <h3 className="text-base font-bold text-white flex items-center space-x-2">
          <History className="w-4 h-4 text-emerald-400" />
          <span>Recent Transfer Jobs</span>
        </h3>
        <span className="text-xs text-slate-500 font-semibold">{history.length} jobs</span>
      </div>

      <div className="divide-y divide-slate-850 overflow-hidden">
        {history.map((job) => {
          const isDone = job.status === 'COMPLETED';
          const isFailed = job.status === 'FAILED';

          return (
            <div
              key={job.jobId}
              className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-slate-900/40 px-2 rounded-xl transition"
            >
              <div className="flex items-center space-x-3 min-w-0">
                <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${
                  isDone
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                    : isFailed
                    ? 'bg-red-500/10 text-red-400 border border-red-500/20'
                    : 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 animate-pulse'
                }`}>
                  {isDone ? <CheckCircle2 className="w-4 h-4" /> : isFailed ? <AlertCircle className="w-4 h-4" /> : <Clock className="w-4 h-4" />}
                </div>
                <div className="min-w-0">
                  <h4 className="text-sm font-bold text-white truncate">{job.targetPlaylistName}</h4>
                  <p className="text-xs text-slate-400 truncate">
                    {job.matchedTracks} / {job.totalTracks} tracks synced • {job.cacheHits} cache hits
                  </p>
                </div>
              </div>

              <div className="flex items-center space-x-3 self-end sm:self-auto shrink-0">
                <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-full border ${
                  isDone
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                    : isFailed
                    ? 'bg-red-500/10 text-red-400 border-red-500/20'
                    : 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20'
                }`}>
                  {job.status}
                </span>

                {job.youtubePlaylistUrl && (
                  <a
                    href={job.youtubePlaylistUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center space-x-1 px-3 py-1 rounded-lg bg-red-600/20 hover:bg-red-600/30 text-red-400 hover:text-red-300 border border-red-500/30 text-xs font-semibold transition"
                  >
                    <Youtube className="w-3.5 h-3.5 text-red-500" />
                    <span>View</span>
                    <ExternalLink className="w-3 h-3 ml-0.5" />
                  </a>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
