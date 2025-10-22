import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth-service';

export interface TicketType {
  id?: number;
  cost: number;
  currency: string;
  typeName: string;
  description: string;
  maxPerDay: number;
  photoUrl: string;
}

export type TicketTypeData = Omit<TicketType, 'id' | 'photoUrl'>;

@Injectable({ providedIn: 'root' })
export class TicketTypeApiService {
  private baseUrl = 'http://localhost:8080/api/v1/ticket-types';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private withCsrf<T>(op: (h: HttpHeaders) => Observable<T>): Observable<T> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(switchMap(op));
  }

  findAll(): Observable<TicketType[]> {
    return this.withCsrf((h) => this.http.get<TicketType[]>(this.baseUrl, { withCredentials: true, headers: h }));
  }

  findById(id: number): Observable<TicketType> {
    return this.withCsrf((h) => this.http.get<TicketType>(`${this.baseUrl}/${id}`, { withCredentials: true, headers: h }));
  }


  create(tt: TicketType): Observable<TicketType> {
    return this.withCsrf((h) => this.http.post<TicketType>(this.baseUrl, tt, { withCredentials: true, headers: h }));
  }


  update(id: number, tt: TicketType): Observable<TicketType> {
    return this.withCsrf((h) => this.http.put<TicketType>(`${this.baseUrl}/${id}`, tt, { withCredentials: true, headers: h }));
  }

  delete(id: number): Observable<void> {
    return this.withCsrf((h) => this.http.delete<void>(`${this.baseUrl}/${id}`, { withCredentials: true, headers: h }));
  }

  private buildForm(data: TicketTypeData, file?: File): FormData {
    const fd = new FormData();
    const json = new Blob([JSON.stringify(data)], { type: 'application/json' });
    fd.append('data', json);
    if (file) fd.append('photo', file);
    return fd;
  }

  createMultipart(data: TicketTypeData, file: File): Observable<TicketType> {
    const form = this.buildForm(data, file);
    return this.withCsrf((h) => this.http.post<TicketType>(this.baseUrl, form, { withCredentials: true, headers: h }));
  }

  updateMultipart(id: number, data: TicketTypeData, file?: File): Observable<TicketType> {
    const form = this.buildForm(data, file);
    return this.withCsrf((h) => this.http.put<TicketType>(`${this.baseUrl}/${id}`, form, { withCredentials: true, headers: h }));
  }
}
