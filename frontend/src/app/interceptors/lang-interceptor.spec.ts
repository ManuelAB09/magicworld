import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { LangInterceptor } from './lang-interceptor';
import { TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';

describe('LangInterceptor', () => {
  let interceptor: LangInterceptor;
  let mockTranslateService: jasmine.SpyObj<TranslateService>;

  beforeEach(() => {
    mockTranslateService = jasmine.createSpyObj('TranslateService', ['getDefaultLang'], {
      currentLang: 'es'
    });
    mockTranslateService.getDefaultLang.and.returnValue('en');

    TestBed.configureTestingModule({
      providers: [
        LangInterceptor,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: TranslateService, useValue: mockTranslateService }
      ]
    });

    interceptor = TestBed.inject(LangInterceptor);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should add Accept-Language header with current language', () => {
    const mockRequest = new HttpRequest('GET', '/api/test');
    const mockHandler: HttpHandler = {
      handle: jasmine.createSpy('handle').and.returnValue(of({}))
    };

    interceptor.intercept(mockRequest, mockHandler);

    expect(mockHandler.handle).toHaveBeenCalled();
    const calledRequest = (mockHandler.handle as jasmine.Spy).calls.mostRecent().args[0] as HttpRequest<any>;
    expect(calledRequest.headers.get('Accept-Language')).toBe('es');
  });

  it('should use default language when currentLang is not set', () => {
    const noLangTranslate = jasmine.createSpyObj('TranslateService', ['getDefaultLang'], {
      currentLang: null
    });
    noLangTranslate.getDefaultLang.and.returnValue('en');

    const newInterceptor = new LangInterceptor(noLangTranslate);
    const mockRequest = new HttpRequest('GET', '/api/test');
    const mockHandler: HttpHandler = {
      handle: jasmine.createSpy('handle').and.returnValue(of({}))
    };

    newInterceptor.intercept(mockRequest, mockHandler);

    const calledRequest = (mockHandler.handle as jasmine.Spy).calls.mostRecent().args[0] as HttpRequest<any>;
    expect(calledRequest.headers.get('Accept-Language')).toBe('en');
  });

  it('should clone request with header', () => {
    const mockRequest = new HttpRequest('POST', '/api/test', { data: 'test' });
    const mockHandler: HttpHandler = {
      handle: jasmine.createSpy('handle').and.returnValue(of({}))
    };

    interceptor.intercept(mockRequest, mockHandler);

    const calledRequest = (mockHandler.handle as jasmine.Spy).calls.mostRecent().args[0] as HttpRequest<any>;
    expect(calledRequest.url).toBe('/api/test');
    expect(calledRequest.method).toBe('POST');
  });
});
