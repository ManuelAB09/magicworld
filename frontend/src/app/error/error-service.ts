import { Injectable } from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

@Injectable({ providedIn: 'root' })
export class ErrorService {
  constructor(private translate: TranslateService) {}
  handleError(err: any): { code: string, args: any } {
    const code = err.error?.code || 'error.unexpected';
    let args = err.error?.args;
    if (code !== 'error.validation') {
      if (Array.isArray(args)) {
        args = args.reduce((acc, val, idx) => ({ ...acc, [idx]: val }), {});
      } else if (typeof args !== 'object' || args === null) {
        args = {};
      }
    } else if (!Array.isArray(args)) {
      args = [];
    }
    return { code, args };
  }
  getValidationMessages(code: string, args: any): string[] {
    if (
      code === 'error.validation' &&
      Array.isArray(args) &&
      args.length > 0 &&
      typeof args[0] === 'object'
    ) {
      const values = Object.values(args[0]);
      return values
        .filter((msgCode): msgCode is string => typeof msgCode === 'string')
        .map(msgCode => this.translate.instant(msgCode));
    }
    return [];
  }
}
