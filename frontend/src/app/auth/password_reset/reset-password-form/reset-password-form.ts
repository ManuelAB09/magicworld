import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../auth.service';
import { ErrorService } from '../../../error/error-service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-reset-password-form',
  templateUrl: './reset-password-form.html',
  styleUrl: './reset-password-form.css',
  imports: [ReactiveFormsModule, TranslatePipe]
})
export class ResetPasswordForm {
  resetForm: FormGroup;
  submitted = false;
  successMessage = '';
  errorCode: string | null = null;
  errorArgs: any = {};
  errorMessages: string[] = [];
  token: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private errorService: ErrorService,
    private translate: TranslateService
  ) {
    this.resetForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsMatchValidator });
    this.token = this.route.snapshot.queryParamMap.get('token');
  }

  get password() {
    return this.resetForm.get('password');
  }
  get confirmPassword() {
    return this.resetForm.get('confirmPassword');
  }

  passwordsMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  }

  onSubmit() {
    this.submitted = true;
    this.successMessage = '';
    this.errorCode = null;
    this.errorArgs = {};
    this.errorMessages = [];

    if (this.resetForm.invalid || !this.token) {
      return;
    }

    const passwordValue = this.resetForm.value.password;
    this.authService.resetPassword(this.token, passwordValue).subscribe({
      next: () => {
        this.successMessage = this.translate.instant('resetPassword.success');
        this.errorCode = null;
        this.errorArgs = {};
        this.errorMessages = [];
        setTimeout(() => this.router.navigate(['/login']), 2000);
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
