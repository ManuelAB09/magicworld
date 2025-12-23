import { TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { App } from './app';
import { provideRouter } from '@angular/router';
import { AuthService, Role } from './auth/auth.service';
import { Observable, of, Subject } from 'rxjs';

class MockAuthService {
  authChanged = new Subject<boolean>();
  isAuthenticated(): Observable<boolean> { return of(false); }
  checkRole(): Observable<Role | null> { return of(null); }
  logout(): Observable<any> { return of({}); }
  notifyAuthChanged(isAuth: boolean) { this.authChanged.next(isAuth); }
}

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        App,
        TranslateModule.forRoot(),
      ],
      providers: [
        provideRouter([]),
        { provide: AuthService, useClass: MockAuthService },
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render navbar', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-navbar')).toBeTruthy();
  });
});
