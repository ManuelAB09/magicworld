import { getBackendBaseUrl } from '../../config/backend';

const apiBase = getBackendBaseUrl();

/**
 * Convierte una URL de imagen relativa a absoluta usando el backend base URL.
 * Si la URL ya es absoluta (empieza con http), la devuelve sin cambios.
 */
export function getImageUrl(url: string | null | undefined): string | null {
  if (!url) return null;
  if (url.startsWith('http')) return url;
  return apiBase + url;
}

