import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { getBackendBaseUrl } from '../config/backend';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface AttractionStatus {
  attractionId: number;
  name: string;
  isOpen: boolean;
  queueSize: number;
  estimatedWaitMinutes: number;
  mapPositionX: number;
  mapPositionY: number;
  intensity: string;
}

export interface ResolutionOption {
  id: string;
  label: string;
  description: string;
  enabled: boolean;
}

export interface AlertDTO {
  id: number;
  alertType: string;
  severity: string;
  message: string;
  suggestion: string;
  attractionId: number;
  attractionName: string;
  timestamp: string;
  isActive: boolean;
  resolutionOptions: ResolutionOption[];
}

export interface ResolutionResult {
  success: boolean;
  message: string;
  code?: string;
  args?: any[];
  actionTaken: string;
  resourcesUsed: Record<string, any>;
  failureReason?: string;
}

export interface DashboardSnapshot {
  currentVisitors: number;
  totalEntriesToday: number;
  totalSalesToday: number;
  activeAttractions: number;
  totalAttractions: number;
  avgParkWaitTime: number;
  ticketsSoldToday: number;
  parkMaxCapacity: number;
  attractionStatuses: AttractionStatus[];
  activeAlerts: AlertDTO[];
}

export interface SimulatorStatus {
  running: boolean;
  simulatedVisitors: number;
  activeQueues: number;
  totalInQueues: number;
}

@Injectable({ providedIn: 'root' })
export class MonitoringService {
  private baseUrl = `${getBackendBaseUrl()}/api/v1/monitoring`;
  private stompClient: Client | null = null;

  private dashboardSubject = new Subject<DashboardSnapshot>();
  private alertSubject = new Subject<AlertDTO>();

  dashboard$ = this.dashboardSubject.asObservable();
  alerts$ = this.alertSubject.asObservable();

  constructor(private http: HttpClient, private auth: AuthService) { }

  private withCsrf<T>(op: (h: HttpHeaders) => Observable<T>): Observable<T> {
    return this.auth.ensureCsrfToken(new HttpHeaders()).pipe(switchMap(op));
  }

  getDashboard(): Observable<DashboardSnapshot> {
    return this.http.get<DashboardSnapshot>(`${this.baseUrl}/dashboard`, { withCredentials: true });
  }

  getActiveAlerts(): Observable<AlertDTO[]> {
    return this.http.get<AlertDTO[]>(`${this.baseUrl}/alerts`, { withCredentials: true });
  }

  resolveAlert(id: number, resolutionOptionId: string, employeeId?: number): Observable<ResolutionResult> {
    return this.withCsrf(h =>
      this.http.post<ResolutionResult>(
        `${this.baseUrl}/alerts/${id}/resolve`,
        { resolutionOptionId, employeeId },
        { withCredentials: true, headers: h }
      )
    );
  }

  startSimulator(): Observable<SimulatorStatus> {
    return this.withCsrf(h =>
      this.http.post<SimulatorStatus>(`${this.baseUrl}/simulator/start`, {}, { withCredentials: true, headers: h })
    );
  }

  stopSimulator(): Observable<SimulatorStatus> {
    return this.withCsrf(h =>
      this.http.post<SimulatorStatus>(`${this.baseUrl}/simulator/stop`, {}, { withCredentials: true, headers: h })
    );
  }

  getSimulatorStatus(): Observable<SimulatorStatus> {
    return this.http.get<SimulatorStatus>(`${this.baseUrl}/simulator/status`, { withCredentials: true });
  }

  connectWebSocket(): void {
    if (this.stompClient?.active) return;

    const socket = new SockJS(`${getBackendBaseUrl()}/ws`);
    this.stompClient = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      onConnect: () => this.subscribeToTopics(),
      onStompError: (frame) => console.error('STOMP error', frame)
    });
    this.stompClient.activate();
  }

  private subscribeToTopics(): void {
    if (!this.stompClient) return;

    this.stompClient.subscribe('/topic/dashboard', (message) => {
      const snapshot = JSON.parse(message.body) as DashboardSnapshot;
      this.dashboardSubject.next(snapshot);
    });

    this.stompClient.subscribe('/topic/alerts', (message) => {
      const alert = JSON.parse(message.body) as AlertDTO;
      this.alertSubject.next(alert);
    });
  }

  disconnectWebSocket(): void {
    this.stompClient?.deactivate();
    this.stompClient = null;
  }
}
