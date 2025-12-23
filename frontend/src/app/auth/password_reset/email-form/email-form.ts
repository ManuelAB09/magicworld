import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../auth.service';
import { ErrorService } from '../../../error/error-service';
import { CommonModule } from '@angular/common';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-email-form',
  templateUrl: './email-form.html',
  styleUrl: './email-form.css',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    TranslatePipe
  ]
})
export class EmailForm {
  emailForm: FormGroup;
  submitted = false;
  successMessage = '';

  errorCode: string | null = null;
  errorArgs: any = {};
  errorMessages: string[] = [];

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private errorService: ErrorService,
    private translate: TranslateService
  ) {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  get email() {
    return this.emailForm.get('email');
  }

  onSubmit() {
    this.submitted = true;
    this.successMessage = '';
    this.errorCode = null;
    this.errorArgs = {};
    this.errorMessages = [];

    if (this.emailForm.invalid) {
      return;
    }

    const emailValue = this.emailForm.value.email;
    this.authService.forgotPassword(emailValue).subscribe({
      next: () => {
        this.errorCode = null;
        this.errorArgs = {};
        this.errorMessages = [];
        this.successMessage = this.translate.instant('passwordReset.success');
        this.emailForm.reset();
        this.submitted = false;
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
