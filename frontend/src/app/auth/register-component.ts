import { Component } from '@angular/core';
import { AuthService } from './auth-service';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, TranslateModule],
  template: `
    <div class="register-background">
      <form class="auth-form" (ngSubmit)="onRegister()">
        <h2>{{ 'REGISTER.TITLE' | translate }}</h2>
        <label for="username">{{ 'REGISTER.USERNAME' | translate }}</label>
        <input id="username" [(ngModel)]="username" name="username" required/>

        <label for="firstname">{{ 'REGISTER.FIRSTNAME' | translate }}</label>
        <input id="firstname" [(ngModel)]="firstname" name="firstname" required/>

        <label for="lastname">{{ 'REGISTER.LASTNAME' | translate }}</label>
        <input id="lastname" [(ngModel)]="lastname" name="lastname" required/>

        <label for="email">{{ 'REGISTER.EMAIL' | translate }}</label>
        <input id="email" [(ngModel)]="email" name="email" required/>

        <label for="password">{{ 'REGISTER.PASSWORD' | translate }}</label>
        <input id="password" [(ngModel)]="password" name="password" type="password" required/>

        <label for="confirmPassword">{{ 'REGISTER.CONFIRM' | translate }}</label>
        <input id="confirmPassword" [(ngModel)]="confirmPassword" name="confirmPassword" type="password" required/>

        @if (errorMessages.length) {
          @for (msg of errorMessages; track $index) {
            <div class="error">{{ msg }}</div>
          }
        }
        @if (errorCode && !errorMessages.length) {
          <div class="error">{{ errorCode | translate:errorArgs }}</div>
        }
        <button type="submit">{{ 'REGISTER.BUTTON' | translate }}</button>
      </form>
    </div>
  `,
  styleUrls: ['../static/css/register.component.css']
})
export class RegisterComponent {
  username = '';
  firstname = '';
  lastname = '';
  email = '';
  password = '';
  confirmPassword = '';
  errorCode: string | null = null;
  errorArgs: any = {};
  errorMessages: string[] = [];

  constructor(
    private errorService: ErrorService,
    private auth: AuthService,
    private translate: TranslateService
  ) {}

  onRegister() {
    this.auth.register({
      username: this.username,
      firstname: this.firstname,
      lastname: this.lastname,
      email: this.email,
      password: this.password,
      confirmPassword: this.confirmPassword
    }).subscribe({
      next: () => {
        this.auth.notifyAuthChanged(true);
        this.errorCode = null;
        this.errorArgs = {};
        this.errorMessages = [];
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
