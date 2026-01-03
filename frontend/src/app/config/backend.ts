const PRODUCTION_HOSTS = ['vercel.app', 'onrender.com', '.now.sh', '.vercel.app'];
const PRODUCTION_URL = 'https://magicworld-vpum.onrender.com';
const LOCAL_URL = 'http://localhost:8080';

export function getBackendBaseUrl(): string {
  if (typeof window === 'undefined') {
    return LOCAL_URL;
  }

  const w = window as any;
  const envUrl = findEnvUrl(w);
  if (envUrl) return envUrl;

  if (isProductionHost(window.location.hostname)) {
    return PRODUCTION_URL;
  }

  return LOCAL_URL;
}

function findEnvUrl(w: any): string | null {
  const candidates = [
    w.NEXT_PUBLIC_BACKEND_URL,
    w.VITE_BACKEND_URL,
    w.REACT_APP_BACKEND_URL,
    w.__BACKEND_URL,
    w.__env?.NEXT_PUBLIC_BACKEND_URL
  ];

  const found = candidates.find(c => typeof c === 'string' && c.trim());
  return found ? found.trim() : null;
}

function isProductionHost(hostname: string): boolean {
  return PRODUCTION_HOSTS.some(h => hostname.includes(h) || hostname.endsWith(h));
}
