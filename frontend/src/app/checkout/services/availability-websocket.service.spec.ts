import { TestBed } from '@angular/core/testing';
import { AvailabilityWebSocketService } from './availability-websocket.service';

describe('AvailabilityWebSocketService', () => {
  let service: AvailabilityWebSocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AvailabilityWebSocketService]
    });
    service = TestBed.inject(AvailabilityWebSocketService);
  });

  afterEach(() => {
    service.disconnect();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should disconnect without errors when not connected', () => {
    expect(() => service.disconnect()).not.toThrow();
  });

  it('should return observable from connect', () => {
    const result = service.connect('2026-01-15');
    expect(result).toBeTruthy();
    expect(result.subscribe).toBeDefined();
  });

  it('should return observable from getTicketTypesChanges', () => {
    const result = service.getTicketTypesChanges();
    expect(result).toBeTruthy();
    expect(result.subscribe).toBeDefined();
  });

  it('should handle ngOnDestroy', () => {
    expect(() => service.ngOnDestroy()).not.toThrow();
  });

  it('should disconnect when connecting again', () => {
    spyOn(service, 'disconnect').and.callThrough();
    service.connect('2026-01-15');
    service.connect('2026-01-16');
    expect(service.disconnect).toHaveBeenCalled();
  });

  it('should handle multiple disconnects', () => {
    expect(() => {
      service.disconnect();
      service.disconnect();
      service.disconnect();
    }).not.toThrow();
  });
});

