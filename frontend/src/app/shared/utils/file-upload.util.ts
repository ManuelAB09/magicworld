export interface FileValidationResult {
  valid: boolean;
  error?: 'invalid_type' | 'size_exceeded' | 'no_file';
}

export function validateImageFile(file: File | null, maxFileBytes: number): FileValidationResult {
  if (!file) {
    return { valid: false, error: 'no_file' };
  }
  if (!file.type.startsWith('image/')) {
    return { valid: false, error: 'invalid_type' };
  }
  if (file.size > maxFileBytes) {
    return { valid: false, error: 'size_exceeded' };
  }
  return { valid: true };
}

export function readFileAsDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(file);
  });
}

export const DEFAULT_MAX_FILE_BYTES = 50 * 1024 * 1024;

