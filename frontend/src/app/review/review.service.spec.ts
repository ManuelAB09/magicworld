import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ReviewService, ReviewDTO, ReviewPage, ReviewRequest } from './review.service';
import { AuthService } from '../auth/auth.service';
import { of } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';

describe('ReviewService', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockReviewPage: ReviewPage = {
    content: [
      { id: 1, stars: 4.5, publicationDate: '2026-01-15', visitDate: '2026-01-10', description: 'Great!', username: 'user1', purchaseId: 1 }
    ],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 10
  };

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['ensureCsrfToken']);
    mockAuthService.ensureCsrfToken.and.returnValue(of(new HttpHeaders()));

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ReviewService,
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    service = TestBed.inject(ReviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch paginated reviews', () => {
    service.findAllPaginated(0, 10).subscribe(page => {
      expect(page.content.length).toBe(1);
      expect(page.totalElements).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url.includes('/api/v1/reviews') && r.params.has('page'));
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    req.flush(mockReviewPage);
  });

  it('should fetch available purchases', () => {
    const mockPurchaseIds = [1, 2, 3];

    service.getAvailablePurchases().subscribe(ids => {
      expect(ids.length).toBe(3);
      expect(ids).toEqual([1, 2, 3]);
    });

    const req = httpMock.expectOne(r => r.url.includes('/available-purchases'));
    expect(req.request.method).toBe('GET');
    req.flush(mockPurchaseIds);
  });

  it('should create a review', () => {
    const request: ReviewRequest = {
      purchaseId: 1,
      visitDate: '2026-01-10',
      stars: 5,
      description: 'Excellent!'
    };
    const mockResponse: ReviewDTO = {
      id: 1,
      stars: 5,
      publicationDate: '2026-02-01',
      visitDate: '2026-01-10',
      description: 'Excellent!',
      username: 'user1',
      purchaseId: 1
    };

    service.create(request).subscribe(review => {
      expect(review.id).toBe(1);
      expect(review.stars).toBe(5);
    });

    const req = httpMock.expectOne(r => r.url.includes('/api/v1/reviews') && r.method === 'POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it('should call ensureCsrfToken before create', () => {
    const request: ReviewRequest = {
      purchaseId: 1,
      visitDate: '2026-01-10',
      stars: 5,
      description: 'Test'
    };

    service.create(request).subscribe();

    expect(mockAuthService.ensureCsrfToken).toHaveBeenCalled();

    const req = httpMock.expectOne(r => r.method === 'POST');
    req.flush({});
  });

  it('should call ensureCsrfToken before getAvailablePurchases', () => {
    service.getAvailablePurchases().subscribe();

    expect(mockAuthService.ensureCsrfToken).toHaveBeenCalled();

    const req = httpMock.expectOne(r => r.url.includes('/available-purchases'));
    req.flush([]);
  });

  it('should handle pagination parameters correctly', () => {
    service.findAllPaginated(2, 20).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/api/v1/reviews'));
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('20');
    req.flush(mockReviewPage);
  });
});
