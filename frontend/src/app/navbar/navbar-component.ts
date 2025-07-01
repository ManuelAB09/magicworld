import {AfterViewInit, Component, OnInit} from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth-service';
import { NgOptimizedImage } from '@angular/common';
import {TranslateModule, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, NgOptimizedImage, TranslateModule],
  template: `
    <nav class="custom-navbar px-4">
      <a class="brand" routerLink="/">
        <img class="brand__logo"
             ngSrc="assets/images/FondoMagicworld.png"
             alt="Magicworld logo"
             width="200"
             height="60"/>
      </a>
      <ul class="nav-list">
        @if (!isAuthenticated) {
          <li class="nav-list__item">
            <a class="nav-btn" routerLink="/login">{{ 'NAV.LOGIN' | translate }}</a>
          </li>
          <li class="nav-list__item">
            <a class="nav-btn" routerLink="/register">{{ 'NAV.REGISTER' | translate }}</a>
          </li>
        }
        @if (isAuthenticated) {
          <li class="nav-list__item">
            <button class="nav-btn" (click)="logout()">{{ 'NAV.LOGOUT' | translate }}</button>
          </li>
        }
        <li class="nav-list__item">
          <a class="nav-btn" routerLink="/season-pass">
            <i class="fa fa-id-card"></i>
            {{ 'NAV.SEASON_PASS' | translate }}
          </a>
        </li>
        <li class="nav-list__item">
          <a class="nav-btn" routerLink="/tickets">
            <i class="fa fa-ticket-alt"></i>
            {{ 'NAV.TICKETS' | translate }}
          </a>
        </li>
        <li class="nav-list__item">
          <a class="nav-btn" routerLink="/hotel-pack">
            <i class="fa fa-bed"></i>
            {{ 'NAV.HOTEL_PACK' | translate }}
          </a>
        </li>
        <li class="nav-list__item">
          <a class="nav-btn" routerLink="/school-groups">
            <i class="fa fa-school"></i>
            {{ 'NAV.SCHOOL_GROUPS' | translate }}
          </a>
        </li>
        <li class="nav-list__item">
          <div class="lang-switcher">
            <span class="world-icon">🌐</span>
            <select class="lang-select" [value]="currentLang" (change)="changeLang($event)">
              <option value="es">ES</option>
              <option value="en">EN</option>
            </select>
          </div>
        </li>
      </ul>
    </nav>
  `,
  styleUrls: ['../static/css/navbar.component.css']
})
export class NavbarComponent implements OnInit, AfterViewInit{
  isAuthenticated = false;
  currentLang = 'es';

  constructor(public auth: AuthService, private translate: TranslateService) {}

  ngOnInit() {
    this.currentLang = this.translate.currentLang || this.translate.getDefaultLang() || 'es';
    this.checkAuth();
    setInterval(() => this.checkAuth(), 7200000);

    this.auth.authChanged.subscribe(isAuth => {
      this.isAuthenticated = isAuth;
    });
  }
  ngAfterViewInit(): void {
    const select = document.querySelector('.lang-select') as HTMLSelectElement;
    if (select) {
      select.addEventListener('change', () => {
        select.blur();
      });
    }
  }


  checkAuth() {
    this.auth.isAuthenticated().subscribe(auth => {
      this.isAuthenticated = auth;
    });
  }

  logout() {
    this.auth.logout().subscribe(() => {
      this.auth.notifyAuthChanged(false);
    });
  }

  changeLang(event: Event) {
    const select = event.target as HTMLSelectElement | null;
    const lang = select?.value || 'es';
    this.currentLang = lang;
    this.translate.use(lang);
  }
}
