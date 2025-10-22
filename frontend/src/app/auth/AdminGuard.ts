import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService, Role } from './auth-service';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): Observable<boolean> {
    return this.auth.checkRoleSecure().pipe(
      map(role => {
        if (role === Role.ADMIN) {
          return true;
        }
        this.router.navigate(['/discounts']);
        return false;
      }),
      catchError(() => {
        this.router.navigate(['/discounts']);
        return of(false);
      })
    );
  }
}
