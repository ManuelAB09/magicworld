import { TestBed } from '@angular/core/testing';
import { ErrorService } from './error-service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

describe('ErrorService', () => {
  let service: ErrorService;
  let translateService: TranslateService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [ErrorService]
    });

    service = TestBed.inject(ErrorService);
    translateService = TestBed.inject(TranslateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('handleError', () => {
    it('should return error code from response', () => {
      const err = { error: { code: 'error.test', args: {} } };
      const result = service.handleError(err);
      expect(result.code).toBe('error.test');
    });

    it('should return default code when no code in response', () => {
      const err = { error: {} };
      const result = service.handleError(err);
      expect(result.code).toBe('error.unexpected');
    });

    it('should return default code when error is null', () => {
      const err = { error: null };
      const result = service.handleError(err);
      expect(result.code).toBe('error.unexpected');
    });

    it('should convert array args to object for non-validation errors', () => {
      const err = { error: { code: 'error.test', args: ['arg1', 'arg2'] } };
      const result = service.handleError(err);
      expect(result.args).toEqual({ 0: 'arg1', 1: 'arg2' });
    });

    it('should keep args as object if already object', () => {
      const err = { error: { code: 'error.test', args: { key: 'value' } } };
      const result = service.handleError(err);
      expect(result.args).toEqual({ key: 'value' });
    });

    it('should return empty object for non-object/non-array args', () => {
      const err = { error: { code: 'error.test', args: 'string' } };
      const result = service.handleError(err);
      expect(result.args).toEqual({});
    });

    it('should keep args as array for validation errors', () => {
      const err = { error: { code: 'error.validation', args: [{ field: 'error.field' }] } };
      const result = service.handleError(err);
      expect(Array.isArray(result.args)).toBeTrue();
    });

    it('should convert non-array args to empty array for validation errors', () => {
      const err = { error: { code: 'error.validation', args: 'invalid' } };
      const result = service.handleError(err);
      expect(result.args).toEqual([]);
    });
  });

  describe('getValidationMessages', () => {
    it('should return empty array for non-validation errors', () => {
      const result = service.getValidationMessages('error.test', {});
      expect(result).toEqual([]);
    });

    it('should return empty array for validation error without array args', () => {
      const result = service.getValidationMessages('error.validation', {});
      expect(result).toEqual([]);
    });

    it('should return empty array for validation error with empty array', () => {
      const result = service.getValidationMessages('error.validation', []);
      expect(result).toEqual([]);
    });

    it('should return translated messages for validation errors', () => {
      spyOn(translateService, 'instant').and.callFake((key: string) => `Translated: ${key}`);
      const args = [{ field1: 'error.field1', field2: 'error.field2' }];
      const result = service.getValidationMessages('error.validation', args);
      expect(result.length).toBe(2);
      expect(result[0]).toContain('Translated:');
    });

    it('should filter out non-string values from validation messages', () => {
      spyOn(translateService, 'instant').and.callFake((key: string) => key);
      const args = [{ field1: 'error.field1', field2: 123 }];
      const result = service.getValidationMessages('error.validation', args);
      expect(result.length).toBe(1);
    });
  });
});

