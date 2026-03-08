import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { CheckoutService, TicketAvailability, PriceCalculationResponse, PaymentResponse } from './checkout.service';

describe('CheckoutService', () => {
  let service: CheckoutService;
  let httpMock: HttpTestingController;

  const mockAvailability: TicketAvailability[] = [
    { id: 1, typeName: 'ADULT', description: 'Adult ticket', cost: 50, adjustedCost: 50, seasonalMultiplier: 1, photoUrl: '/img.jpg', maxPerDay: 100, available: 80 }
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
      discountCodes: ['SAVE10'],
      visitDate: '2025-01-15'
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

  it('should get closure days with from and to params', () => {
    const mockClosures = [
      { id: 1, closureDate: '2025-06-01', reason: 'Maintenance' }
    ];

    service.getClosureDays('2025-06-01', '2025-06-30').subscribe(response => {
      expect(response.length).toBe(1);
      expect(response[0].reason).toBe('Maintenance');
    });

    const req = httpMock.expectOne(r =>
      r.url.includes('/park-closures') &&
      r.params.get('from') === '2025-06-01' &&
      r.params.get('to') === '2025-06-30'
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockClosures);
  });

  it('should get all closure days', () => {
    const mockClosures = [
      { id: 1, closureDate: '2025-06-01', reason: 'Maintenance' },
      { id: 2, closureDate: '2025-07-04', reason: 'Holiday' }
    ];

    service.getAllClosureDays().subscribe(response => {
      expect(response.length).toBe(2);
    });

    const req = httpMock.expectOne(r =>
      r.url.includes('/park-closures') && !r.params.has('from')
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockClosures);
  });

  it('should create closure day', () => {
    const newClosure = { closureDate: '2025-08-15', reason: 'Special event' };
    const mockResponse = { id: 3, ...newClosure };

    service.createClosureDay(newClosure).subscribe(response => {
      expect(response.id).toBe(3);
      expect(response.reason).toBe('Special event');
    });

    const req = httpMock.expectOne(r => r.url.includes('/park-closures'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newClosure);
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockResponse);
  });

  it('should delete closure day', () => {
    service.deleteClosureDay(3).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/park-closures/3'));
    expect(req.request.method).toBe('DELETE');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(null);
  });

  it('should get all seasonal pricing', () => {
    const mockPricing = [
      { id: 1, name: 'Summer', startDate: '2025-06-01', endDate: '2025-08-31', multiplier: 1.5, applyOnWeekdays: true, applyOnWeekends: true }
    ];

    service.getAllSeasonalPricing().subscribe(response => {
      expect(response.length).toBe(1);
      expect(response[0].name).toBe('Summer');
    });

    const req = httpMock.expectOne(r => r.url.includes('/seasonal-pricing'));
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockPricing);
  });

  it('should create seasonal pricing', () => {
    const newPricing = { name: 'Winter', startDate: '2025-12-01', endDate: '2026-02-28', multiplier: 1.2, applyOnWeekdays: true, applyOnWeekends: false };
    const mockResponse = { id: 2, ...newPricing };

    service.createSeasonalPricing(newPricing).subscribe(response => {
      expect(response.id).toBe(2);
      expect(response.name).toBe('Winter');
    });

    const req = httpMock.expectOne(r => r.url.includes('/seasonal-pricing'));
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockResponse);
  });

  it('should update seasonal pricing', () => {
    const updateData = { name: 'Summer Updated', multiplier: 1.8 };
    const mockResponse = { id: 1, name: 'Summer Updated', startDate: '2025-06-01', endDate: '2025-08-31', multiplier: 1.8, applyOnWeekdays: true, applyOnWeekends: true };

    service.updateSeasonalPricing(1, updateData).subscribe(response => {
      expect(response.multiplier).toBe(1.8);
    });

    const req = httpMock.expectOne(r => r.url.includes('/seasonal-pricing/1'));
    expect(req.request.method).toBe('PUT');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockResponse);
  });

  it('should delete seasonal pricing', () => {
    service.deleteSeasonalPricing(1).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/seasonal-pricing/1'));
    expect(req.request.method).toBe('DELETE');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(null);
  });
});

