import { of, throwError } from 'rxjs';
import { checkAdminRole } from './admin-check.util';
import { AuthService, Role } from '../../auth/auth.service';

describe('AdminCheckUtil', () => {
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['checkRoleSecure']);
  });

  describe('checkAdminRole', () => {
    it('should return true for ADMIN role', (done) => {
      mockAuthService.checkRoleSecure.and.returnValue(of(Role.ADMIN));

      checkAdminRole(mockAuthService).subscribe(result => {
        expect(result).toBeTrue();
        done();
      });
    });

    it('should return false for USER role', (done) => {
      mockAuthService.checkRoleSecure.and.returnValue(of(Role.USER));

      checkAdminRole(mockAuthService).subscribe(result => {
        expect(result).toBeFalse();
        done();
      });
    });

    it('should return false on error', (done) => {
      mockAuthService.checkRoleSecure.and.returnValue(throwError(() => new Error('Auth error')));

      checkAdminRole(mockAuthService).subscribe(result => {
        expect(result).toBeFalse();
        done();
      });
    });

    it('should call checkRoleSecure on auth service', () => {
      mockAuthService.checkRoleSecure.and.returnValue(of(Role.USER));

      checkAdminRole(mockAuthService).subscribe();

      expect(mockAuthService.checkRoleSecure).toHaveBeenCalled();
    });
  });
});
