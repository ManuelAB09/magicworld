import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { CheckoutStep1Component } from './checkout-step1';

describe('CheckoutStep1Component', () => {
  let component: CheckoutStep1Component;
  let fixture: ComponentFixture<CheckoutStep1Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CheckoutStep1Component,
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CheckoutStep1Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with tomorrow as min date', () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const expectedDate = tomorrow.toISOString().split('T')[0];
    expect(component.minDate).toBe(expectedDate);
  });

  it('should not allow proceeding with empty cart', () => {
    expect(component.canProceed()).toBeFalse();
  });

  it('should return empty cartItems when cart is empty', () => {
    expect(component.cartItems).toEqual([]);
  });
});

