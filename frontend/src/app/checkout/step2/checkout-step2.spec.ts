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

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to checkout if no cart data', () => {
    sessionStorage.removeItem('checkout_cart');
    fixture.detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['/checkout']);
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
});

