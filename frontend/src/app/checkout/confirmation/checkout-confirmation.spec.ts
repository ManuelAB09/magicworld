import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { CheckoutConfirmationComponent } from './checkout-confirmation';

describe('CheckoutConfirmationComponent', () => {
  let component: CheckoutConfirmationComponent;
  let fixture: ComponentFixture<CheckoutConfirmationComponent>;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CheckoutConfirmationComponent,
        TranslateModule.forRoot()
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate');

    fixture = TestBed.createComponent(CheckoutConfirmationComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should redirect to home if no result data', () => {
    sessionStorage.removeItem('checkout_result');
    fixture.detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should show modal when result data exists', () => {
    sessionStorage.setItem('checkout_result', JSON.stringify({
      success: true,
      purchaseId: 123,
      totalAmount: 50.00,
      visitDate: '2026-01-15'
    }));
    fixture.detectChanges();
    expect(component.showModal).toBeTrue();
    expect(component.result?.success).toBeTrue();
  });

  it('should navigate to home on goHome()', () => {
    component.goHome();
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should navigate to checkout on goBack()', () => {
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/checkout']);
  });
});

