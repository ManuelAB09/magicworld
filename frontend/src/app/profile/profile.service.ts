import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { AuthService, UserProfile } from '../auth/auth.service';
import { getBackendBaseUrl } from '../config/backend';

export interface UpdateProfileRequest {
  firstname: string;
  lastname: string;
  email: string;
  password?: string;
}

export interface PurchaseLineDTO {
  id: number;
  validDate: string;
  quantity: number;
  totalCost: number;
  ticketTypeName: string;
}

export interface PurchaseDTO {
  id: number;
  purchaseDate: string;
  lines: PurchaseLineDTO[];
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private usersUrl = `${getBackendBaseUrl()}/api/v1/users`;
  private purchasesUrl = `${getBackendBaseUrl()}/api/v1/purchases`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  updateProfile(request: UpdateProfileRequest): Observable<UserProfile> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.put<UserProfile>(
        `${this.usersUrl}/profile`,
        request,
        { withCredentials: true, headers: h }
      ))
    );
  }

  deleteProfile(): Observable<void> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.delete<void>(
        `${this.usersUrl}/profile`,
        { withCredentials: true, headers: h }
      ))
    );
  }

  getMyPurchases(): Observable<PurchaseDTO[]> {
    const headers = new HttpHeaders();
    return this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.get<PurchaseDTO[]>(
        `${this.purchasesUrl}/my-purchases`,
        { withCredentials: true, headers: h }
      ))
    );
  }
}
