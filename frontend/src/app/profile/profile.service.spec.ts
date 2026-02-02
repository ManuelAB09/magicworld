import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProfileService, UpdateProfileRequest, PurchaseDTO } from './profile.service';
import { AuthService, UserProfile, Role } from '../auth/auth.service';
import { of } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';

describe('ProfileService', () => {
  let service: ProfileService;
  let httpMock: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockProfile: UserProfile = {
    username: 'testuser',
    firstname: 'Test',
    lastname: 'User',
    email: 'test@example.com',
    role: Role.USER
  };

  const mockPurchases: PurchaseDTO[] = [
    {
      id: 1,
      purchaseDate: '2026-01-01',
      lines: [
        { id: 1, validDate: '2026-01-10', quantity: 2, totalCost: 59.80, ticketTypeName: 'Adult' }
      ]
    }
  ];

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['ensureCsrfToken']);
    mockAuthService.ensureCsrfToken.and.returnValue(of(new HttpHeaders()));

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ProfileService,
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    service = TestBed.inject(ProfileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should update profile', () => {
    const request: UpdateProfileRequest = {
      firstname: 'Updated',
      lastname: 'Name',
      email: 'updated@example.com'
    };

    service.updateProfile(request).subscribe(profile => {
      expect(profile.firstname).toBe('Updated');
    });

    const req = httpMock.expectOne(r => r.url.includes('/api/v1/users/profile') && r.method === 'PUT');
    expect(req.request.body).toEqual(request);
    req.flush({ ...mockProfile, firstname: 'Updated' });
  });

  it('should update profile with password', () => {
    const request: UpdateProfileRequest = {
      firstname: 'Test',
      lastname: 'User',
      email: 'test@example.com',
      password: 'NewPassword123!'
    };

    service.updateProfile(request).subscribe();

    const req = httpMock.expectOne(r => r.method === 'PUT');
    expect(req.request.body.password).toBe('NewPassword123!');
    req.flush(mockProfile);
  });

  it('should delete profile', () => {
    service.deleteProfile().subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/api/v1/users/profile') && r.method === 'DELETE');
    req.flush(null);
  });

  it('should get my purchases', () => {
    service.getMyPurchases().subscribe(purchases => {
      expect(purchases.length).toBe(1);
      expect(purchases[0].id).toBe(1);
    });

    const req = httpMock.expectOne(r => r.url.includes('/api/v1/purchases/my-purchases'));
    expect(req.request.method).toBe('GET');
    req.flush(mockPurchases);
  });

  it('should call ensureCsrfToken before updateProfile', () => {
    const request: UpdateProfileRequest = {
      firstname: 'Test',
      lastname: 'User',
      email: 'test@example.com'
    };

    service.updateProfile(request).subscribe();

    expect(mockAuthService.ensureCsrfToken).toHaveBeenCalled();

    const req = httpMock.expectOne(r => r.method === 'PUT');
    req.flush(mockProfile);
  });

  it('should call ensureCsrfToken before deleteProfile', () => {
    service.deleteProfile().subscribe();

    expect(mockAuthService.ensureCsrfToken).toHaveBeenCalled();

    const req = httpMock.expectOne(r => r.method === 'DELETE');
    req.flush(null);
  });

  it('should call ensureCsrfToken before getMyPurchases', () => {
    service.getMyPurchases().subscribe();

    expect(mockAuthService.ensureCsrfToken).toHaveBeenCalled();

    const req = httpMock.expectOne(r => r.url.includes('/my-purchases'));
    req.flush([]);
  });

  it('should handle multiple purchase lines', () => {
    const purchasesWithMultipleLines: PurchaseDTO[] = [
      {
        id: 1,
        purchaseDate: '2026-01-01',
        lines: [
          { id: 1, validDate: '2026-01-10', quantity: 2, totalCost: 59.80, ticketTypeName: 'Adult' },
          { id: 2, validDate: '2026-01-10', quantity: 1, totalCost: 19.90, ticketTypeName: 'Child' }
        ]
      }
    ];

    service.getMyPurchases().subscribe(purchases => {
      expect(purchases[0].lines.length).toBe(2);
    });

    const req = httpMock.expectOne(r => r.url.includes('/my-purchases'));
    req.flush(purchasesWithMultipleLines);
  });
});
