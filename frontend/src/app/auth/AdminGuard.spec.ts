import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AdminGuard } from './AdminGuard';
import { AuthService, Role } from './auth.service';
import { of, throwError } from 'rxjs';

describe('AdminGuard', () => {
  let guard: AdminGuard;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['checkRoleSecure']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AdminGuard,
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    guard = TestBed.inject(AdminGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should allow access for admin users', (done) => {
    mockAuthService.checkRoleSecure.and.returnValue(of(Role.ADMIN));

    guard.canActivate().subscribe(result => {
      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
      done();
    });
  });

  it('should deny access and redirect for non-admin users', (done) => {
    mockAuthService.checkRoleSecure.and.returnValue(of(Role.USER));

    guard.canActivate().subscribe(result => {
      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/discounts']);
      done();
    });
  });

  it('should deny access and redirect on null role', (done) => {
    mockAuthService.checkRoleSecure.and.returnValue(of(null));

    guard.canActivate().subscribe(result => {
      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/discounts']);
      done();
    });
  });

  it('should deny access and redirect on error', (done) => {
    mockAuthService.checkRoleSecure.and.returnValue(throwError(() => new Error('Auth error')));

    guard.canActivate().subscribe(result => {
      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/discounts']);
      done();
    });
  });
});

