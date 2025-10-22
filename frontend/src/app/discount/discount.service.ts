import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth-service';

export interface Discount {
  id?: number;
  discountPercentage: number;
  expiryDate: string; // ISO yyyy-MM-dd
  discountCode: string;
}

export interface DiscountRequest {
  discount: Discount;
  applicableTicketTypesNames: string[];
}

@Injectable({ providedIn: 'root' })
export class DiscountApiService {
  private baseUrl = 'http://localhost:8080/api/v1/discounts';

  constructor(private http: HttpClient, private auth: AuthService) {}

  findAll(): Observable<Discount[]> {
    return this.http.get<Discount[]>(this.baseUrl, { withCredentials: true });
  }

  findById(id: number): Observable<Discount> {
    return this.http.get<Discount>(`${this.baseUrl}/${id}`, { withCredentials: true });
  }

  getTicketTypesByDiscount(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${id}/ticket-types`, { withCredentials: true });
  }

  create(req: DiscountRequest): Observable<Discount> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.post<Discount>(this.baseUrl, req, { withCredentials: true, headers: h }))
    );
  }

  update(id: number, req: DiscountRequest): Observable<Discount> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.put<Discount>(`${this.baseUrl}/${id}`, req, { withCredentials: true, headers: h }))
    );
  }

  delete(id: number): Observable<void> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.delete<void>(`${this.baseUrl}/${id}`, { withCredentials: true, headers: h }))
    );
  }
}

