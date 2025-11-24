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

  findAll(filters?: { minHeight?: number; minWeight?: number; minAge?: number }): Observable<Attraction[]> {
    if (!filters) {
      return this.http.get<Attraction[]>(this.baseUrl, { withCredentials: true});
    }
    let params: any = {};
    if (filters.minHeight != null) params.minHeight = String(filters.minHeight);
    if (filters.minWeight != null) params.minWeight = String(filters.minWeight);
    if (filters.minAge != null) params.minAge = String(filters.minAge);
    return this.http.get<Attraction[]>(this.baseUrl, { withCredentials: true, params });
  }

  findById(id: number): Observable<Attraction> {
    return this.withCsrf((h) => this.http.get<Attraction>(`${this.baseUrl}/${id}`, { withCredentials: true, headers: h }));
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
