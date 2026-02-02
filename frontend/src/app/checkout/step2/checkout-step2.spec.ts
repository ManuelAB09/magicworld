import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CheckoutStep2Component } from './checkout-step2';
import { Router } from '@angular/router';

describe('CheckoutStep2Component', () => {
  let component: CheckoutStep2Component;
  let fixture: ComponentFixture<CheckoutStep2Component>;
  let router: Router;

  const mockCartData = {
    items: [{ ticketTypeName: 'ADULT', quantity: 2, unitPrice: 50, totalPrice: 100, photoUrl: '/img.jpg', description: 'Adult' }],
    visitDate: '2026-01-15'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CheckoutStep2Component,
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(CheckoutStep2Component);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    sessionStorage.removeItem('checkout_cart');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to checkout if no cart data', () => {
    sessionStorage.removeItem('checkout_cart');
    fixture.detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['/checkout']);
  });

  it('should load cart from session storage', () => {
    sessionStorage.setItem('checkout_cart', JSON.stringify(mockCartData));
    fixture.detectChanges();
    expect(component.cart).toBeTruthy();
  });

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
});

