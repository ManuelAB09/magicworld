import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';
import { TicketTypeApiService, TicketTypeData } from './ticket-type.service';
import { catchError, of } from 'rxjs';
import { getBackendBaseUrl } from '../config/backend';

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

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  existingPhotoUrl: string | null = null;
  photoRequiredError = false;

  private apiBase = getBackendBaseUrl();
  private readonly maxFileBytes = 50 * 1024 * 1024;

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
        if (tt) {
          this.form.patchValue({
            typeName: tt.typeName,
            description: tt.description,
            currency: tt.currency,
            cost: tt.cost,
            maxPerDay: tt.maxPerDay
          });
          this.existingPhotoUrl = tt.photoUrl;
        }
        this.loading = false;
      });
    } else {
      this.loading = false;
    }
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      this.selectedFile = null;
      this.previewUrl = null;
      return;
    }
    const file = input.files[0];
    if (!file.type.startsWith('image/')) {

      this.selectedFile = null;
      this.previewUrl = null;
      this.photoRequiredError = true;
      return;
    }

    if (file.size > this.maxFileBytes) {
      this.selectedFile = null;
      this.previewUrl = null;
      this.photoRequiredError = false;
      this.errorKey = 'error.file.size_exceeded';

      this.errorArgs = { 0: this.maxFileBytes };
      this.validationMessages = [];
      return;
    }
    this.selectedFile = file;
    this.photoRequiredError = false;
    const reader = new FileReader();
    reader.onload = () => this.previewUrl = reader.result as string;
    reader.readAsDataURL(file);
  }

  private getDataFromForm(): TicketTypeData {
    return {
      typeName: this.form.value.typeName,
      description: this.form.value.description,
      currency: this.form.value.currency,
      cost: Number(this.form.value.cost),
      maxPerDay: Number(this.form.value.maxPerDay)
    };
  }

  getImageUrl(url: string | null): string | null {
    if (!url) return null;
    if (url.startsWith('http')) return url;
    return this.apiBase + url;
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.isEdit && !this.selectedFile) {
      this.photoRequiredError = true;
      return;
    }

    this.loading = true;
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];

    const data = this.getDataFromForm();

    const obs = this.isEdit && this.id
      ? this.api.updateMultipart(this.id, data, this.selectedFile || undefined)
      : this.api.createMultipart(data, this.selectedFile!);

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
