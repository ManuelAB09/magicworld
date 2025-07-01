import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ErrorService {
  handleError(err: any): { code: string, args: any } {
    const code = err.error?.code || 'error.unexpected';
    const args = Array.isArray(err.error?.args)
      ? err.error.args.reduce((acc: any, val: any, idx: number) => {
        acc[idx] = val;
        return acc;
      }, {})
      : {};
    return { code, args };
  }
}
