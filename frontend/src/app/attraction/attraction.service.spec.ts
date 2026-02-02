import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AttractionApiService, Attraction, AttractionData } from './attraction.service';
import { AuthService } from '../auth/auth.service';
import { of } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';

describe('AttractionApiService', () => {
  let service: AttractionApiService;
  let httpMock: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockAttraction: Attraction = {
    id: 1,
    name: 'Test',
    intensity: 'LOW',
    category: 'OTHER',
    minimumHeight: 100,
    minimumAge: 10,
    minimumWeight: 30,
    description: 'Test desc',
    photoUrl: '/images/test.jpg',
    isActive: true,
    mapPositionX: 50,
    mapPositionY: 50
  };

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['ensureCsrfToken']);
    mockAuthService.ensureCsrfToken.and.returnValue(of(new HttpHeaders()));

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AttractionApiService,
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    service = TestBed.inject(AttractionApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all attractions', () => {
    service.findAll().subscribe(attractions => {
      expect(attractions.length).toBe(1);
      expect(attractions[0].name).toBe('Test');
    });

    const req = httpMock.expectOne(req => req.url.includes('/attractions'));
    expect(req.request.method).toBe('GET');
    req.flush([mockAttraction]);
  });

  it('should fetch all attractions with filters', () => {
    service.findAll({ minHeight: 100, minWeight: 50, minAge: 10 }).subscribe(attractions => {
      expect(attractions.length).toBe(1);
    });

    const req = httpMock.expectOne(req =>
      req.url.includes('/attractions') &&
      req.params.get('minHeight') === '100' &&
      req.params.get('minWeight') === '50' &&
      req.params.get('minAge') === '10'
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockAttraction]);
  });

  it('should fetch attraction by id', () => {
    service.findById(1).subscribe(attraction => {
      expect(attraction.name).toBe('Test');
    });

    const req = httpMock.expectOne(req => req.url.includes('/attractions/1'));
    expect(req.request.method).toBe('GET');
    req.flush(mockAttraction);
  });

  it('should delete attraction', () => {
    service.delete(1).subscribe();

    const req = httpMock.expectOne(req => req.url.includes('/attractions/1'));
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should create attraction with multipart', () => {
    const data: AttractionData = {
      name: 'New',
      intensity: 'LOW',
      category: 'OTHER',
      minimumHeight: 100,
      minimumAge: 10,
      minimumWeight: 30,
      description: 'Desc',
      isActive: true,
      mapPositionX: 50,
      mapPositionY: 50
    };
    const file = new File([''], 'test.jpg', { type: 'image/jpeg' });

    service.createMultipart(data, file).subscribe(attraction => {
      expect(attraction.name).toBe('Test');
    });

    const req = httpMock.expectOne(req => req.url.includes('/attractions'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush(mockAttraction);
  });

  it('should update attraction with multipart', () => {
    const data: AttractionData = {
      name: 'Updated',
      intensity: 'MEDIUM',
      category: 'ROLLER_COASTER',
      minimumHeight: 120,
      minimumAge: 12,
      minimumWeight: 40,
      description: 'Updated desc',
      isActive: true,
      mapPositionX: 60,
      mapPositionY: 70
    };

    service.updateMultipart(1, data).subscribe(attraction => {
      expect(attraction.name).toBe('Test');
    });

    const req = httpMock.expectOne(req => req.url.includes('/attractions/1'));
    expect(req.request.method).toBe('PUT');
    req.flush(mockAttraction);
  });
});

