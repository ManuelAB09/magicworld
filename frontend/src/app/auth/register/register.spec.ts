import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Register } from './register';
import { AuthService } from '../auth.service';
import { ErrorService } from '../../error/error-service';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError, Subject } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['register', 'notifyAuthChanged'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    await TestBed.configureTestingModule({
      imports: [Register, TranslateModule.forRoot(), FormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty form fields', () => {
    expect(component.username).toBe('');
    expect(component.firstname).toBe('');
    expect(component.lastname).toBe('');
    expect(component.email).toBe('');
    expect(component.password).toBe('');
    expect(component.confirmPassword).toBe('');
  });

  it('should have password visibility options hidden by default', () => {
    expect(component.showPassword).toBeFalse();
    expect(component.showConfirmPassword).toBeFalse();
  });

  it('should toggle password visibility', () => {
    expect(component.showPassword).toBeFalse();
    component.togglePasswordVisibility();
    expect(component.showPassword).toBeTrue();
  });

  it('should toggle confirm password visibility', () => {
    expect(component.showConfirmPassword).toBeFalse();
    component.toggleConfirmPasswordVisibility();
    expect(component.showConfirmPassword).toBeTrue();
  });

  it('should call register service on onRegister', fakeAsync(() => {
    mockAuthService.register.and.returnValue(of({}));
    component.username = 'testuser';
    component.firstname = 'Test';
    component.lastname = 'User';
    component.email = 'test@test.com';
    component.password = 'password123';
    component.confirmPassword = 'password123';
    component.onRegister();
    tick();
    expect(mockAuthService.register).toHaveBeenCalledWith({
      username: 'testuser',
      firstname: 'Test',
      lastname: 'User',
      email: 'test@test.com',
      password: 'password123',
      confirmPassword: 'password123'
    });
    expect(mockAuthService.notifyAuthChanged).toHaveBeenCalledWith(true);
  }));

  it('should clear error on successful registration', fakeAsync(() => {
    mockAuthService.register.and.returnValue(of({}));
    component.errorCode = 'some.error';
    component.errorMessages = ['error'];
    component.username = 'test';
    component.firstname = 'Test';
    component.lastname = 'User';
    component.email = 'test@test.com';
    component.password = 'pass';
    component.confirmPassword = 'pass';
    component.onRegister();
    tick();
    expect(component.errorCode).toBeNull();
    expect(component.errorMessages.length).toBe(0);
  }));

  it('should handle registration error', fakeAsync(() => {
    mockAuthService.register.and.returnValue(throwError(() => ({ error: { code: 'error.register' } })));
    mockErrorService.handleError.and.returnValue({ code: 'error.register', args: {} });
    mockErrorService.getValidationMessages.and.returnValue(['Register error']);
    component.username = 'test';
    component.firstname = 'Test';
    component.lastname = 'User';
    component.email = 'test@test.com';
    component.password = 'pass';
    component.confirmPassword = 'pass';
    component.onRegister();
    tick();
    expect(component.errorCode).toBe('error.register');
    expect(component.errorMessages).toEqual(['Register error']);
  }));
});

