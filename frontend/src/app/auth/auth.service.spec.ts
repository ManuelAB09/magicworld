import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService, Role, UserDTO } from './auth.service';
import { Router } from '@angular/router';
import { HttpHeaders } from '@angular/common/http';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService,
        { provide: Router, useValue: mockRouter }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login successfully', () => {
    const credentials = { username: 'test', password: 'pass' };

    service.login(credentials).subscribe();

    const req = httpMock.expectOne(req => req.url.includes('/login'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credentials);
    req.flush({});
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should register successfully', () => {
    const data = {
      username: 'test',
      firstname: 'Test',
      lastname: 'User',
      email: 'test@test.com',
      password: 'pass123',
      confirmPassword: 'pass123'
    };

    service.register(data).subscribe();

    const req = httpMock.expectOne(req => req.url.includes('/register'));
    expect(req.request.method).toBe('POST');
    req.flush({});
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should check authentication status', () => {
    service.isAuthenticated().subscribe(isAuth => {
      expect(isAuth).toBeTrue();
    });

    const req = httpMock.expectOne(req => req.url.includes('/csrf-token'));
    req.flush({});
  });

  it('should return false when not authenticated', () => {
    service.isAuthenticated().subscribe(isAuth => {
      expect(isAuth).toBeFalse();
    });

    const req = httpMock.expectOne(req => req.url.includes('/csrf-token'));
    req.flush(null, { status: 401, statusText: 'Unauthorized' });
  });

  it('should notify auth changed', () => {
    let result: boolean | undefined;
    service.authChanged.subscribe(v => result = v);
    service.notifyAuthChanged(true);
    expect(result).toBeTrue();
  });

  it('should send forgot password request', () => {
    service.forgotPassword('test@test.com').subscribe();

    const req = httpMock.expectOne(req => req.url.includes('/forgot-password'));
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should send reset password request', () => {
    service.resetPassword('token123', 'newpass').subscribe();

    const req = httpMock.expectOne(req => req.url.includes('/reset-password'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'token123', newPassword: 'newpass' });
    req.flush({});
  });

  it('should get cookie value', () => {
    Object.defineProperty(document, 'cookie', {
      writable: true,
      value: 'XSRF-TOKEN=test-token'
    });
    const token = service.getCookie('XSRF-TOKEN');
    expect(token).toBe('test-token');
  });

  it('should return null for non-existent cookie', () => {
    Object.defineProperty(document, 'cookie', {
      writable: true,
      value: ''
    });
    const token = service.getCookie('NON_EXISTENT');
    expect(token).toBeNull();
  });

  it('should logout successfully', () => {
    Object.defineProperty(document, 'cookie', {
      writable: true,
      value: 'XSRF-TOKEN=test-token'
    });

    service.logout().subscribe();


    const logoutReq = httpMock.expectOne(req => req.url.includes('/logout'));
    expect(logoutReq.request.method).toBe('POST');
    logoutReq.flush({});
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should check role secure', () => {
    const mockUser: UserDTO = {
      username: 'admin',
      firstname: 'Admin',
      lastname: 'User',
      email: 'admin@test.com',
      role: Role.ADMIN
    };

    Object.defineProperty(document, 'cookie', {
      writable: true,
      value: 'XSRF-TOKEN=test-token'
    });

    service.checkRoleSecure().subscribe(role => {
      expect(role).toBe(Role.ADMIN);
    });

    const req = httpMock.expectOne(req => req.url.includes('/me'));
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('should get CSRF token from server', () => {
    service.getCsrfTokenFromServer().subscribe(token => {
      expect(token).toBe('server-token');
    });

    const req = httpMock.expectOne(req => req.url.includes('/csrf-token'));
    expect(req.request.method).toBe('GET');
    req.flush({}, { headers: { 'X-XSRF-TOKEN': 'server-token' } });
  });

  it('should return empty string when no CSRF header from server', () => {
    service.getCsrfTokenFromServer().subscribe(token => {
      expect(token).toBe('');
    });

    const req = httpMock.expectOne(req => req.url.includes('/csrf-token'));
    req.flush({});
  });

  it('should ensure CSRF token from cookie', () => {
    Object.defineProperty(document, 'cookie', {
      writable: true,
      value: 'XSRF-TOKEN=cookie-token'
    });

    const headers = new HttpHeaders();
    service.ensureCsrfToken(headers).subscribe(h => {
      expect(h.get('X-XSRF-TOKEN')).toBe('cookie-token');
    });
  });

  it('should fetch CSRF token from server when not in cookie', () => {
    Object.defineProperty(document, 'cookie', {
      writable: true,
      value: ''
    });

    const headers = new HttpHeaders();
    service.ensureCsrfToken(headers).subscribe(h => {
      expect(h.get('X-XSRF-TOKEN')).toBe('fetched-token');
    });

    const req = httpMock.expectOne(req => req.url.includes('/csrf-token'));
    req.flush({}, { headers: { 'X-XSRF-TOKEN': 'fetched-token' } });
  });
});

