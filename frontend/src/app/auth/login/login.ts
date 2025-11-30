import { Component } from '@angular/core';
import { AuthService } from '../auth.service';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../../error/error-service';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [
    FormsModule,
    TranslateModule,
    RouterLink
  ],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  username = '';
  password = '';
  errorCode: string|null = null;
  errorArgs: any = {};
  errorMessages: string[] = [];
  showPassword = false;

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

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
