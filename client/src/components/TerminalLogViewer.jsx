import React, { useState, useEffect, useRef } from 'react';
import { Terminal, Copy, Check, Trash2, ArrowDownCircle } from 'lucide-react';

export default function TerminalLogViewer({ logs = [] }) {
  const [autoScroll, setAutoScroll] = useState(true);
  const [copied, setCopied] = useState(false);
  const terminalEndRef = useRef(null);

  useEffect(() => {
    if (autoScroll && terminalEndRef.current) {
      terminalEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [logs, autoScroll]);

  const handleCopyLogs = () => {
    const text = logs
      .map((l) => `[${l.timestamp ? new Date(l.timestamp).toLocaleTimeString() : 'LOG'}] [${l.level || 'INFO'}] ${l.message} ${l.details ? '- ' + l.details : ''}`)
      .join('\n');
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const getLevelColor = (level) => {
    switch (level?.toUpperCase()) {
      case 'SUCCESS':
        return 'text-emerald-400 font-bold';
      case 'ERROR':
        return 'text-red-400 font-bold';
      case 'WARN':
        return 'text-amber-400 font-bold';
      case 'INFO':
      default:
        return 'text-cyan-400 font-bold';
    }
  };

  return (
    <div className="rounded-2xl bg-slate-950 border border-slate-800 shadow-2xl overflow-hidden font-mono text-xs">
      {/* Terminal Title Bar */}
      <div className="flex items-center justify-between px-4 py-2.5 bg-slate-900/90 border-b border-slate-800 select-none">
        <div className="flex items-center space-x-2">
          <div className="flex space-x-1.5">
            <div className="w-3 h-3 rounded-full bg-red-500/80" />
            <div className="w-3 h-3 rounded-full bg-yellow-500/80" />
            <div className="w-3 h-3 rounded-full bg-emerald-500/80" />
          </div>
          <span className="text-slate-400 text-[11px] font-sans font-semibold ml-2 flex items-center space-x-1.5">
            <Terminal className="w-3.5 h-3.5 text-cyan-400" />
            <span>syncstream-engine.log (Live Virtual Thread Output)</span>
          </span>
        </div>

        <div className="flex items-center space-x-2 font-sans">
          {/* Auto Scroll Toggle */}
          <button
            onClick={() => setAutoScroll(!autoScroll)}
            className={`flex items-center space-x-1 px-2 py-1 rounded text-[10px] font-semibold border transition ${
              autoScroll
                ? 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30'
                : 'bg-slate-800 text-slate-400 border-slate-700'
            }`}
          >
            <ArrowDownCircle className="w-3 h-3" />
            <span>Auto-Scroll: {autoScroll ? 'ON' : 'OFF'}</span>
          </button>

          {/* Copy Logs */}
          <button
            onClick={handleCopyLogs}
            className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded transition border border-transparent hover:border-slate-700"
            title="Copy all logs"
          >
            {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
          </button>
        </div>
      </div>

      {/* Terminal Body */}
      <div className="p-4 h-64 overflow-y-auto space-y-1.5 bg-slate-950/95 leading-relaxed">
        {logs.length === 0 ? (
          <div className="text-slate-600 italic py-8 text-center">
            Waiting for transfer engine activity...
          </div>
        ) : (
          logs.map((log, index) => {
            const timeStr = log.timestamp
              ? new Date(log.timestamp).toLocaleTimeString()
              : new Date().toLocaleTimeString();

            return (
              <div key={log.id || index} className="flex items-start space-x-2 text-slate-300 hover:bg-slate-900/40 px-1 rounded transition">
                <span className="text-slate-600 select-none shrink-0 font-light">[{timeStr}]</span>
                <span className={`shrink-0 ${getLevelColor(log.level)}`}>
                  [{log.level || 'INFO'}]
                </span>
                <span className="text-slate-200">{log.message}</span>
                {log.details && (
                  <span className="text-slate-500 text-[11px] truncate">({log.details})</span>
                )}
              </div>
            );
          })
        )}
        <div ref={terminalEndRef} />
      </div>
    </div>
  );
}
