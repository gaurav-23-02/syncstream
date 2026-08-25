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

/**
 * Subscribes to the backend Server-Sent Events (SSE) stream for a given transfer jobId.
 *
 * @param {string} jobId
 * @param {object} callbacks - { onProgress, onComplete, onError }
 * @returns {() => void} cleanup function to close the EventSource
 */
export function subscribeTransferProgress(jobId, { onProgress, onComplete, onError }) {
  const url = `${API_BASE}/transfer/progress/${jobId}`;
  console.log(`[SSE] Subscribing to transfer stream: ${url}`);

  const eventSource = new EventSource(url, { withCredentials: true });

  eventSource.addEventListener('progress', (e) => {
    try {
      const data = JSON.parse(e.data);
      if (onProgress) onProgress(data);
    } catch (err) {
      console.error('[SSE] Failed to parse progress event data:', err);
    }
  });

  eventSource.addEventListener('complete', (e) => {
    try {
      const data = JSON.parse(e.data);
      if (onComplete) onComplete(data);
    } catch (err) {
      console.error('[SSE] Failed to parse complete event data:', err);
    }
    eventSource.close();
  });

  eventSource.addEventListener('error', (e) => {
    console.warn('[SSE] EventSource encountered error or was closed by server');
    if (onError) onError(e);
    eventSource.close();
  });

  return () => {
    console.log(`[SSE] Closing connection for job: ${jobId}`);
    eventSource.close();
  };
}
