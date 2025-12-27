import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { EmailForm } from './email-form';
import { AuthService } from '../../auth.service';
import { ErrorService } from '../../../error/error-service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of, throwError, Subject } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';

describe('EmailForm', () => {
  let component: EmailForm;
  let fixture: ComponentFixture<EmailForm>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;
  let translateService: TranslateService;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['forgotPassword'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    await TestBed.configureTestingModule({
      imports: [EmailForm, TranslateModule.forRoot(), ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmailForm);
    component = fixture.componentInstance;
    translateService = TestBed.inject(TranslateService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty email', () => {
    expect(component.emailForm.get('email')?.value).toBe('');
  });

  it('should have email field required', () => {
    expect(component.emailForm.get('email')?.hasError('required')).toBeTrue();
  });

  it('should validate email format', () => {
    component.emailForm.patchValue({ email: 'invalid-email' });
    expect(component.emailForm.get('email')?.hasError('email')).toBeTrue();
  });

  it('should accept valid email', () => {
    component.emailForm.patchValue({ email: 'test@example.com' });
    expect(component.emailForm.valid).toBeTrue();
  });

  it('should not submit if form is invalid', () => {
    component.onSubmit();
    expect(mockAuthService.forgotPassword).not.toHaveBeenCalled();
  });

  it('should call forgotPassword on valid submit', fakeAsync(() => {
    mockAuthService.forgotPassword.and.returnValue(of({}));
    component.emailForm.patchValue({ email: 'test@example.com' });
    component.onSubmit();
    tick();
    expect(mockAuthService.forgotPassword).toHaveBeenCalledWith('test@example.com');
  }));

  it('should show success message on successful submit', fakeAsync(() => {
    mockAuthService.forgotPassword.and.returnValue(of({}));
    component.emailForm.patchValue({ email: 'test@example.com' });
    component.onSubmit();
    tick();
    expect(component.successMessage).not.toBe('');
    expect(component.errorCode).toBeNull();
  }));

  it('should reset form on successful submit', fakeAsync(() => {
    mockAuthService.forgotPassword.and.returnValue(of({}));
    component.emailForm.patchValue({ email: 'test@example.com' });
    component.onSubmit();
    tick();
    expect(component.submitted).toBeFalse();
  }));

  it('should handle error on failed submit', fakeAsync(() => {
    mockAuthService.forgotPassword.and.returnValue(throwError(() => ({ error: { code: 'error.email' } })));
    mockErrorService.handleError.and.returnValue({ code: 'error.email', args: {} });
    mockErrorService.getValidationMessages.and.returnValue(['Email error']);
    component.emailForm.patchValue({ email: 'test@example.com' });
    component.onSubmit();
    tick();
    expect(component.errorCode).toBe('error.email');
    expect(component.errorMessages).toEqual(['Email error']);
  }));

  it('should return email control', () => {
    expect(component.email).toBe(component.emailForm.get('email'));
  });
});

