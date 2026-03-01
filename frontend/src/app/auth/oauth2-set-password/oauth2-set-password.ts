import { Component } from '@angular/core';
import { AuthService } from '../auth.service';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../../error/error-service';

@Component({
  selector: 'app-oauth2-set-password',
  imports: [FormsModule, TranslateModule],
  templateUrl: './oauth2-set-password.html',
  styleUrls: ['./oauth2-set-password.css']
})
export class OAuth2SetPassword {
  password = '';
  confirmPassword = '';
  errorCode: string | null = null;
  errorArgs: any = {};
  errorMessages: string[] = [];
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private auth: AuthService,
    private errorService: ErrorService,
    private translate: TranslateService
  ) {}

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onSubmit() {
    this.auth.completeOAuth2Registration({
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
