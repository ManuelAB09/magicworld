import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { getBackendBaseUrl } from '../config/backend';
import { AttractionStatus, SimulatorStatus } from '../admin-dashboard/monitoring.service';

@Injectable({ providedIn: 'root' })
export class ParkStatusService {
  private baseUrl = `${getBackendBaseUrl()}/api/v1/park-status`;

  constructor(private http: HttpClient) {}

  getSimulatorStatus(): Observable<SimulatorStatus> {
    return this.http.get<SimulatorStatus>(`${this.baseUrl}/simulator`, { withCredentials: true });
  }

  getAttractionStatuses(): Observable<AttractionStatus[]> {
    return this.http.get<AttractionStatus[]>(`${this.baseUrl}/attractions`, { withCredentials: true });
  }
}

