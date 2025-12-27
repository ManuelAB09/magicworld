import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { DiscountForm } from './discount-form';
import { DiscountApiService, Discount, DiscountRequest } from './discount.service';
import { TicketTypeApiService, TicketType } from '../ticket-type/ticket-type.service';
import { ErrorService } from '../error/error-service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';
import { Component } from '@angular/core';

@Component({ template: '', standalone: true })
class DummyComponent {}

describe('DiscountForm', () => {
  let component: DiscountForm;
  let fixture: ComponentFixture<DiscountForm>;
  let mockApiService: jasmine.SpyObj<DiscountApiService>;
  let mockTicketTypeService: jasmine.SpyObj<TicketTypeApiService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;

  const mockDiscount: Discount = {
    id: 1,
    discountCode: 'SUMMER20',
    discountPercentage: 20,
    expiryDate: '2025-12-31'
  };

  const mockTicketTypes: TicketType[] = [
    { id: 1, typeName: 'Adult', description: 'Adult ticket', cost: 50, currency: 'EUR', maxPerDay: 100, photoUrl: '/img.jpg' },
    { id: 2, typeName: 'Child', description: 'Child ticket', cost: 25, currency: 'EUR', maxPerDay: 50, photoUrl: '/img2.jpg' }
  ];

  beforeEach(async () => {
    mockApiService = jasmine.createSpyObj('DiscountApiService', ['findById', 'create', 'update', 'delete', 'getTicketTypesByDiscount']);
    mockTicketTypeService = jasmine.createSpyObj('TicketTypeApiService', ['findAll']);
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    mockTicketTypeService.findAll.and.returnValue(of(mockTicketTypes));
    mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
    mockErrorService.getValidationMessages.and.returnValue([]);

    await TestBed.configureTestingModule({
      imports: [DiscountForm, TranslateModule.forRoot(), ReactiveFormsModule],
      providers: [
        provideRouter([{ path: 'discounts', component: DummyComponent }]),
        { provide: DiscountApiService, useValue: mockApiService },
        { provide: TicketTypeApiService, useValue: mockTicketTypeService },
        { provide: ErrorService, useValue: mockErrorService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => null } }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DiscountForm);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form for new discount', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.isEdit).toBeFalse();
    expect(component.form.get('discountCode')?.value).toBe('');
  }));

  it('should load ticket types on init', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    expect(component.allTicketTypes.length).toBe(2);
  }));

  it('should have required validators', () => {
    fixture.detectChanges();
    expect(component.form.get('discountPercentage')?.hasError('required')).toBeTrue();
    expect(component.form.get('expiryDate')?.hasError('required')).toBeTrue();
    expect(component.form.get('discountCode')?.hasError('required')).toBeTrue();
  });

  it('should validate discount percentage range', () => {
    fixture.detectChanges();
    component.form.patchValue({ discountPercentage: 0 });
    expect(component.form.get('discountPercentage')?.hasError('min')).toBeTrue();
    component.form.patchValue({ discountPercentage: 101 });
    expect(component.form.get('discountPercentage')?.hasError('max')).toBeTrue();
  });

  it('should toggle ticket type selection', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.toggleTypeName('Adult', true);
    expect(component.selectedNames.has('Adult')).toBeTrue();
    component.toggleTypeName('Adult', false);
    expect(component.selectedNames.has('Adult')).toBeFalse();
  }));

  it('should not submit if form is invalid', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.submit();
    expect(mockApiService.create).not.toHaveBeenCalled();
  }));

  it('should show error when no ticket types selected', fakeAsync(() => {
    fixture.detectChanges();
    tick();
    component.form.patchValue({
      discountCode: 'TEST',
      discountPercentage: 10,
      expiryDate: '2025-12-31'
    });
    component.submit();
    expect(component.errorKey).toBe('DISCOUNT_FORM.TYPES_REQUIRED');
  }));

  it('should call create on submit for new discount', fakeAsync(() => {
    mockApiService.create.and.returnValue(of(mockDiscount));
    fixture.detectChanges();
    tick();
    component.form.patchValue({
      discountCode: 'TEST',
      discountPercentage: 10,
      expiryDate: '2025-12-31'
    });
    component.toggleTypeName('Adult', true);
    component.submit();
    tick();
    expect(mockApiService.create).toHaveBeenCalled();
  }));

  describe('Edit mode', () => {
    beforeEach(async () => {
      await TestBed.resetTestingModule();
      mockApiService = jasmine.createSpyObj('DiscountApiService', ['findById', 'create', 'update', 'delete', 'getTicketTypesByDiscount']);
      mockTicketTypeService = jasmine.createSpyObj('TicketTypeApiService', ['findAll']);
      mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

      mockTicketTypeService.findAll.and.returnValue(of(mockTicketTypes));
      mockApiService.findById.and.returnValue(of(mockDiscount));
      mockApiService.getTicketTypesByDiscount.and.returnValue(of([{ typeName: 'Adult' }]));
      mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });
      mockErrorService.getValidationMessages.and.returnValue([]);

      await TestBed.configureTestingModule({
        imports: [DiscountForm, TranslateModule.forRoot(), ReactiveFormsModule],
        providers: [
          provideRouter([{ path: 'discounts', component: DummyComponent }]),
          { provide: DiscountApiService, useValue: mockApiService },
          { provide: TicketTypeApiService, useValue: mockTicketTypeService },
          { provide: ErrorService, useValue: mockErrorService },
          {
            provide: ActivatedRoute,
            useValue: {
              snapshot: { paramMap: { get: () => '1' } }
            }
          }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(DiscountForm);
      component = fixture.componentInstance;
    });

    it('should load existing discount in edit mode', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      expect(component.isEdit).toBeTrue();
      expect(component.id).toBe(1);
      expect(component.form.get('discountCode')?.value).toBe('SUMMER20');
    }));

    it('should load associated ticket types in edit mode', fakeAsync(() => {
      fixture.detectChanges();
      tick();
      expect(component.selectedNames.has('Adult')).toBeTrue();
    }));

    it('should call update on submit in edit mode', fakeAsync(() => {
      mockApiService.update.and.returnValue(of(mockDiscount));
      fixture.detectChanges();
      tick();
      component.submit();
      tick();
      expect(mockApiService.update).toHaveBeenCalled();
    }));

    it('should delete discount when confirmed', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(true);
      mockApiService.delete.and.returnValue(of(void 0));
      fixture.detectChanges();
      tick();
      component.delete();
      tick();
      expect(mockApiService.delete).toHaveBeenCalledWith(1);
    }));

    it('should not delete discount when cancelled', fakeAsync(() => {
      spyOn(window, 'confirm').and.returnValue(false);
      fixture.detectChanges();
      tick();
      component.delete();
      tick();
      expect(mockApiService.delete).not.toHaveBeenCalled();
    }));
  });
});

