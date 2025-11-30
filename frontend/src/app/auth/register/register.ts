import { Component } from '@angular/core';
import { AuthService } from '../auth.service';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../../error/error-service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, TranslateModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register {
  username = '';
  firstname = '';
  lastname = '';
  email = '';
  password = '';
  confirmPassword = '';
  errorCode: string | null = null;
  errorArgs: any = {};
  errorMessages: string[] = [];
  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private errorService: ErrorService,
    private auth: AuthService,
    private translate: TranslateService
  ) {}

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

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
