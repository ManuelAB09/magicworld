import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TicketTypeApiService, TicketType, TicketTypeData } from './ticket-type.service';
import { AuthService } from '../auth/auth.service';
import { of } from 'rxjs';
import { HttpHeaders } from '@angular/common/http';

describe('TicketTypeApiService', () => {
  let service: TicketTypeApiService;
  let httpMock: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const mockTicketType: TicketType = {
    id: 1,
    typeName: 'Adult',
    description: 'Adult ticket',
    cost: 50,
    maxPerDay: 100,
    photoUrl: '/images/adult.jpg'
  };

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['ensureCsrfToken']);
    mockAuthService.ensureCsrfToken.and.returnValue(of(new HttpHeaders()));

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        TicketTypeApiService,
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    service = TestBed.inject(TicketTypeApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all ticket types', () => {
    service.findAll().subscribe(ticketTypes => {
      expect(ticketTypes.length).toBe(1);
      expect(ticketTypes[0].typeName).toBe('Adult');
    });

    const req = httpMock.expectOne(r => r.url.includes('/ticket-types'));
    expect(req.request.method).toBe('GET');
    req.flush([mockTicketType]);
  });

  it('should fetch ticket type by id', () => {
    service.findById(1).subscribe(ticketType => {
      expect(ticketType.typeName).toBe('Adult');
    });

    const req = httpMock.expectOne(r => r.url.includes('/ticket-types/1'));
    expect(req.request.method).toBe('GET');
    req.flush(mockTicketType);
  });

  it('should create ticket type', () => {
    service.create(mockTicketType).subscribe(ticketType => {
      expect(ticketType.typeName).toBe('Adult');
    });

    const req = httpMock.expectOne(r => r.url.includes('/ticket-types'));
    expect(req.request.method).toBe('POST');
    req.flush(mockTicketType);
  });

  it('should update ticket type', () => {
    const updated = { ...mockTicketType, cost: 60 };
    service.update(1, updated).subscribe(ticketType => {
      expect(ticketType).toBeTruthy();
    });

    const req = httpMock.expectOne(r => r.url.includes('/ticket-types/1'));
    expect(req.request.method).toBe('PUT');
    req.flush(mockTicketType);
  });

  it('should delete ticket type', () => {
    service.delete(1).subscribe();

    const req = httpMock.expectOne(r => r.url.includes('/ticket-types/1'));
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('should create ticket type with multipart', () => {
    const data: TicketTypeData = {
      typeName: 'New',
      description: 'New ticket',
      cost: 30,
      maxPerDay: 50
    };
    const file = new File([''], 'test.jpg', { type: 'image/jpeg' });

    service.createMultipart(data, file).subscribe(ticketType => {
      expect(ticketType.typeName).toBe('Adult');
    });

    const req = httpMock.expectOne(r => r.url.includes('/ticket-types'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush(mockTicketType);
  });

  it('should update ticket type with multipart', () => {
    const data: TicketTypeData = {
      typeName: 'Updated',
      description: 'Updated ticket',
      cost: 55,
      maxPerDay: 120
    };

    service.updateMultipart(1, data).subscribe(ticketType => {
      expect(ticketType.typeName).toBe('Adult');
    });

    const req = httpMock.expectOne(r => r.url.includes('/ticket-types/1'));
    expect(req.request.method).toBe('PUT');
    req.flush(mockTicketType);
  });
});

