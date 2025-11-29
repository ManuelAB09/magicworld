import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {catchError, map, Observable, of, Subject, switchMap} from 'rxjs';
import { Router } from '@angular/router';

export enum Role {
  ADMIN = 'ADMIN',
  USER = 'USER'
}
export interface UserDTO {
  username: string;
  firstname: string;
  lastname: string;
  email: string;
  role: Role | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/v1/auth';

  constructor(private http: HttpClient, private router: Router) {}

  authChanged = new Subject<boolean>();

  login(credentials: { username: string; password: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials, { withCredentials: true }).pipe(
      map(response => {
        this.router.navigate(['/']);
        return response;
      })
    );
  }

  register(data: {
    username: string;
    firstname: string;
    lastname: string;
    email: string;
    password: string;
    confirmPassword: string;
  }): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data, { withCredentials: true }).pipe(
      map(response => {
        this.router.navigate(['/']);
        return response;
      })
    );
  }

  logout(): Observable<any> {
    const headers = new HttpHeaders();
    return this.ensureCsrfToken(headers).pipe(
      switchMap(updatedHeaders =>
        this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true, headers: updatedHeaders }).pipe(
          map(response => {
            this.router.navigate(['/']);
            return response;
          })
        )
      )
    );
  }
  getCookie(name: string): string | null {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? decodeURIComponent(match[2]) : null;
  }

  getCsrfTokenFromServer(): Observable<string> {
    return this.http.get(`${this.apiUrl}/csrf-token`, {
      withCredentials: true,
      observe: 'response'
    }).pipe(
      map((resp: HttpResponse<any>) => {
        const header = resp.headers.get('X-XSRF-TOKEN');
        if (header) {
          return header;
        }
      return '';})
    );
  }

  ensureCsrfToken(headers: HttpHeaders): Observable<HttpHeaders> {
    const csrfToken = this.getCookie('XSRF-TOKEN');
    if (csrfToken) {
      return of(headers.set('X-XSRF-TOKEN', csrfToken));
    }
    return this.getCsrfTokenFromServer().pipe(
      map(token => token ? headers.set('X-XSRF-TOKEN', token) : headers)
    );
  }


  isAuthenticated(): Observable<boolean> {
    return this.http.get(
      `${this.apiUrl}/csrf-token`,
      { withCredentials: true }
    ).pipe(
      map(() => true),
      catchError(error => {
        if (error.status === 401) {
          return of(false);
        }
        throw error;
      })
    );
  }

  checkRoleSecure(): Observable<Role | null> {
    const headers = new HttpHeaders();
    return this.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.get<UserDTO>(`${this.apiUrl}/me`, { withCredentials: true, headers: h })),
      map(user => user.role)
    );
  }

  notifyAuthChanged(isAuth: boolean) {
    this.authChanged.next(isAuth);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, email, {
      withCredentials: true,
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/reset-password`, {
      token,
      newPassword
    }, { withCredentials: true });
  }



}
