import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ResetPasswordForm } from './reset-password-form';
import { AuthService } from '../../auth.service';
import { ErrorService } from '../../../error/error-service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { provideRouter, ActivatedRoute, Router } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';

describe('ResetPasswordForm', () => {
  let component: ResetPasswordForm;
  let fixture: ComponentFixture<ResetPasswordForm>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['resetPassword'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ResetPasswordForm, TranslateModule.forRoot(), ReactiveFormsModule],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (key: string) => key === 'token' ? 'valid-token' : null
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get token from query params', () => {
    expect(component.token).toBe('valid-token');
  });

  it('should initialize form with empty fields', () => {
    expect(component.resetForm.get('password')?.value).toBe('');
    expect(component.resetForm.get('confirmPassword')?.value).toBe('');
  });

  it('should have password field required', () => {
    expect(component.resetForm.get('password')?.hasError('required')).toBeTrue();
  });

  it('should validate minimum password length', () => {
    component.resetForm.patchValue({ password: 'short' });
    expect(component.resetForm.get('password')?.hasError('minlength')).toBeTrue();
  });

  it('should validate passwords match', () => {
    component.resetForm.patchValue({ password: 'password123', confirmPassword: 'different' });
    expect(component.resetForm.hasError('mismatch')).toBeTrue();
  });

  it('should accept matching passwords', () => {
    component.resetForm.patchValue({ password: 'password123', confirmPassword: 'password123' });
    expect(component.resetForm.hasError('mismatch')).toBeFalse();
  });

  it('should not submit if form is invalid', () => {
    component.onSubmit();
    expect(mockAuthService.resetPassword).not.toHaveBeenCalled();
  });

  it('should not submit if token is null', fakeAsync(async () => {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [ResetPasswordForm, TranslateModule.forRoot(), ReactiveFormsModule],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: { get: () => null }
            }
          }
        }
      ]
    }).compileComponents();

    const newFixture = TestBed.createComponent(ResetPasswordForm);
    const newComponent = newFixture.componentInstance;
    newFixture.detectChanges();
    newComponent.resetForm.patchValue({ password: 'password123', confirmPassword: 'password123' });
    newComponent.onSubmit();
    expect(mockAuthService.resetPassword).not.toHaveBeenCalled();
  }));

  it('should call resetPassword on valid submit', fakeAsync(() => {
    mockAuthService.resetPassword.and.returnValue(of({}));
    component.resetForm.patchValue({ password: 'password123', confirmPassword: 'password123' });
    component.onSubmit();
    tick();
    expect(mockAuthService.resetPassword).toHaveBeenCalledWith('valid-token', 'password123');
  }));

  it('should show success message and navigate on successful reset', fakeAsync(() => {
    mockAuthService.resetPassword.and.returnValue(of({}));
    component.resetForm.patchValue({ password: 'password123', confirmPassword: 'password123' });
    component.onSubmit();
    tick();
    expect(component.successMessage).not.toBe('');
    tick(2000);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('should handle error on failed reset', fakeAsync(() => {
    mockAuthService.resetPassword.and.returnValue(throwError(() => ({ error: { code: 'error.reset' } })));
    mockErrorService.handleError.and.returnValue({ code: 'error.reset', args: {} });
    mockErrorService.getValidationMessages.and.returnValue(['Reset error']);
    component.resetForm.patchValue({ password: 'password123', confirmPassword: 'password123' });
    component.onSubmit();
    tick();
    expect(component.errorCode).toBe('error.reset');
    expect(component.errorMessages).toEqual(['Reset error']);
  }));

  it('should return password control', () => {
    expect(component.password).toBe(component.resetForm.get('password'));
  });

  it('should return confirmPassword control', () => {
    expect(component.confirmPassword).toBe(component.resetForm.get('confirmPassword'));
  });
});

