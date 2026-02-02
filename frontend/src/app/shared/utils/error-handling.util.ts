import { ErrorService } from '../../error/error-service';

export interface ErrorState {
  errorKey: string | null;
  errorArgs: any;
  validationMessages: string[];
}

export function createInitialErrorState(): ErrorState {
  return {
    errorKey: null,
    errorArgs: null,
    validationMessages: []
  };
}

export function clearErrorState(state: ErrorState): void {
  state.errorKey = null;
  state.errorArgs = null;
  state.validationMessages = [];
}

export function handleApiError(err: any, errorService: ErrorService): ErrorState {
  const mapped = errorService.handleError(err);
  return {
    errorKey: mapped.code,
    errorArgs: mapped.args,
    validationMessages: errorService.getValidationMessages(mapped.code, mapped.args)
  };
}

export function applyErrorState(target: ErrorState, source: ErrorState): void {
  target.errorKey = source.errorKey;
  target.errorArgs = source.errorArgs;
  target.validationMessages = source.validationMessages;
}

