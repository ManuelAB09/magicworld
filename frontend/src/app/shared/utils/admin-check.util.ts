import { Observable, catchError, map, of } from 'rxjs';
import { AuthService, Role } from '../../auth/auth.service';

/**
 * Verifica si el usuario actual tiene rol de administrador.
 * Retorna un Observable<boolean>.
 */
export function checkAdminRole(auth: AuthService): Observable<boolean> {
  return auth.checkRoleSecure().pipe(
    map(role => role === Role.ADMIN),
    catchError(() => of(false))
  );
}

