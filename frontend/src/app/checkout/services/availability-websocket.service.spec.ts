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

  it('should create a STOMP client on connect', () => {
    service.connect('2026-01-15');
    expect((service as any).client).toBeTruthy();
  });

  it('should set client to null after disconnect', () => {
    service.connect('2026-01-15');
    service.disconnect();
    expect((service as any).client).toBeNull();
  });

  it('should set onConnect handler on client', () => {
    service.connect('2026-01-15');
    const client = (service as any).client;
    expect(client.onConnect).toBeDefined();
    expect(typeof client.onConnect).toBe('function');
  });

  it('should set onStompError handler on client', () => {
    service.connect('2026-01-15');
    const client = (service as any).client;
    expect(client.onStompError).toBeDefined();
    expect(typeof client.onStompError).toBe('function');
  });

  it('should handle onStompError without throwing', () => {
    service.connect('2026-01-15');
    const client = (service as any).client;
    spyOn(console, 'error');
    expect(() => client.onStompError({ headers: {}, body: 'error' })).not.toThrow();
    expect(console.error).toHaveBeenCalled();
  });

  it('should emit availability data when onConnect subscribes and receives message', () => {
    const mockAvailability = [{ id: 1, typeName: 'ADULT', available: 50 }];
    let received: any[] = [];

    const obs = service.connect('2026-01-15');
    obs.subscribe(data => { received = data; });

    const client = (service as any).client;

    // Simulate onConnect by calling the handler with a mock frame
    // We need to mock the client.subscribe method
    const mockUnsubscribe = { unsubscribe: jasmine.createSpy('unsubscribe') };
    client.subscribe = jasmine.createSpy('subscribe').and.callFake(
      (dest: string, callback: (msg: any) => void) => {
        // Simulate receiving a message for the availability topic
        if (dest.includes('/topic/availability/')) {
          callback({ body: JSON.stringify(mockAvailability) });
        }
        return mockUnsubscribe;
      }
    );

    // Trigger onConnect
    client.onConnect();

    expect(received.length).toBe(1);
    expect((received as any)[0].typeName).toBe('ADULT');
  });

  it('should emit ticket types data when receiving on ticket-types topic', () => {
    const mockTicketTypes = [{ id: 1, typeName: 'VIP' }];
    let received: any[] = [];

    service.connect('2026-01-15');
    service.getTicketTypesChanges().subscribe(data => { received = data; });

    const client = (service as any).client;

    const mockUnsubscribe = { unsubscribe: jasmine.createSpy('unsubscribe') };
    client.subscribe = jasmine.createSpy('subscribe').and.callFake(
      (dest: string, callback: (msg: any) => void) => {
        if (dest === '/topic/ticket-types') {
          callback({ body: JSON.stringify(mockTicketTypes) });
        }
        return mockUnsubscribe;
      }
    );

    client.onConnect();

    expect(received.length).toBe(1);
    expect((received as any)[0].typeName).toBe('VIP');
  });

  it('should unsubscribe from topics on disconnect after connect', () => {
    service.connect('2026-01-15');
    const client = (service as any).client;

    const mockUnsubscribe = { unsubscribe: jasmine.createSpy('unsubscribe') };
    client.subscribe = jasmine.createSpy('subscribe').and.returnValue(mockUnsubscribe);

    // Trigger onConnect to set up subscriptions
    client.onConnect();

    // Now disconnect
    spyOn(client, 'deactivate');
    service.disconnect();

    expect(mockUnsubscribe.unsubscribe).toHaveBeenCalledTimes(2);
  });

  it('should configure client with reconnectDelay', () => {
    service.connect('2026-01-15');
    const client = (service as any).client;
    expect(client.reconnectDelay).toBe(5000);
  });

  it('should ngOnDestroy call disconnect', () => {
    spyOn(service, 'disconnect');
    service.ngOnDestroy();
    expect(service.disconnect).toHaveBeenCalled();
  });
});

