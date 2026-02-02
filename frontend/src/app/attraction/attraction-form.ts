import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';
import { AttractionApiService, AttractionData, AttractionCategory } from './attraction.service';
import { catchError, of } from 'rxjs';
import { MapPicker3DComponent } from './map-picker-3d';
import { getImageUrl, handleApiError, validateImageFile, readFileAsDataUrl, DEFAULT_MAX_FILE_BYTES } from '../shared/utils';

@Component({
  selector: 'app-attraction-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, TranslatePipe, MapPicker3DComponent],
  templateUrl: './attraction-form.html',
  styleUrls: ['./attraction-form.css']
})
export class AttractionForm implements OnInit {
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


  intensities: Array<'LOW'|'MEDIUM'|'HIGH'> = ['LOW','MEDIUM','HIGH'];
  categories: AttractionCategory[] = [
    'ROLLER_COASTER', 'FERRIS_WHEEL', 'CAROUSEL', 'WATER_RIDE',
    'HAUNTED_HOUSE', 'DROP_TOWER', 'BUMPER_CARS', 'TRAIN_RIDE', 'SWING_RIDE', 'OTHER'
  ];

  constructor(
    private fb: FormBuilder,
    private api: AttractionApiService,
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private error: ErrorService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(50)]],
      intensity: ['LOW', [Validators.required]],
      category: ['OTHER', [Validators.required]],
      minimumHeight: [0, [Validators.required, Validators.min(0)]],
      minimumAge: [0, [Validators.required, Validators.min(0)]],
      minimumWeight: [0, [Validators.required, Validators.min(0)]],
      description: ['', [Validators.required, Validators.maxLength(255)]],
      isActive: [true, [Validators.required]],
      mapPositionX: [50, [Validators.required, Validators.min(0), Validators.max(100)]],
      mapPositionY: [50, [Validators.required, Validators.min(0), Validators.max(100)]]
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
      ).subscribe(a => {
        if (a) {
          this.form.patchValue({
            name: a.name,
            intensity: a.intensity,
            category: a.category,
            minimumHeight: a.minimumHeight,
            minimumAge: a.minimumAge,
            minimumWeight: a.minimumWeight,
            description: a.description,
            isActive: a.isActive,
            mapPositionX: a.mapPositionX,
            mapPositionY: a.mapPositionY
          });
          this.existingPhotoUrl = a.photoUrl;
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

  private getDataFromForm(): AttractionData {
    return {
      name: this.form.value.name,
      intensity: this.form.value.intensity,
      category: this.form.value.category,
      minimumHeight: Number(this.form.value.minimumHeight),
      minimumAge: Number(this.form.value.minimumAge),
      minimumWeight: Number(this.form.value.minimumWeight),
      description: this.form.value.description,
      isActive: !!this.form.value.isActive,
      mapPositionX: Number(this.form.value.mapPositionX),
      mapPositionY: Number(this.form.value.mapPositionY)
    };
  }

  onMapPreviewClick(event: MouseEvent): void {
    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const x = ((event.clientX - rect.left) / rect.width) * 100;
    const y = ((event.clientY - rect.top) / rect.height) * 100;
    this.form.patchValue({
      mapPositionX: Math.round(x * 10) / 10,
      mapPositionY: Math.round(y * 10) / 10
    });
  }

  onMapPositionChange(event: { x: number; y: number }): void {
    this.form.patchValue({
      mapPositionX: event.x,
      mapPositionY: event.y
    });
    this.form.get('mapPositionX')?.markAsTouched();
    this.form.get('mapPositionY')?.markAsTouched();
  }

  getMarkerStyle(): { [key: string]: string } {
    const x = this.form.value.mapPositionX ?? 50;
    const y = this.form.value.mapPositionY ?? 50;
    return {
      left: `${x}%`,
      top: `${y}%`
    };
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
      if (res) this.router.navigate(['/attractions']);
    });
  }

  delete() {
    if (!this.isEdit || !this.id) return;
    if (!confirm(this.translate.instant('ATTRACTION_FORM.CONFIRM_DELETE'))) return;

    this.loading = true;
    this.api.delete(this.id).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/attractions']);
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
