import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TicketType {
  id: number;
  cost: number; // BigDecimal serialized
  currency: string;
  typeName: string;
  description: string;
  maxPerDay: number;
}

@Injectable({ providedIn: 'root' })
export class TicketTypeApiService {
  private baseUrl = 'http://localhost:8080/api/v1/ticket-types';

  constructor(private http: HttpClient) {}

  findAll(): Observable<TicketType[]> {
    return this.http.get<TicketType[]>(this.baseUrl, { withCredentials: true });
  }
}

