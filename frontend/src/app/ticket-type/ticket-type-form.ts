import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';
import { TicketType, TicketTypeApiService } from './ticket-type.service';
import { catchError, of, switchMap } from 'rxjs';

@Component({
  selector: 'app-ticket-type-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, TranslatePipe],
  templateUrl: './ticket-type-form.html',
  styleUrls: ['./ticket-type-form.css']
})
export class TicketTypeForm implements OnInit {
  form!: FormGroup;
  loading = false;
  errorKey: string | null = null;
  errorArgs: any = null;
  validationMessages: string[] = [];
  isEdit = false;
  id: number | null = null;

  constructor(
    private fb: FormBuilder,
    private api: TicketTypeApiService,
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private error: ErrorService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      typeName: ['', [Validators.required, Validators.maxLength(50)]],
      description: ['', [Validators.required, Validators.maxLength(255)]],
      currency: ['', [Validators.required]],
      cost: [null, [Validators.required, Validators.min(0.01)]],
      maxPerDay: [null, [Validators.required, Validators.min(1)]]
    });

    this.loading = true;
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];

    const idParam = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!idParam;
    if (idParam) {
      this.id = Number(idParam);
      this.api.findById(this.id).pipe(
        catchError(err => {
          const mapped = this.error.handleError(err);
          this.errorKey = mapped.code;
          this.errorArgs = mapped.args;
          this.validationMessages = this.error.getValidationMessages(mapped.code, mapped.args);
          return of(null);
        })
      ).subscribe(tt => {
        if (tt) this.form.patchValue(tt);
        this.loading = false;
      });
    } else {
      this.loading = false;
    }
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];

    const payload: TicketType = { ...this.form.value } as TicketType;

    const obs = this.isEdit && this.id ? this.api.update(this.id, payload) : this.api.create(payload);
    obs.pipe(
      catchError(err => {
        const mapped = this.error.handleError(err);
        this.errorKey = mapped.code;
        this.errorArgs = mapped.args;
        this.validationMessages = this.error.getValidationMessages(mapped.code, mapped.args);
        return of(null);
      })
    ).subscribe(res => {
      this.loading = false;
      if (res) this.router.navigate(['/ticket-types']);
    });
  }

  delete() {
    if (!this.isEdit || !this.id) return;
    const ok = confirm(this.translate.instant('TICKET_TYPE_FORM.CONFIRM_DELETE'));
    if (!ok) return;
    this.loading = true;
    this.api.delete(this.id).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/ticket-types']);
      },
      error: (err) => {
        this.loading = false;
        const mapped = this.error.handleError(err);
        this.errorKey = mapped.code;
        this.errorArgs = mapped.args;
        this.validationMessages = this.error.getValidationMessages(mapped.code, mapped.args);
      }
    });
  }
}

