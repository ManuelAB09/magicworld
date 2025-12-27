import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Navbar } from './navbar';
import { AuthService, Role } from '../auth/auth.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';

describe('Navbar', () => {
  let component: Navbar;
  let fixture: ComponentFixture<Navbar>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let authChangedSubject: Subject<boolean>;

  beforeEach(async () => {
    authChangedSubject = new Subject<boolean>();
    mockAuthService = jasmine.createSpyObj('AuthService', ['isAuthenticated', 'checkRoleSecure', 'logout', 'notifyAuthChanged'], {
      authChanged: authChangedSubject
    });

    mockAuthService.isAuthenticated.and.returnValue(of(false));
    mockAuthService.checkRoleSecure.and.returnValue(of(null));
    mockAuthService.logout.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [Navbar, TranslateModule.forRoot()],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Navbar);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with isAuthenticated false', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.isAuthenticated).toBeFalse();
  }));

  it('should check authentication on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(mockAuthService.isAuthenticated).toHaveBeenCalled();
  }));

  it('should update isAuthenticated when auth changes', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.isAuthenticated).toBeFalse();
    authChangedSubject.next(true);
    tick();
    expect(component.isAuthenticated).toBeTrue();
  }));

  it('should get role when authenticated', fakeAsync(() => {
    mockAuthService.isAuthenticated.and.returnValue(of(true));
    mockAuthService.checkRoleSecure.and.returnValue(of(Role.ADMIN));
    fixture.detectChanges();
    tick();
    authChangedSubject.next(true);
    tick();
    expect(mockAuthService.checkRoleSecure).toHaveBeenCalled();
  }));

  it('should reset role when logged out', fakeAsync(() => {
    component.role = Role.ADMIN;
    fixture.detectChanges();
    tick();
    authChangedSubject.next(false);
    tick();
    expect(component.role).toBeNull();
  }));

  it('should call logout on logout()', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.logout();
    tick();
    expect(mockAuthService.logout).toHaveBeenCalled();
  }));

  it('should notify auth changed after logout', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.logout();
    tick();
    expect(mockAuthService.notifyAuthChanged).toHaveBeenCalledWith(false);
  }));

  it('should reset role after logout', fakeAsync(() => {
    component.role = Role.ADMIN;
    fixture.detectChanges();
    tick();
    component.logout();
    tick();
    expect(component.role).toBeNull();
  }));

  it('should change language on changeLang', fakeAsync(() => {
    const translateService = TestBed.inject(TranslateService);
    spyOn(translateService, 'use');
    fixture.detectChanges();
    tick();
    const mockEvent = { target: { value: 'en' } } as unknown as Event;
    component.changeLang(mockEvent);
    expect(component.currentLang).toBe('en');
    expect(translateService.use).toHaveBeenCalledWith('en');
  }));

  it('should default to es when no language selected', fakeAsync(() => {
    const translateService = TestBed.inject(TranslateService);
    spyOn(translateService, 'use');
    fixture.detectChanges();
    tick();
    const mockEvent = { target: null } as unknown as Event;
    component.changeLang(mockEvent);
    expect(component.currentLang).toBe('es');
  }));

  it('should have swagger URL configured', () => {
    expect(component.swaggerUrl).toContain('/swagger-ui/index.html');
  });
});

