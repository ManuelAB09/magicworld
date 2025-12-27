import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DiscountApiService, Discount, DiscountRequest } from './discount.service';
import { AuthService } from '../auth/auth.service';
import { of } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';

describe('DiscountApiService', () => {
  let service: DiscountApiService;
  let httpMock: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockDiscount: Discount = {
    id: 1,
    discountCode: 'TEST',
    discountPercentage: 20,
    expiryDate: '2025-12-31'
  };

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['ensureCsrfToken']);
    mockAuthService.ensureCsrfToken.and.returnValue(of(new HttpHeaders()));

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        DiscountApiService,
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    service = TestBed.inject(DiscountApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all discounts', () => {
    service.findAll().subscribe(discounts => {
      expect(discounts.length).toBe(1);
      expect(discounts[0].discountCode).toBe('TEST');
    });

    const req = httpMock.expectOne(r => r.url.includes('/discounts'));
    expect(req.request.method).toBe('GET');
    req.flush([mockDiscount]);
  });

  it('should fetch discount by id', () => {
    service.findById(1).subscribe(discount => {
      expect(discount.discountCode).toBe('TEST');
    });

    const req = httpMock.expectOne(r => r.url.includes('/discounts/1'));
    expect(req.request.method).toBe('GET');
    req.flush(mockDiscount);
  });

  it('should fetch ticket types by discount', () => {
    service.getTicketTypesByDiscount(1).subscribe(types => {
      expect(types.length).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url.includes('/discounts/1/ticket-types'));
    expect(req.request.method).toBe('GET');
    req.flush([{ typeName: 'Adult' }]);
  });

  it('should create discount', () => {
    const request: DiscountRequest = {
      discount: mockDiscount,
      applicableTicketTypesNames: ['Adult']
    };

    service.create(request).subscribe(discount => {
      expect(discount.discountCode).toBe('TEST');
    });

    const req = httpMock.expectOne(r => r.url.includes('/discounts'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockDiscount);
  });

  it('should update discount', () => {
    const request: DiscountRequest = {
      discount: { ...mockDiscount, discountPercentage: 25 },
      applicableTicketTypesNames: ['Adult', 'Child']
    };

    service.update(1, request).subscribe(discount => {
      expect(discount).toBeTruthy();
    });

    const req = httpMock.expectOne(r => r.url.includes('/discounts/1'));
    expect(req.request.method).toBe('PUT');
    req.flush(mockDiscount);
  });

  it('should delete discount', () => {
    service.delete(1).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/discounts/1'));
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});

