import { Injectable, OnDestroy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { getBackendBaseUrl } from '../../config/backend';
import { TicketAvailability } from './checkout.service';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({ providedIn: 'root' })
export class AvailabilityWebSocketService implements OnDestroy {
  private client: Client | null = null;
  private availabilitySubject = new Subject<TicketAvailability[]>();
  private ticketTypesSubject = new Subject<any[]>();
  private discountChangesSubject = new Subject<void>();
  private currentSubscription: { unsubscribe: () => void } | null = null;
  private ticketTypesSubscription: { unsubscribe: () => void } | null = null;
  private discountsSubscription: { unsubscribe: () => void } | null = null;

  connect(date: string): Observable<TicketAvailability[]> {
    this.disconnect();

    const wsUrl = `${getBackendBaseUrl()}/ws`;

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onConnect = () => {
      this.currentSubscription = this.client!.subscribe(
        `/topic/availability/${date}`,
        (message: IMessage) => {
          const availability = JSON.parse(message.body) as TicketAvailability[];
          this.availabilitySubject.next(availability);
        }
      );

      this.ticketTypesSubscription = this.client!.subscribe(
        '/topic/ticket-types',
        (message: IMessage) => {
          const ticketTypes = JSON.parse(message.body);
          this.ticketTypesSubject.next(ticketTypes);
        }
      );

      this.discountsSubscription = this.client!.subscribe(
        '/topic/discounts',
        () => {
          this.discountChangesSubject.next();
        }
      );
    };

    this.client.onStompError = (frame) => {
      console.error('WebSocket STOMP error:', frame);
    };

    this.client.activate();

    return this.availabilitySubject.asObservable();
  }

  getTicketTypesChanges(): Observable<any[]> {
    return this.ticketTypesSubject.asObservable();
  }

  getDiscountChanges(): Observable<void> {
    return this.discountChangesSubject.asObservable();
  }

  disconnect(): void {
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
      this.currentSubscription = null;
    }
    if (this.ticketTypesSubscription) {
      this.ticketTypesSubscription.unsubscribe();
      this.ticketTypesSubscription = null;
    }
    if (this.discountsSubscription) {
      this.discountsSubscription.unsubscribe();
      this.discountsSubscription = null;
    }
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}

