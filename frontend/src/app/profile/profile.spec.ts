import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { ProfileComponent } from './profile';
import { ProfileService, PurchaseDTO, UpdateProfileRequest } from './profile.service';
import { AuthService, UserProfile, Role } from '../auth/auth.service';
import { ErrorService } from '../error/error-service';
import { of, throwError, Subject } from 'rxjs';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let mockProfileService: jasmine.SpyObj<ProfileService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockProfile: UserProfile = {
    username: 'testuser',
    firstname: 'Test',
    lastname: 'User',
    email: 'test@example.com',
    role: Role.USER
  };

  const mockPurchases: PurchaseDTO[] = [
    {
      id: 1,
      purchaseDate: '2026-01-01',
      lines: [
        { id: 1, validDate: '2026-01-10', quantity: 2, totalCost: 59.80, ticketTypeName: 'Adult' },
        { id: 2, validDate: '2026-01-10', quantity: 1, totalCost: 19.90, ticketTypeName: 'Child' }
      ]
    }
  ];

  beforeEach(async () => {
    mockProfileService = jasmine.createSpyObj('ProfileService', ['updateProfile', 'deleteProfile', 'getMyPurchases']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getProfile', 'notifyAuthChanged'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    mockAuthService.getProfile.and.returnValue(of(mockProfile));
    mockProfileService.getMyPurchases.and.returnValue(of(mockPurchases));
    mockProfileService.updateProfile.and.returnValue(of(mockProfile));
    mockProfileService.deleteProfile.and.returnValue(of(void 0));
    mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
    mockErrorService.getValidationMessages.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [
        ProfileComponent,
        HttpClientTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: ProfileService, useValue: mockProfileService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load profile on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.profile).toBeTruthy();
    expect(component.profile?.username).toBe('testuser');
    expect(component.loading).toBeFalse();
  }));

  it('should show profile tab by default', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.activeTab).toBe('profile');
  }));

  it('should switch to purchases tab', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.switchTab('purchases');
    tick();
    expect(component.activeTab).toBe('purchases');
    expect(mockProfileService.getMyPurchases).toHaveBeenCalled();
  }));

  it('should load purchases when switching to purchases tab', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.switchTab('purchases');
    tick();
    expect(component.purchases.length).toBe(1);
  }));

  it('should open edit form with profile data', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.openEditForm();
    expect(component.showEditForm).toBeTrue();
    expect(component.editFirstname).toBe('Test');
    expect(component.editLastname).toBe('User');
    expect(component.editEmail).toBe('test@example.com');
    expect(component.editPassword).toBe('');
  }));

  it('should close edit form', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.showEditForm = true;
    component.closeEditForm();
    expect(component.showEditForm).toBeFalse();
  }));

  it('should calculate total cost correctly', () => {
    const purchase: PurchaseDTO = {
      id: 1,
      purchaseDate: '2026-01-01',
      lines: [
        { id: 1, validDate: '2026-01-10', quantity: 2, totalCost: 59.80, ticketTypeName: 'Adult' },
        { id: 2, validDate: '2026-01-10', quantity: 1, totalCost: 19.90, ticketTypeName: 'Child' }
      ]
    };
    expect(component.getTotalCost(purchase)).toBeCloseTo(79.70, 2);
  });

  it('should save profile successfully', fakeAsync(() => {
    const updatedProfile: UserProfile = { ...mockProfile, firstname: 'Updated' };
    mockProfileService.updateProfile.and.returnValue(of(updatedProfile));

    fixture.detectChanges();
    tick();

    component.openEditForm();
    component.editFirstname = 'Updated';
    component.saveProfile();
    tick();

    expect(mockProfileService.updateProfile).toHaveBeenCalled();
    expect(component.showEditForm).toBeFalse();
    expect(component.profile?.firstname).toBe('Updated');
    expect(component.successMessage).toBe('PROFILE.UPDATE_SUCCESS');
  }));

  it('should not save profile with empty fields', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    component.openEditForm();
    component.editFirstname = '';
    component.saveProfile();

    expect(mockProfileService.updateProfile).not.toHaveBeenCalled();
  }));

  it('should handle save profile error', fakeAsync(() => {
    mockProfileService.updateProfile.and.returnValue(throwError(() => ({ error: { code: 'error.test' } })));

    fixture.detectChanges();
    tick();

    component.openEditForm();
    component.saveProfile();
    tick();

    expect(component.saving).toBeFalse();
  }));

  it('should delete profile successfully', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);

    fixture.detectChanges();
    tick();

    component.confirmDelete();
    tick();

    expect(mockProfileService.deleteProfile).toHaveBeenCalled();
    expect(mockAuthService.notifyAuthChanged).toHaveBeenCalledWith(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  }));

  it('should not delete profile if not confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(false);

    fixture.detectChanges();
    tick();

    component.confirmDelete();

    expect(mockProfileService.deleteProfile).not.toHaveBeenCalled();
  }));

  it('should handle delete profile error', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockProfileService.deleteProfile.and.returnValue(throwError(() => ({ error: { code: 'error.test' } })));

    fixture.detectChanges();
    tick();

    component.confirmDelete();
    tick();

    expect(component.deleting).toBeFalse();
  }));

  it('should handle profile load error', fakeAsync(() => {
    mockAuthService.getProfile.and.returnValue(throwError(() => new Error('Load error')));
    fixture.detectChanges();
    tick();
    expect(component.profile).toBeNull();
    expect(component.loading).toBeFalse();
  }));

  it('should not reload purchases if already loaded', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    component.switchTab('purchases');
    tick();
    expect(mockProfileService.getMyPurchases).toHaveBeenCalledTimes(1);

    component.switchTab('profile');
    component.switchTab('purchases');
    expect(mockProfileService.getMyPurchases).toHaveBeenCalledTimes(1);
  }));

  it('should save profile with password change', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    component.openEditForm();
    component.editPassword = 'NewPassword123!';
    component.saveProfile();
    tick();

    expect(mockProfileService.updateProfile).toHaveBeenCalledWith(jasmine.objectContaining({
      password: 'NewPassword123!'
    }));
  }));

  it('should save profile without password when empty', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    component.openEditForm();
    component.editPassword = '';
    component.saveProfile();
    tick();

    expect(mockProfileService.updateProfile).toHaveBeenCalledWith(jasmine.objectContaining({
      password: undefined
    }));
  }));
});
