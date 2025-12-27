import { getBackendBaseUrl } from './backend';

describe('Backend Config', () => {

  afterEach(() => {
    // Reset window properties
    (window as any).NEXT_PUBLIC_BACKEND_URL = undefined;
    (window as any).VITE_BACKEND_URL = undefined;
    (window as any).REACT_APP_BACKEND_URL = undefined;
    (window as any).__BACKEND_URL = undefined;
    (window as any).__env = undefined;
  });

  it('should return localhost:8080 as default', () => {
    expect(getBackendBaseUrl()).toBe('http://localhost:8080');
  });

  it('should use NEXT_PUBLIC_BACKEND_URL if set', () => {
    (window as any).NEXT_PUBLIC_BACKEND_URL = 'https://next.example.com';
    expect(getBackendBaseUrl()).toBe('https://next.example.com');
  });

  it('should use VITE_BACKEND_URL if set', () => {
    (window as any).VITE_BACKEND_URL = 'https://vite.example.com';
    expect(getBackendBaseUrl()).toBe('https://vite.example.com');
  });

  it('should use REACT_APP_BACKEND_URL if set', () => {
    (window as any).REACT_APP_BACKEND_URL = 'https://react.example.com';
    expect(getBackendBaseUrl()).toBe('https://react.example.com');
  });

  it('should use __BACKEND_URL if set', () => {
    (window as any).__BACKEND_URL = 'https://custom.example.com';
    expect(getBackendBaseUrl()).toBe('https://custom.example.com');
  });

  it('should use __env.NEXT_PUBLIC_BACKEND_URL if set', () => {
    (window as any).__env = { NEXT_PUBLIC_BACKEND_URL: 'https://env.example.com' };
    expect(getBackendBaseUrl()).toBe('https://env.example.com');
  });

  it('should trim whitespace from URL', () => {
    (window as any).NEXT_PUBLIC_BACKEND_URL = '  https://trimmed.example.com  ';
    expect(getBackendBaseUrl()).toBe('https://trimmed.example.com');
  });

  it('should skip empty string values', () => {
    (window as any).NEXT_PUBLIC_BACKEND_URL = '';
    (window as any).VITE_BACKEND_URL = 'https://vite.example.com';
    expect(getBackendBaseUrl()).toBe('https://vite.example.com');
  });

  it('should skip whitespace-only values', () => {
    (window as any).NEXT_PUBLIC_BACKEND_URL = '   ';
    (window as any).VITE_BACKEND_URL = 'https://vite.example.com';
    expect(getBackendBaseUrl()).toBe('https://vite.example.com');
  });
});

