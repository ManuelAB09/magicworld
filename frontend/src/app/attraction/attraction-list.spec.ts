import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AttractionList } from './attraction-list';
import { AttractionApiService, Attraction } from './attraction.service';
import { AuthService, Role } from '../auth/auth.service';
import { ErrorService } from '../error/error-service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';

describe('AttractionList', () => {
  let component: AttractionList;
  let fixture: ComponentFixture<AttractionList>;
  let mockApiService: jasmine.SpyObj<AttractionApiService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;
  let translateService: TranslateService;

  const mockAttractions: Attraction[] = [
    { id: 1, name: 'Roller Coaster', intensity: 'HIGH', category: 'ROLLER_COASTER', minimumHeight: 120, minimumAge: 10, minimumWeight: 30, description: 'Exciting ride', photoUrl: '/images/roller.jpg', isActive: true, mapPositionX: 30, mapPositionY: 40 },
    { id: 2, name: 'Carousel', intensity: 'LOW', category: 'CAROUSEL', minimumHeight: 80, minimumAge: 3, minimumWeight: 15, description: 'Fun for kids', photoUrl: '/images/carousel.jpg', isActive: true, mapPositionX: 60, mapPositionY: 70 }
  ];

  beforeEach(async () => {
    mockApiService = jasmine.createSpyObj('AttractionApiService', ['findAll', 'delete']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['checkRoleSecure'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    mockApiService.findAll.and.returnValue(of(mockAttractions));
    mockAuthService.checkRoleSecure.and.returnValue(of(Role.ADMIN));
    mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
    mockErrorService.getValidationMessages.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [AttractionList, TranslateModule.forRoot()],
      providers: [
        provideRouter([]),
        { provide: AttractionApiService, useValue: mockApiService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AttractionList);
    component = fixture.componentInstance;
    translateService = TestBed.inject(TranslateService);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load attractions on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.items.length).toBe(2);
    expect(component.items[0].name).toBe('Roller Coaster');
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

  it('should handle error when loading attractions fails', fakeAsync(() => {
    mockApiService.findAll.and.returnValue(throwError(() => ({ error: { code: 'error.test' } })));
    fixture.detectChanges();
    tick();
    expect(component.errorKey).toBe('error.test');
    expect(component.items.length).toBe(0);
  }));

  it('should apply filters correctly', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.filters = { minHeight: 100, minWeight: 20, minAge: 5 };
    component.applyFilters();
    tick();
    expect(mockApiService.findAll).toHaveBeenCalledWith({ minHeight: 100, minWeight: 20, minAge: 5 });
  }));

  it('should show validation error for negative filter values', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.filters = { minHeight: -10, minWeight: 0, minAge: 0 };
    component.applyFilters();
    expect(component.validationMessages.length).toBeGreaterThan(0);
  }));

  it('should clear filters', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.filters = { minHeight: 100, minWeight: 50, minAge: 10 };
    component.clearFilters();
    tick();
    expect(component.filters.minHeight).toBe(0);
    expect(component.filters.minWeight).toBe(0);
    expect(component.filters.minAge).toBe(0);
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

  it('should delete attraction when confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockApiService.delete.and.returnValue(of(void 0));
    fixture.detectChanges();
    tick();
    const initialCount = component.items.length;
    component.delete(1);
    tick();
    expect(component.items.length).toBe(initialCount - 1);
  }));

  it('should not delete attraction when cancelled', fakeAsync(() => {
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

