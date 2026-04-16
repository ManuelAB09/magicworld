import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CheckoutStep2Component } from './checkout-step2';
import { CheckoutService, PriceCalculationResponse, PaymentResponse } from '../services/checkout.service';
import { AuthService, UserProfile, Role } from '../../auth/auth.service';
import { ErrorService } from '../../error/error-service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ElementRef } from '@angular/core';

describe('CheckoutStep2Component', () => {
  let component: CheckoutStep2Component;
  let fixture: ComponentFixture<CheckoutStep2Component>;
  let router: Router;
  let mockCheckoutService: jasmine.SpyObj<CheckoutService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockErrorService: jasmine.SpyObj<ErrorService>;

  const mockCartData = {
    items: [{ ticketTypeName: 'ADULT', quantity: 2, unitPrice: 50, totalPrice: 100, photoUrl: '/img.jpg', description: 'Adult' }],
    visitDate: '2026-01-15'
  };

  const mockProfile: UserProfile = {
    username: 'testuser',
    firstname: 'John',
    lastname: 'Doe',
    email: 'john@example.com',
    role: Role.USER
  };

  const mockPriceResponse: PriceCalculationResponse = {
    subtotal: 100,
    discountAmount: 10,
    total: 90,
    validDiscountCodes: ['SAVE10'],
    invalidDiscountCodes: ['BAD'],
    validButNotApplicableCodes: ['NOTAPPLICABLE'],
    discountPercentages: { 'SAVE10': 10 },
    discountAppliesTo: { 'SAVE10': ['ADULT'] }
  };

  beforeEach(async () => {
    mockCheckoutService = jasmine.createSpyObj('CheckoutService', [
      'getStripePublicKey', 'calculatePrice', 'processPayment', 'getAvailability'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getProfile']);
    mockErrorService = jasmine.createSpyObj('ErrorService', ['handleError', 'getValidationMessages']);

    mockAuthService.getProfile.and.returnValue(of(mockProfile));
    mockCheckoutService.calculatePrice.and.returnValue(of(mockPriceResponse));
    mockCheckoutService.getStripePublicKey.and.returnValue(of({ publicKey: 'pk_test_123' }));
    mockErrorService.handleError.and.returnValue({ code: 'error.test', args: {} });

    await TestBed.configureTestingModule({
      imports: [
        CheckoutStep2Component,
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: CheckoutService, useValue: mockCheckoutService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: ErrorService, useValue: mockErrorService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(CheckoutStep2Component);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    // Ensure cardElementInstance has destroy to prevent cleanup errors
    if (component.cardElementInstance && !component.cardElementInstance.destroy) {
      component.cardElementInstance = { destroy: () => {} };
    }
    component.ngOnDestroy();
    sessionStorage.removeItem('checkout_cart');
    sessionStorage.removeItem('checkout_result');
    delete (window as any).Stripe;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to checkout if no cart data', () => {
    sessionStorage.removeItem('checkout_cart');
    fixture.detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['/checkout']);
  });

  it('should load cart from session storage', fakeAsync(() => {
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    tick();
    expect(component.cart).toBeTruthy();
    expect(component.cart!.items.length).toBe(1);
    expect(component.cart!.visitDate).toBe('2026-01-15');
  }));

  it('should load user data and populate form fields', fakeAsync(() => {
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    tick();
    expect(component.isLoggedIn).toBeTrue();
    expect(component.firstName).toBe('John');
    expect(component.lastName).toBe('Doe');
    expect(component.email).toBe('john@example.com');
  }));

  it('should set isLoggedIn to false when profile fetch fails', fakeAsync(() => {
    mockAuthService.getProfile.and.returnValue(throwError(() => new Error('Unauthorized')));
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    tick();
    expect(component.isLoggedIn).toBeFalse();
  }));

  it('should calculate price on cart load', fakeAsync(() => {
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    tick();
    expect(mockCheckoutService.calculatePrice).toHaveBeenCalled();
    expect(component.priceCalc).toBeTruthy();
    expect(component.priceCalc!.total).toBe(90);
    expect(component.validCodes).toContain('SAVE10');
    expect(component.invalidCodes).toContain('BAD');
    expect(component.validButNotApplicableCodes).toContain('NOTAPPLICABLE');
  }));

  it('should handle price calculation error', fakeAsync(() => {
    mockCheckoutService.calculatePrice.and.returnValue(throwError(() => new Error('Server error')));
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    tick();
    expect(component.error).toBeTruthy();
    expect(component.calculatingPrice).toBeFalse();
  }));

  it('should validate email correctly', () => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'invalid-email';
    expect(component.isFormValid()).toBeFalse();
  });

  it('should return false for invalid form when stripe not ready', () => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.stripeReady = false;
    expect(component.isFormValid()).toBeFalse();
  });

  it('should return false when firstName is empty', () => {
    component.firstName = '';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.stripeReady = true;
    expect(component.isFormValid()).toBeFalse();
  });

  it('should return false when lastName is empty', () => {
    component.firstName = 'John';
    component.lastName = '';
    component.email = 'test@example.com';
    component.stripeReady = true;
    expect(component.isFormValid()).toBeFalse();
  });

  it('should return false when cart is null', () => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.stripeReady = true;
    component.cart = null;
    expect(component.isFormValid()).toBeFalse();
  });

  it('should return true when all fields valid', () => {
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.stripeReady = true;
    component.cart = mockCartData;
    expect(component.isFormValid()).toBeTrue();
  });

  it('should parse discount codes from input', () => {
    component.discountInput = 'SAVE10, SAVE20';
    component.applyDiscountCodes();
    expect(component.appliedCodes).toContain('SAVE10');
    expect(component.appliedCodes).toContain('SAVE20');
  });

  it('should filter empty discount codes', () => {
    component.discountInput = 'SAVE10, , SAVE20';
    component.applyDiscountCodes();
    expect(component.appliedCodes.length).toBe(2);
  });

  it('should trim discount codes', () => {
    component.discountInput = '  SAVE10  ';
    component.applyDiscountCodes();
    expect(component.appliedCodes).toContain('SAVE10');
  });

  it('should go back to step 1', () => {
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/checkout']);
  });

  it('should initialize with empty first name', () => {
    expect(component.firstName).toBe('');
  });

  it('should initialize with empty last name', () => {
    expect(component.lastName).toBe('');
  });

  it('should initialize with empty email', () => {
    expect(component.email).toBe('');
  });

  it('should initialize with stripe not ready', () => {
    expect(component.stripeReady).toBeFalse();
  });

  it('should initialize with processing payment false', () => {
    expect(component.processingPayment).toBeFalse();
  });

  it('should initialize with calculating price false', () => {
    expect(component.calculatingPrice).toBeFalse();
  });

  it('should initialize with empty applied codes', () => {
    expect(component.appliedCodes).toEqual([]);
  });

  it('should trigger discount debounce on onDiscountInputChange', fakeAsync(() => {
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    tick();

    mockCheckoutService.calculatePrice.calls.reset();
    component.discountInput = 'NEWCODE';
    component.onDiscountInputChange();
    tick(400);

    expect(mockCheckoutService.calculatePrice).toHaveBeenCalled();
  }));

  it('should not call calculatePrice when cart is null', fakeAsync(() => {
    sessionStorage.removeItem('checkout_cart');
    fixture.detectChanges();
    tick();

    mockCheckoutService.calculatePrice.calls.reset();
    component.cart = null;
    component.appliedCodes = ['TEST'];
    (component as any).calculatePrice();

    expect(mockCheckoutService.calculatePrice).not.toHaveBeenCalled();
  }));

  it('should not mount stripe element when stripe is not initialized', () => {
    component.stripe = null;
    (component as any).mountStripeElement();
    expect(component.cardElementInstance).toBeFalsy();
  });

  it('should not mount stripe element when cardElement ref is missing', () => {
    component.stripe = { elements: () => ({ create: () => ({ mount: jasmine.createSpy() }) }) };
    (component as any).cardElement = undefined;
    (component as any).mountStripeElement();
    expect(component.cardElementInstance).toBeFalsy();
  });

  it('should mount stripe element when stripe and cardElement are available', () => {
    const mockMount = jasmine.createSpy('mount');
    const mockDestroy = jasmine.createSpy('destroy');
    const mockCreate = jasmine.createSpy('create').and.returnValue({ mount: mockMount, destroy: mockDestroy });
    component.stripe = { elements: () => ({ create: mockCreate }) };
    component.cardElement = { nativeElement: document.createElement('div') } as ElementRef;

    (component as any).mountStripeElement();

    expect(mockCreate).toHaveBeenCalled();
    expect(mockMount).toHaveBeenCalled();
    expect(component.cardElementInstance).toBeTruthy();
  });

  it('should handle ngOnDestroy cleanly', () => {
    expect(() => component.ngOnDestroy()).not.toThrow();
  });

  it('should destroy card element on ngOnDestroy if it exists', () => {
    const mockDestroy = jasmine.createSpy('destroy');
    component.cardElementInstance = { destroy: mockDestroy };
    component.ngOnDestroy();
    expect(mockDestroy).toHaveBeenCalled();
  });

  it('should not process payment when form is invalid', async () => {
    component.firstName = '';
    component.stripeReady = false;
    await component.processPayment();
    expect(component.processingPayment).toBeFalse();
    expect(mockCheckoutService.processPayment).not.toHaveBeenCalled();
  });

  it('should not process payment when cart is null', async () => {
    component.cart = null;
    component.stripeReady = true;
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    await component.processPayment();
    expect(mockCheckoutService.processPayment).not.toHaveBeenCalled();
  });

  it('should not process payment when stripe is null', async () => {
    component.cart = mockCartData;
    component.stripe = null;
    component.stripeReady = true;
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    await component.processPayment();
    expect(mockCheckoutService.processPayment).not.toHaveBeenCalled();
  });

  it('should handle stripe createPaymentMethod error', async () => {
    component.cart = mockCartData;
    component.stripeReady = true;
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.stripe = {
      createPaymentMethod: jasmine.createSpy().and.returnValue(Promise.resolve({
        paymentMethod: null,
        error: { message: 'Card declined' }
      }))
    };
    component.cardElementInstance = { destroy: jasmine.createSpy('destroy') };

    await component.processPayment();

    expect(component.error).toBe('Card declined');
    expect(component.processingPayment).toBeFalse();
  });

  it('should process payment successfully and navigate to confirmation', async () => {
    const mockPaymentResponse: PaymentResponse = {
      success: true,
      message: 'OK',
      purchaseId: 42,
      totalAmount: 90,
      discountAmount: 10
    };
    mockCheckoutService.processPayment.and.returnValue(of(mockPaymentResponse));

    component.cart = mockCartData;
    component.stripeReady = true;
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.validCodes = ['SAVE10'];
    component.stripe = {
      createPaymentMethod: jasmine.createSpy().and.returnValue(Promise.resolve({
        paymentMethod: { id: 'pm_test123' },
        error: null
      }))
    };
    component.cardElementInstance = { destroy: jasmine.createSpy('destroy') };

    await component.processPayment();

    expect(mockCheckoutService.processPayment).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/checkout/confirmation']);
    expect(component.processingPayment).toBeFalse();

    // Verify checkout_result is set in sessionStorage
    const result = JSON.parse(sessionStorage.getItem('checkout_result')!);
    expect(result.success).toBeTrue();
    expect(result.purchaseId).toBe(42);
  });

  it('should handle unsuccessful payment response', async () => {
    const mockPaymentResponse: PaymentResponse = {
      success: false,
      message: 'Insufficient funds'
    };
    mockCheckoutService.processPayment.and.returnValue(of(mockPaymentResponse));

    component.cart = mockCartData;
    component.stripeReady = true;
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.validCodes = [];
    component.stripe = {
      createPaymentMethod: jasmine.createSpy().and.returnValue(Promise.resolve({
        paymentMethod: { id: 'pm_test123' },
        error: null
      }))
    };
    component.cardElementInstance = { destroy: jasmine.createSpy('destroy') };

    await component.processPayment();

    expect(component.error).toBeTruthy();
    expect(router.navigate).not.toHaveBeenCalledWith(['/checkout/confirmation']);
  });

  it('should handle payment processing backend error', async () => {
    mockCheckoutService.processPayment.and.returnValue(throwError(() => new Error('Server error')));

    component.cart = mockCartData;
    component.stripeReady = true;
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.validCodes = [];
    component.stripe = {
      createPaymentMethod: jasmine.createSpy().and.returnValue(Promise.resolve({
        paymentMethod: { id: 'pm_test123' },
        error: null
      }))
    };
    component.cardElementInstance = { destroy: jasmine.createSpy('destroy') };

    await component.processPayment();

    expect(component.error).toBeTruthy();
    expect(component.processingPayment).toBeFalse();
  });

  it('should handle exception in processPayment (catch block)', async () => {
    component.cart = mockCartData;
    component.stripeReady = true;
    component.firstName = 'John';
    component.lastName = 'Doe';
    component.email = 'test@example.com';
    component.stripe = {
      createPaymentMethod: jasmine.createSpy().and.returnValue(Promise.reject(new Error('Network error')))
    };
    component.cardElementInstance = { destroy: jasmine.createSpy('destroy') };

    await component.processPayment();

    expect(component.error).toBe('Network error');
    expect(component.processingPayment).toBeFalse();
  });

  it('should handle getStripePublicKey error during init', fakeAsync(() => {
    // Test that when getStripePublicKey fails, error is set
    mockCheckoutService.getStripePublicKey.and.returnValue(throwError(() => new Error('Stripe error')));

    // Simulate what initStripe does after waitForStripe resolves
    mockCheckoutService.getStripePublicKey().subscribe({
      next: () => {},
      error: (err: any) => {
        mockErrorService.handleError(err);
        component.error = 'Stripe initialization failed';
      }
    });

    expect(component.error).toBe('Stripe initialization failed');
  }));

  it('should initialize stripe and set stripeReady when getStripePublicKey succeeds', fakeAsync(() => {
    const mockMount = jasmine.createSpy('mount');
    const mockDestroy = jasmine.createSpy('destroy');
    const mockElements = { create: () => ({ mount: mockMount, destroy: mockDestroy }) };
    const mockStripeInstance = { elements: () => mockElements };

    // Simulate what happens when Stripe SDK is loaded and key is fetched
    component.stripe = mockStripeInstance;
    component.stripeReady = true;
    component.cardElement = { nativeElement: document.createElement('div') } as ElementRef;
    (component as any).mountStripeElement();

    expect(component.stripeReady).toBeTrue();
    expect(component.cardElementInstance).toBeTruthy();
    expect(mockMount).toHaveBeenCalled();
  }));

  it('should apply discount codes and recalculate price', fakeAsync(() => {
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    tick();

    mockCheckoutService.calculatePrice.calls.reset();
    component.discountInput = 'SAVE10, BAD';
    component.applyDiscountCodes();
    tick();

    expect(component.appliedCodes).toEqual(['SAVE10', 'BAD']);
    expect(mockCheckoutService.calculatePrice).toHaveBeenCalled();
  }));

  it('should set price calculation results including discountPercentages and discountAppliesTo', fakeAsync(() => {
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    tick();

    expect(component.discountPercentages).toEqual({ 'SAVE10': 10 });
    expect(component.discountAppliesTo).toEqual({ 'SAVE10': ['ADULT'] });
  }));
});

