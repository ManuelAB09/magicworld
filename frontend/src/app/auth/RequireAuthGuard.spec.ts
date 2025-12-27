import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RequireAuthGuard } from './RequireAuthGuard';
import { AuthService } from './auth.service';
import { of, throwError } from 'rxjs';

describe('RequireAuthGuard', () => {
  let guard: RequireAuthGuard;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        RequireAuthGuard,
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    guard = TestBed.inject(RequireAuthGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access when user is authenticated', (done) => {
    mockAuthService.isAuthenticated.and.returnValue(of(true));

    guard.canActivate().subscribe(result => {
      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
      done();
    });
  });

  it('should deny access and redirect to login when user is not authenticated', (done) => {
    mockAuthService.isAuthenticated.and.returnValue(of(false));

    guard.canActivate().subscribe(result => {
      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
      done();
    });
  });

  it('should deny access and redirect on error', (done) => {
    mockAuthService.isAuthenticated.and.returnValue(throwError(() => new Error('Auth error')));

    guard.canActivate().subscribe(result => {
      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
      done();
    });
  });
});

