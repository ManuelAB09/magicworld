import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';

@Injectable({ providedIn: 'root' })
export class LangInterceptor implements HttpInterceptor {
  constructor(private translate: TranslateService) {}
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const lang = this.translate.currentLang || this.translate.getDefaultLang();
    const cloned = req.clone({
      setHeaders: { 'Accept-Language': lang }
    });
    return next.handle(cloned);
  }
}
