import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth-service';

export interface Attraction {
  id?: number;
  name: string;
  intensity: 'LOW' | 'MEDIUM' | 'HIGH';
  minimumHeight: number;
  minimumAge: number;
  minimumWeight: number;
  description: string;
  photoUrl: string;
  isActive: boolean;
}

export type AttractionData = Omit<Attraction, 'id' | 'photoUrl'>;

@Injectable({ providedIn: 'root' })
export class AttractionApiService {
  private baseUrl = 'http://localhost:8080/api/v1/attractions';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private withCsrf<T>(op: (h: HttpHeaders) => Observable<T>): Observable<T> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(switchMap(op));
  }

  findAll(): Observable<Attraction[]> {
    return this.http.get<Attraction[]>(this.baseUrl, { withCredentials: true});
  }

  findById(id: number): Observable<Attraction> {
    return this.withCsrf((h) => this.http.get<Attraction>(`${this.baseUrl}/${id}`, { withCredentials: true, headers: h }));
  }

  createJson(data: Attraction): Observable<Attraction> {
    return this.withCsrf((h) => this.http.post<Attraction>(this.baseUrl, data, { withCredentials: true, headers: h }));
  }

  updateJson(id: number, data: Attraction): Observable<Attraction> {
    return this.withCsrf((h) => this.http.put<Attraction>(`${this.baseUrl}/${id}`, data, { withCredentials: true, headers: h }));
  }

  delete(id: number): Observable<void> {
    return this.withCsrf((h) => this.http.delete<void>(`${this.baseUrl}/${id}`, { withCredentials: true, headers: h }));
  }

  private buildForm(data: AttractionData, file?: File): FormData {
    const fd = new FormData();
    const json = new Blob([JSON.stringify(data)], { type: 'application/json' });
    fd.append('data', json);
    if (file) fd.append('photo', file);
    return fd;
  }


  createMultipart(data: AttractionData, file: File): Observable<Attraction> {
    const form = this.buildForm(data, file);
    return this.withCsrf((h) => this.http.post<Attraction>(this.baseUrl, form, { withCredentials: true, headers: h }));
  }

  updateMultipart(id: number, data: AttractionData, file?: File): Observable<Attraction> {
    const form = this.buildForm(data, file);
    return this.withCsrf((h) => this.http.put<Attraction>(`${this.baseUrl}/${id}`, form, { withCredentials: true, headers: h }));
  }
}
