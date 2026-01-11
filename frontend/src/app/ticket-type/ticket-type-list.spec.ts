import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TicketTypeList } from './ticket-type-list';
import { TicketTypeApiService, TicketType } from './ticket-type.service';
import { AuthService, Role } from '../auth/auth.service';
import { ErrorService } from '../error/error-service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';

describe('TicketTypeList', () => {
  let component: TicketTypeList;
  let fixture: ComponentFixture<TicketTypeList>;
  let mockApiService: jasmine.SpyObj<TicketTypeApiService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;

  const mockTicketTypes: TicketType[] = [
    { id: 1, typeName: 'Adult', description: 'Adult ticket', cost: 50, maxPerDay: 100, photoUrl: '/images/adult.jpg' },
    { id: 2, typeName: 'Child', description: 'Child ticket', cost: 25, maxPerDay: 50, photoUrl: '/images/child.jpg' }
  ];

  beforeEach(async () => {
    mockApiService = jasmine.createSpyObj('TicketTypeApiService', ['findAll', 'delete']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['checkRoleSecure'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    mockApiService.findAll.and.returnValue(of(mockTicketTypes));
    mockAuthService.checkRoleSecure.and.returnValue(of(Role.ADMIN));
    mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
    mockErrorService.getValidationMessages.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [TicketTypeList, TranslateModule.forRoot()],
      providers: [
        provideRouter([]),
        { provide: TicketTypeApiService, useValue: mockApiService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TicketTypeList);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load ticket types on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.items.length).toBe(2);
    expect(component.items[0].typeName).toBe('Adult');
  }));

  it('should set isAdmin to true when user is admin', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.isAdmin).toBeTrue();
  }));

  it('should set isAdmin to false when user is not admin', fakeAsync(() => {
    mockAuthService.checkRoleSecure.and.returnValue(of(Role.USER));
    fixture.detectChanges();
    tick();
    expect(component.isAdmin).toBeFalse();
  }));

  it('should handle error when loading ticket types fails', fakeAsync(() => {
    mockApiService.findAll.and.returnValue(throwError(() => ({ error: { code: 'error.load' } })));
    fixture.detectChanges();
    tick();
    expect(component.errorKey).toBe('error.test');
    expect(component.items.length).toBe(0);
  }));

  it('should return correct image URL for relative path', () => {
    const result = component.getImageUrl('/images/test.jpg');
    expect(result).toContain('/images/test.jpg');
  });

  it('should return same URL for absolute path', () => {
    const result = component.getImageUrl('https://example.com/image.jpg');
    expect(result).toBe('https://example.com/image.jpg');
  });

  it('should return null for null/undefined image URL', () => {
    expect(component.getImageUrl(null)).toBeNull();
    expect(component.getImageUrl(undefined)).toBeNull();
  });

  it('should delete ticket type when confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockApiService.delete.and.returnValue(of(void 0));
    fixture.detectChanges();
    tick();
    const initialCount = component.items.length;
    component.delete(1);
    tick();
    expect(component.items.length).toBe(initialCount - 1);
  }));

  it('should not delete ticket type when cancelled', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(false);
    fixture.detectChanges();
    tick();
    const initialCount = component.items.length;
    component.delete(1);
    tick();
    expect(component.items.length).toBe(initialCount);
  }));

  it('should handle delete error', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockApiService.delete.and.returnValue(throwError(() => ({ error: { code: 'error.delete' } })));
    fixture.detectChanges();
    tick();
    component.delete(1);
    tick();
    expect(component.errorKey).toBe('error.test');
  }));
});

