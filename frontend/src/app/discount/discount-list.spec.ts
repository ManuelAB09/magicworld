import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { DiscountList } from './discount-list';
import { DiscountApiService, Discount } from './discount.service';
import { AuthService, Role } from '../auth/auth.service';
import { ErrorService } from '../error/error-service';
import { TranslateModule } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of, throwError, Subject } from 'rxjs';

describe('DiscountList', () => {
  let component: DiscountList;
  let fixture: ComponentFixture<DiscountList>;
  let mockApiService: jasmine.SpyObj<DiscountApiService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;

  const mockDiscounts: Discount[] = [
    { id: 1, discountCode: 'SUMMER20', discountPercentage: 20, expiryDate: '2025-12-31' },
    { id: 2, discountCode: 'WINTER10', discountPercentage: 10, expiryDate: '2025-06-30' }
  ];

  beforeEach(async () => {
    mockApiService = jasmine.createSpyObj('DiscountApiService', ['findAll', 'getTicketTypesByDiscount', 'delete']);
    mockAuthService = jasmine.createSpyObj('AuthService', ['checkRoleSecure'], {
      authChanged: new Subject<boolean>()
    });
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    mockApiService.findAll.and.returnValue(of(mockDiscounts));
    mockApiService.getTicketTypesByDiscount.and.returnValue(of([{ typeName: 'Adult' }]));
    mockAuthService.checkRoleSecure.and.returnValue(of(Role.ADMIN));
    mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
    mockErrorService.getValidationMessages.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [DiscountList, TranslateModule.forRoot()],
      providers: [
        provideRouter([]),
        { provide: DiscountApiService, useValue: mockApiService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DiscountList);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load discounts on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.discounts.length).toBe(2);
    expect(component.discounts[0].discountCode).toBe('SUMMER20');
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

  it('should load applied ticket types for each discount', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(mockApiService.getTicketTypesByDiscount).toHaveBeenCalledWith(1);
    expect(mockApiService.getTicketTypesByDiscount).toHaveBeenCalledWith(2);
    expect(component.appliedTypesMap[1]).toEqual(['Adult']);
  }));

  it('should handle error when loading discounts fails', fakeAsync(() => {
    mockApiService.findAll.and.returnValue(throwError(() => ({ error: { code: 'error.load' } })));
    fixture.detectChanges();
    tick();
    expect(component.errorKey).toBe('error.test');
    expect(component.discounts.length).toBe(0);
  }));

  it('should delete discount when confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockApiService.delete.and.returnValue(of(void 0));
    fixture.detectChanges();
    tick();
    const initialCount = component.discounts.length;
    component.onDelete(mockDiscounts[0]);
    tick();
    expect(component.discounts.length).toBe(initialCount - 1);
  }));

  it('should not delete discount when cancelled', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(false);
    fixture.detectChanges();
    tick();
    const initialCount = component.discounts.length;
    component.onDelete(mockDiscounts[0]);
    tick();
    expect(component.discounts.length).toBe(initialCount);
  }));

  it('should handle delete error', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockApiService.delete.and.returnValue(throwError(() => ({ error: { code: 'error.delete' } })));
    fixture.detectChanges();
    tick();
    component.onDelete(mockDiscounts[0]);
    tick();
    expect(component.errorKey).toBe('error.test');
  }));

  it('should not delete discount without id', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    const discountWithoutId: Discount = { discountCode: 'TEST', discountPercentage: 10, expiryDate: '2025-12-31' };
    component.onDelete(discountWithoutId);
    expect(mockApiService.delete).not.toHaveBeenCalled();
  }));

  it('should track deleting state per discount', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockApiService.delete.and.returnValue(of(void 0));
    fixture.detectChanges();
    tick();
    expect(component.deleting[1]).toBeFalsy();
  }));
});

