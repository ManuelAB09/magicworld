/**
 * Cryptographically secure random number generator.
 * Drop-in replacement for Math.random() that uses the Web Crypto API.
 * Returns a value in [0, 1) just like Math.random().
 */
export function secureRandom(): number {
  const array = new Uint32Array(1);
  crypto.getRandomValues(array);
  return array[0] / (0xFFFFFFFF + 1);
}

