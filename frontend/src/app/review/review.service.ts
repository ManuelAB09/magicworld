import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { getBackendBaseUrl } from '../config/backend';

export interface ReviewDTO {
  id: number;
  stars: number;
  publicationDate: string;
  visitDate: string;
  description: string;
  username: string;
  purchaseId: number;
}

export interface ReviewRequest {
  purchaseId: number;
  visitDate: string;
  stars: number;
  description: string;
}

export interface ReviewPage {
  content: ReviewDTO[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private baseUrl = `${getBackendBaseUrl()}/api/v1/reviews`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  findAllPaginated(page: number, size: number): Observable<ReviewPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ReviewPage>(this.baseUrl, { params });
  }

  getAvailablePurchases(): Observable<number[]> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.get<number[]>(
        `${this.baseUrl}/available-purchases`,
        { withCredentials: true, headers: h }
      ))
    );
  }

  create(request: ReviewRequest): Observable<ReviewDTO> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.post<ReviewDTO>(
        this.baseUrl,
        request,
        { withCredentials: true, headers: h }
      ))
    );
  }
}
