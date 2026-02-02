import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { CheckoutService, TicketAvailability, PriceCalculationResponse, PaymentResponse } from './checkout.service';

describe('CheckoutService', () => {
  let service: CheckoutService;
  let httpMock: HttpTestingController;

  const mockAvailability: TicketAvailability[] = [
    { id: 1, typeName: 'ADULT', description: 'Adult ticket', cost: 50, photoUrl: '/img.jpg', maxPerDay: 100, available: 80 }
  ];

  const mockPriceResponse: PriceCalculationResponse = {
    subtotal: 100,
    discountAmount: 10,
    total: 90,
    validDiscountCodes: ['SAVE10'],
    invalidDiscountCodes: [],
    validButNotApplicableCodes: [],
    discountPercentages: { 'SAVE10': 10 },
    discountAppliesTo: { 'SAVE10': ['ADULT'] }
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CheckoutService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(CheckoutService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get stripe public key', () => {
    service.getStripePublicKey().subscribe(response => {
      expect(response.publicKey).toBe('pk_test_123');
    });

    const req = httpMock.expectOne(r => r.url.includes('/stripe-key'));
    expect(req.request.method).toBe('GET');
    req.flush({ publicKey: 'pk_test_123' });
  });

  it('should get availability for date', () => {
    service.getAvailability('2025-01-15').subscribe(response => {
      expect(response.length).toBe(1);
      expect(response[0].typeName).toBe('ADULT');
    });

    const req = httpMock.expectOne(r => r.url.includes('/availability') && r.params.get('date') === '2025-01-15');
    expect(req.request.method).toBe('GET');
    req.flush(mockAvailability);
  });

  it('should calculate price', () => {
    const request = {
      items: [{ ticketTypeName: 'ADULT', quantity: 2 }],
      discountCodes: ['SAVE10']
    };

    service.calculatePrice(request).subscribe(response => {
      expect(response.total).toBe(90);
      expect(response.discountAmount).toBe(10);
    });

    const req = httpMock.expectOne(r => r.url.includes('/calculate'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockPriceResponse);
  });

  it('should process payment', () => {
    const paymentRequest = {
      visitDate: '2025-01-15',
      items: [{ ticketTypeName: 'ADULT', quantity: 2 }],
      discountCodes: [],
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      stripePaymentMethodId: 'pm_123'
    };

    const mockPaymentResponse: PaymentResponse = {
      success: true,
      message: 'Payment successful',
      purchaseId: 1,
      totalAmount: 100
    };

    service.processPayment(paymentRequest).subscribe(response => {
      expect(response.success).toBeTrue();
      expect(response.purchaseId).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url.includes('/process'));
    expect(req.request.method).toBe('POST');
    req.flush(mockPaymentResponse);
  });

  it('should include withCredentials in requests', () => {
    service.getStripePublicKey().subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/stripe-key'));
    expect(req.request.withCredentials).toBeTrue();
    req.flush({ publicKey: 'pk_test' });
  });
});

