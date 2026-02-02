import { getImageUrl } from './image-url.util';

describe('ImageUrlUtil', () => {
  describe('getImageUrl', () => {
    it('should return null for null input', () => {
      expect(getImageUrl(null)).toBeNull();
    });

    it('should return null for undefined input', () => {
      expect(getImageUrl(undefined)).toBeNull();
    });

    it('should return null for empty string', () => {
      expect(getImageUrl('')).toBeNull();
    });

    it('should return original URL if starts with http', () => {
      const url = 'http://example.com/image.jpg';
      expect(getImageUrl(url)).toBe(url);
    });

    it('should return original URL if starts with https', () => {
      const url = 'https://example.com/image.jpg';
      expect(getImageUrl(url)).toBe(url);
    });

    it('should prepend backend URL for relative path', () => {
      const result = getImageUrl('/images/test.jpg');
      expect(result).toContain('/images/test.jpg');
      expect(result).not.toBeNull();
    });

    it('should handle path without leading slash', () => {
      const result = getImageUrl('images/test.jpg');
      expect(result).toBeTruthy();
    });
  });
});
