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

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should disconnect without errors when not connected', () => {
    expect(() => service.disconnect()).not.toThrow();
  });
});

