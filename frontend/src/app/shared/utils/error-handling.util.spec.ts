import {
  createInitialErrorState,
  clearErrorState,
  handleApiError,
  applyErrorState,
  ErrorState
} from './error-handling.util';
import { ErrorService } from '../../error/error-service';

describe('ErrorHandlingUtil', () => {
  describe('createInitialErrorState', () => {
    it('should create initial state with null values', () => {
      const state = createInitialErrorState();
      expect(state.errorKey).toBeNull();
      expect(state.errorArgs).toBeNull();
    });

    it('should create initial state with empty validation messages', () => {
      const state = createInitialErrorState();
      expect(state.validationMessages).toEqual([]);
    });
  });

  describe('clearErrorState', () => {
    it('should clear errorKey to null', () => {
      const state: ErrorState = {
        errorKey: 'some.error',
        errorArgs: { field: 'test' },
        validationMessages: ['Error 1']
      };
      clearErrorState(state);
      expect(state.errorKey).toBeNull();
    });

    it('should clear errorArgs to null', () => {
      const state: ErrorState = {
        errorKey: 'some.error',
        errorArgs: { field: 'test' },
        validationMessages: ['Error 1']
      };
      clearErrorState(state);
      expect(state.errorArgs).toBeNull();
    });

    it('should clear validationMessages to empty array', () => {
      const state: ErrorState = {
        errorKey: 'some.error',
        errorArgs: { field: 'test' },
        validationMessages: ['Error 1', 'Error 2']
      };
      clearErrorState(state);
      expect(state.validationMessages).toEqual([]);
    });
  });

  describe('handleApiError', () => {
    let mockErrorService: jasmine.SpyObj<ErrorService>;

    beforeEach(() => {
      mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);
    });

    it('should return error state from error service', () => {
      mockErrorService.handleError.and.returnValue({ code: 'error.code', args: { id: 1 } });
      mockErrorService.getValidationMessages.and.returnValue(['Validation message']);

      const result = handleApiError({ status: 400 }, mockErrorService);

      expect(result.errorKey).toBe('error.code');
      expect(result.errorArgs).toEqual({ id: 1 });
    });

    it('should include validation messages', () => {
      mockErrorService.handleError.and.returnValue({ code: 'error.code', args: null });
      mockErrorService.getValidationMessages.and.returnValue(['Message 1', 'Message 2']);

      const result = handleApiError({ status: 400 }, mockErrorService);

      expect(result.validationMessages).toEqual(['Message 1', 'Message 2']);
    });
  });

  describe('applyErrorState', () => {
    it('should copy errorKey from source to target', () => {
      const target: ErrorState = createInitialErrorState();
      const source: ErrorState = {
        errorKey: 'new.error',
        errorArgs: { value: 123 },
        validationMessages: ['New message']
      };

      applyErrorState(target, source);

      expect(target.errorKey).toBe('new.error');
    });

    it('should copy errorArgs from source to target', () => {
      const target: ErrorState = createInitialErrorState();
      const source: ErrorState = {
        errorKey: 'new.error',
        errorArgs: { value: 123 },
        validationMessages: ['New message']
      };

      applyErrorState(target, source);

      expect(target.errorArgs).toEqual({ value: 123 });
    });

    it('should copy validationMessages from source to target', () => {
      const target: ErrorState = createInitialErrorState();
      const source: ErrorState = {
        errorKey: 'new.error',
        errorArgs: null,
        validationMessages: ['Msg 1', 'Msg 2']
      };

      applyErrorState(target, source);

      expect(target.validationMessages).toEqual(['Msg 1', 'Msg 2']);
    });
  });
});
