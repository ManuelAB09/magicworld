import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ParkStatusService } from './park-status.service';

describe('ParkStatusService', () => {
  let service: ParkStatusService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ParkStatusService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ParkStatusService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get simulator status', () => {
    const mockStatus = { running: true, simulatedVisitors: 50, activeQueues: 3, totalInQueues: 120 };

    service.getSimulatorStatus().subscribe(status => {
      expect(status.running).toBeTrue();
      expect(status.simulatedVisitors).toBe(50);
      expect(status.activeQueues).toBe(3);
      expect(status.totalInQueues).toBe(120);
    });

    const req = httpMock.expectOne(r => r.url.includes('/park-status/simulator'));
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockStatus);
  });

  it('should get attraction statuses', () => {
    const mockStatuses = [
      { attractionId: 1, name: 'Ride1', isOpen: true, queueSize: 30, estimatedWaitMinutes: 15, mapPositionX: 50, mapPositionY: 50, intensity: 'HIGH' },
      { attractionId: 2, name: 'Ride2', isOpen: false, queueSize: 0, estimatedWaitMinutes: 0, mapPositionX: 30, mapPositionY: 70, intensity: 'LOW' }
    ];

    service.getAttractionStatuses().subscribe(statuses => {
      expect(statuses.length).toBe(2);
      expect(statuses[0].name).toBe('Ride1');
      expect(statuses[1].isOpen).toBeFalse();
    });

    const req = httpMock.expectOne(r => r.url.includes('/park-status/attractions'));
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(mockStatuses);
  });

  it('should include withCredentials in simulator status request', () => {
    service.getSimulatorStatus().subscribe();
    const req = httpMock.expectOne(r => r.url.includes('/park-status/simulator'));
    expect(req.request.withCredentials).toBeTrue();
    req.flush({ running: false, simulatedVisitors: 0, activeQueues: 0, totalInQueues: 0 });
  });

  it('should include withCredentials in attraction statuses request', () => {
    service.getAttractionStatuses().subscribe();
    const req = httpMock.expectOne(r => r.url.includes('/park-status/attractions'));
    expect(req.request.withCredentials).toBeTrue();
    req.flush([]);
  });
});

