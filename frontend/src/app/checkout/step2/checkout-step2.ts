import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { CheckoutService, CartItem, PriceCalculationResponse, PaymentRequest } from '../services/checkout.service';
import { AuthService, UserProfile } from '../../auth/auth.service';
import { ErrorService } from '../../error/error-service';
import { CurrencyService } from '../../shared/currency.service';

declare const Stripe: any;

interface CheckoutCart {
  items: CartItem[];
  visitDate: string;
}

@Component({
  selector: 'app-checkout-step2',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './checkout-step2.html',
  styleUrl: './checkout-step2.css'
})
export class CheckoutStep2Component implements OnInit, OnDestroy, AfterViewInit {
  @ViewChild('cardElement') cardElement!: ElementRef;

  cart: CheckoutCart | null = null;
  priceCalc: PriceCalculationResponse | null = null;

  firstName = '';
  lastName = '';
  email = '';

  discountInput = '';
  appliedCodes: string[] = [];
  validCodes: string[] = [];
  invalidCodes: string[] = [];
  validButNotApplicableCodes: string[] = [];
  discountPercentages: { [key: string]: number } = {};
  discountAppliesTo: { [key: string]: string[] } = {};

  calculatingPrice = false;
  processingPayment = false;
  error: string | null = null;
  isLoggedIn = false;

  stripe: any;
  cardElementInstance: any;
  stripeReady = false;

  private destroy$ = new Subject<void>();
  private discountInput$ = new Subject<string>();

  constructor(
    private checkoutService: CheckoutService,
    private authService: AuthService,
    private router: Router,
    private translate: TranslateService,
    private errorService: ErrorService,
    public currency: CurrencyService
  ) {}

  ngOnInit(): void {
    this.loadCart();
    this.loadUserData();
    this.initStripe();
    this.setupDiscountDebounce();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.mountStripeElement(), 500);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.cardElementInstance) {
      this.cardElementInstance.destroy();
    }
  }

  private loadCart(): void {
    const cartData = sessionStorage.getItem('checkout_cart');
    if (!cartData) {
      this.router.navigate(['/checkout']);
      return;
    }

    this.cart = JSON.parse(cartData);
    this.calculatePrice();
  }

  private loadUserData(): void {
    this.authService.getProfile()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (profile: UserProfile) => {
          if (profile) {
            this.isLoggedIn = true;
            this.firstName = profile.firstname || '';
            this.lastName = profile.lastname || '';
            this.email = profile.email || '';
          }
        },
        error: () => {
          this.isLoggedIn = false;
        }
      });
  }

  private initStripe(): void {
    this.waitForStripe().then(() => {
      this.checkoutService.getStripePublicKey()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            this.stripe = Stripe(response.publicKey);
            this.stripeReady = true;
            this.mountStripeElement();
          },
          error: (err) => {
            const { code, args } = this.errorService.handleError(err);
            this.error = this.translate.instant(code, args);
          }
        });
    });
  }

  private waitForStripe(): Promise<void> {
    return new Promise((resolve) => {
      if (typeof Stripe !== 'undefined') {
        resolve();
        return;
      }
      const checkInterval = setInterval(() => {
        if (typeof Stripe !== 'undefined') {
          clearInterval(checkInterval);
          resolve();
        }
      }, 100);
    });
  }

  private mountStripeElement(): void {
    if (!this.stripe || !this.cardElement?.nativeElement) return;

    const elements = this.stripe.elements();
    this.cardElementInstance = elements.create('card', {
      style: {
        base: {
          fontSize: '16px',
          color: '#2c0d0d',
          fontFamily: '"Segoe UI", Arial, sans-serif',
          '::placeholder': { color: '#888' }
        },
        invalid: { color: '#dc3545' }
      }
    });
    this.cardElementInstance.mount(this.cardElement.nativeElement);
  }

  private setupDiscountDebounce(): void {
    this.discountInput$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.applyDiscountCodes());
  }

  onDiscountInputChange(): void {
    this.discountInput$.next(this.discountInput);
  }

  applyDiscountCodes(): void {
    this.appliedCodes = this.discountInput
      .split(',')
      .map(c => c.trim())
      .filter(c => c.length > 0);

    this.calculatePrice();
  }

  private calculatePrice(): void {
    if (!this.cart) return;

    this.calculatingPrice = true;
    const request = {
      items: this.cart.items.map(item => ({
        ticketTypeName: item.ticketTypeName,
        quantity: item.quantity
      })),
      discountCodes: this.appliedCodes
    };

    this.checkoutService.calculatePrice(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.priceCalc = response;
          this.validCodes = response.validDiscountCodes || [];
          this.invalidCodes = response.invalidDiscountCodes || [];
          this.validButNotApplicableCodes = response.validButNotApplicableCodes || [];
          this.discountPercentages = response.discountPercentages || {};
          this.discountAppliesTo = response.discountAppliesTo || {};
          this.calculatingPrice = false;
        },
        error: (err) => {
          const { code, args } = this.errorService.handleError(err);
          this.error = this.translate.instant(code, args);
          this.calculatingPrice = false;
        }
      });
  }

  isFormValid(): boolean {
    return (
      this.firstName.trim().length > 0 &&
      this.lastName.trim().length > 0 &&
      this.isValidEmail(this.email) &&
      this.stripeReady &&
      this.cart !== null
    );
  }


  private isValidEmail(emailValue: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(emailValue);
  }

  async processPayment(): Promise<void> {
    if (!this.isFormValid() || !this.cart || !this.stripe) return;

    this.processingPayment = true;
    this.error = null;

    try {
      const { paymentMethod, error } = await this.stripe.createPaymentMethod({
        type: 'card',
        card: this.cardElementInstance,
        billing_details: {
          name: `${this.firstName} ${this.lastName}`,
          email: this.email
        }
      });

      if (error) {
        this.error = error.message;
        this.processingPayment = false;
        return;
      }

      const paymentRequest: PaymentRequest = {
        visitDate: this.cart.visitDate,
        items: this.cart.items.map(item => ({
          ticketTypeName: item.ticketTypeName,
          quantity: item.quantity
        })),
        discountCodes: this.validCodes,
        email: this.email,
        firstName: this.firstName,
        lastName: this.lastName,
        stripePaymentMethodId: paymentMethod.id
      };

      this.checkoutService.processPayment(paymentRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            this.processingPayment = false;
            if (response.success) {
              sessionStorage.setItem('checkout_result', JSON.stringify({
                success: true,
                purchaseId: response.purchaseId,
                totalAmount: response.totalAmount,
                discountAmount: response.discountAmount,
                visitDate: this.cart!.visitDate
              }));
              sessionStorage.removeItem('checkout_cart');
              this.router.navigate(['/checkout/confirmation']);
            } else {
              this.error = this.translate.instant(response.message);
            }
          },
          error: (err) => {
            const { code, args } = this.errorService.handleError(err);
            this.error = this.translate.instant(code, args);
            this.processingPayment = false;
          }
        });
    } catch (e: any) {
      this.error = e.message || this.translate.instant('error.unexpected');
      this.processingPayment = false;
    }
  }

  goBack(): void {
    this.router.navigate(['/checkout']);
  }
}

