import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Login } from './login';
import { AuthService } from '../auth.service';
import { ErrorService } from '../../error/error-service';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['login', 'notifyAuthChanged'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    await TestBed.configureTestingModule({
      imports: [Login, TranslateModule.forRoot(), FormsModule],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty credentials', () => {
    expect(component.username).toBe('');
    expect(component.password).toBe('');
  });

  it('should have showPassword false by default', () => {
    expect(component.showPassword).toBeFalse();
  });

  it('should toggle password visibility', () => {
    expect(component.showPassword).toBeFalse();
    component.togglePasswordVisibility();
    expect(component.showPassword).toBeTrue();
    component.togglePasswordVisibility();
    expect(component.showPassword).toBeFalse();
  });

  it('should call login service on onLogin', fakeAsync(() => {
    mockAuthService.login.and.returnValue(of({}));
    component.username = 'testuser';
    component.password = 'testpass';
    component.onLogin();
    tick();
    expect(mockAuthService.login).toHaveBeenCalledWith({ username: 'testuser', password: 'testpass' });
    expect(mockAuthService.notifyAuthChanged).toHaveBeenCalledWith(true);
  }));

  it('should clear error on successful login', fakeAsync(() => {
    mockAuthService.login.and.returnValue(of({}));
    component.errorCode = 'some.error';
    component.errorMessages = ['error'];
    component.username = 'test';
    component.password = 'pass';
    component.onLogin();
    tick();
    expect(component.errorCode).toBeNull();
    expect(component.errorMessages.length).toBe(0);
  }));

  it('should handle login error', fakeAsync(() => {
    mockAuthService.login.and.returnValue(throwError(() => ({ error: { code: 'error.auth' } })));
    mockErrorService.handleError.and.returnValue({ code: 'error.auth', args: {} });
    mockErrorService.getValidationMessages.and.returnValue(['Auth error']);
    component.username = 'test';
    component.password = 'wrongpass';
    component.onLogin();
    tick();
    expect(component.errorCode).toBe('error.auth');
    expect(component.errorMessages).toEqual(['Auth error']);
  }));
});

