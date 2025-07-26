import {AfterViewInit, Component, OnInit} from '@angular/core';
import { RouterLink } from '@angular/router';
import {AuthService, Role} from '../auth/auth-service';
import { NgOptimizedImage } from '@angular/common';
import {TranslateModule, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, NgOptimizedImage, TranslateModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class Navbar implements OnInit, AfterViewInit{
  isAuthenticated = false;
  currentLang = 'es';
  role: Role | null = null;

  constructor(public auth: AuthService, private translate: TranslateService) {}

  ngOnInit() {
    this.currentLang = this.translate.currentLang || this.translate.getDefaultLang() || 'es';
    this.checkAuth();
    this.getRole();
    setInterval(() => this.checkAuth(), 7200000);


    this.auth.authChanged.subscribe(isAuth => {
      this.isAuthenticated = isAuth;
      if (isAuth) {
        this.getRole();
      } else {
        this.role = null;
      }
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
      this.role = null;
    });
  }

  getRole() {
    this.auth.checkRole().subscribe(role => {
      this.role = role;
    });
  }

  changeLang(event: Event) {
    const select = event.target as HTMLSelectElement | null;
    const lang = select?.value || 'es';
    this.currentLang = lang;
    this.translate.use(lang);
  }
}
