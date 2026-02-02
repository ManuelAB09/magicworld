import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';
import { TicketTypeApiService, TicketTypeData } from './ticket-type.service';
import { catchError, of } from 'rxjs';
import { CurrencyService } from '../shared/currency.service';
import { getImageUrl, handleApiError, validateImageFile, readFileAsDataUrl, DEFAULT_MAX_FILE_BYTES } from '../shared/utils';

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

  constructor(
    private fb: FormBuilder,
    private api: TicketTypeApiService,
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private error: ErrorService,
    private currencyService: CurrencyService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.form = this.fb.group({
      typeName: ['', [Validators.required, Validators.maxLength(50)]],
      description: ['', [Validators.required, Validators.maxLength(255)]],
      cost: [null, [Validators.required, Validators.min(0.01)]],
      maxPerDay: [null, [Validators.required, Validators.min(1)]]
    });
  }

  private loadData(): void {
    this.loading = true;
    this.clearError();

    const idParam = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!idParam;
    if (idParam) {
      this.id = Number(idParam);
      this.api.findById(this.id).pipe(
        catchError(err => {
          this.setError(err);
          return of(null);
        })
      ).subscribe(tt => {
        if (tt) {
          this.form.patchValue({
            typeName: tt.typeName,
            description: tt.description,
            cost: this.currencyService.convertFromEur(tt.cost),
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
    const validation = validateImageFile(file, DEFAULT_MAX_FILE_BYTES);

    if (!validation.valid) {
      this.selectedFile = null;
      this.previewUrl = null;
      if (validation.error === 'invalid_type') {
        this.photoRequiredError = true;
      } else if (validation.error === 'size_exceeded') {
        this.photoRequiredError = false;
        this.errorKey = 'error.file.size_exceeded';
        this.errorArgs = { 0: DEFAULT_MAX_FILE_BYTES };
        this.validationMessages = [];
      }
      return;
    }

    this.selectedFile = file;
    this.photoRequiredError = false;
    readFileAsDataUrl(file).then(url => this.previewUrl = url);
  }

  private getDataFromForm(): TicketTypeData {
    return {
      typeName: this.form.value.typeName,
      description: this.form.value.description,
      cost: this.currencyService.convertToEur(Number(this.form.value.cost)),
      maxPerDay: Number(this.form.value.maxPerDay)
    };
  }

  getCurrencySymbol(): string {
    return this.currencyService.getCurrencySymbol();
  }

  getImageUrl(url: string | null): string | null {
    return getImageUrl(url);
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
    this.clearError();

    const data = this.getDataFromForm();
    const obs = this.isEdit && this.id
      ? this.api.updateMultipart(this.id, data, this.selectedFile || undefined)
      : this.api.createMultipart(data, this.selectedFile!);

    obs.pipe(
      catchError(err => {
        this.setError(err);
        return of(null);
      })
    ).subscribe(res => {
      this.loading = false;
      if (res) this.router.navigate(['/ticket-types']);
    });
  }

  delete() {
    if (!this.isEdit || !this.id) return;
    if (!confirm(this.translate.instant('TICKET_TYPE_FORM.CONFIRM_DELETE'))) return;

    this.loading = true;
    this.api.delete(this.id).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/ticket-types']);
      },
      error: (err) => {
        this.loading = false;
        this.setError(err);
      }
    });
  }

  private clearError(): void {
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];
  }

  private setError(err: any): void {
    const state = handleApiError(err, this.error);
    this.errorKey = state.errorKey;
    this.errorArgs = state.errorArgs;
    this.validationMessages = state.validationMessages;
  }
}
