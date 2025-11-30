export function getBackendBaseUrl(): string {
  if (typeof window !== 'undefined') {
    const w = window as any;
    const candidates = [
      w.NEXT_PUBLIC_BACKEND_URL,
      w.VITE_BACKEND_URL,
      w.REACT_APP_BACKEND_URL,
      w.__BACKEND_URL,
      w.__env && w.__env.NEXT_PUBLIC_BACKEND_URL
    ];
    for (const c of candidates) {
      if (typeof c === 'string' && c.trim()) return c.trim();
    }

    const host = window.location.hostname || '';
    if (host.includes('vercel.app') || host.includes('onrender.com') || host.endsWith('.now.sh') || host.endsWith('.vercel.app')) {
      return 'https://magicworld-vpum.onrender.com';
    }
  }

  return 'http://localhost:8080';
}
