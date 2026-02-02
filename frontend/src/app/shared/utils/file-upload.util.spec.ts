import {
  validateImageFile,
  readFileAsDataUrl,
  DEFAULT_MAX_FILE_BYTES,
  FileValidationResult
} from './file-upload.util';

describe('FileUploadUtil', () => {
  describe('validateImageFile', () => {
    it('should return no_file error when file is null', () => {
      const result = validateImageFile(null, DEFAULT_MAX_FILE_BYTES);
      expect(result.valid).toBeFalse();
      expect(result.error).toBe('no_file');
    });

    it('should return invalid_type error for non-image file', () => {
      const file = new File(['content'], 'test.txt', { type: 'text/plain' });
      const result = validateImageFile(file, DEFAULT_MAX_FILE_BYTES);
      expect(result.valid).toBeFalse();
      expect(result.error).toBe('invalid_type');
    });

    it('should return size_exceeded error for large file', () => {
      const largeContent = new Array(1024 * 1024 + 1).fill('a').join('');
      const file = new File([largeContent], 'test.jpg', { type: 'image/jpeg' });
      const result = validateImageFile(file, 1024 * 1024);
      expect(result.valid).toBeFalse();
      expect(result.error).toBe('size_exceeded');
    });

    it('should return valid for correct image file', () => {
      const file = new File(['image content'], 'test.jpg', { type: 'image/jpeg' });
      const result = validateImageFile(file, DEFAULT_MAX_FILE_BYTES);
      expect(result.valid).toBeTrue();
      expect(result.error).toBeUndefined();
    });

    it('should accept png images', () => {
      const file = new File(['png content'], 'test.png', { type: 'image/png' });
      const result = validateImageFile(file, DEFAULT_MAX_FILE_BYTES);
      expect(result.valid).toBeTrue();
    });

    it('should accept gif images', () => {
      const file = new File(['gif content'], 'test.gif', { type: 'image/gif' });
      const result = validateImageFile(file, DEFAULT_MAX_FILE_BYTES);
      expect(result.valid).toBeTrue();
    });

    it('should reject pdf files', () => {
      const file = new File(['pdf content'], 'test.pdf', { type: 'application/pdf' });
      const result = validateImageFile(file, DEFAULT_MAX_FILE_BYTES);
      expect(result.valid).toBeFalse();
      expect(result.error).toBe('invalid_type');
    });
  });

  describe('readFileAsDataUrl', () => {
    it('should read file as data URL', async () => {
      const file = new File(['test content'], 'test.txt', { type: 'text/plain' });
      const result = await readFileAsDataUrl(file);
      expect(result).toContain('data:');
    });

    it('should return base64 encoded content', async () => {
      const file = new File(['hello'], 'test.txt', { type: 'text/plain' });
      const result = await readFileAsDataUrl(file);
      expect(result).toContain('base64');
    });
  });

  describe('DEFAULT_MAX_FILE_BYTES', () => {
    it('should be 50MB', () => {
      expect(DEFAULT_MAX_FILE_BYTES).toBe(50 * 1024 * 1024);
    });
  });
});
