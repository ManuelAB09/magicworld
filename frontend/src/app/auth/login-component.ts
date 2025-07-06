import { Component } from '@angular/core';
import { AuthService } from './auth-service';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';

@Component({
  selector: 'app-login',
  imports: [
    FormsModule,
    TranslateModule
  ],
  template: `
    <div class="login-background">
      <form class="auth-form" (ngSubmit)="onLogin()">
        <h2>{{ 'LOGIN.TITLE' | translate }}</h2>
        <label for="username">{{ 'LOGIN.USERNAME' | translate }}</label>
        <input id="username" [(ngModel)]="username" name="username" required />

        <label for="password">{{ 'LOGIN.PASSWORD' | translate }}</label>
        <input id="password" [(ngModel)]="password" name="password" type="password" required />

        @if (errorMessages.length > 0) {
          @for (msg of errorMessages; track $index) {
            <div class="error">{{ msg }}</div>
          }
        }
        @else if (errorCode) {
          <div class="error">{{ errorCode | translate:errorArgs }}</div>
        }

        <button type="submit">{{ 'LOGIN.BUTTON' | translate }}</button>
      </form>
    </div>
  `,
  styleUrls: ['../static/css/login.component.css']
})
export class LoginComponent {
  username = '';
  password = '';
  errorCode: string|null = null;
  errorArgs: any = {};
  errorMessages: string[] = [];

  constructor(
    private auth: AuthService,
    private errorService: ErrorService,
    private translate: TranslateService
  ) {}

  onLogin() {
    this.auth.login({ username: this.username, password: this.password })
      .subscribe({
        next: () => {
          this.errorCode = null;
          this.errorArgs = {};
          this.errorMessages = [];
          this.auth.notifyAuthChanged(true);
        },
        error: err => {
          const { code, args } = this.errorService.handleError(err);
          this.errorCode = code;
          this.errorArgs = args;
          this.errorMessages = this.errorService.getValidationMessages(code, args);
        }
      });
  }
}
