import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DiscountApiService, Discount, DiscountRequest } from './discount.service';
import { TicketType, TicketTypeApiService } from '../ticket-type/ticket-type.service';
import { catchError, of, switchMap } from 'rxjs';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';

@Component({
  selector: 'app-discount-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, TranslatePipe],
  templateUrl: './discount-form.html',
  styleUrls: ['./discount-form.css']
})
export class DiscountForm implements OnInit {
  form!: FormGroup;
  allTicketTypes: TicketType[] = [];
  selectedNames = new Set<string>();
  loading = false;
  errorKey: string | null = null;
  errorArgs: any = null;
  validationMessages: string[] = [];
  isEdit = false;
  id: number | null = null;

  constructor(
    private fb: FormBuilder,
    private api: DiscountApiService,
    private ticketsApi: TicketTypeApiService,
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService,
    private error: ErrorService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      discountPercentage: [null, [Validators.required, Validators.min(1), Validators.max(100)]],
      expiryDate: ['', [Validators.required]],
      discountCode: ['', [Validators.required, Validators.maxLength(20)]]
    });

    this.loading = true;
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];

    this.ticketsApi.findAll().pipe(
      switchMap(tt => {
        this.allTicketTypes = tt || [];
        const idParam = this.route.snapshot.paramMap.get('id');
        this.isEdit = !!idParam;
        if (idParam) {
          this.id = Number(idParam);
          return this.api.findById(this.id).pipe(
            switchMap((d: Discount) => {
              this.form.patchValue({
                discountPercentage: d.discountPercentage,
                expiryDate: d.expiryDate,
                discountCode: d.discountCode
              });
              return this.api.getTicketTypesByDiscount(this.id!).pipe(
                catchError(() => of([]))
              );
            })
          );
        }
        return of([]);
      }),
      catchError((err) => {
        const mapped = this.error.handleError(err);
        this.errorKey = mapped.code;
        this.errorArgs = mapped.args;
        this.validationMessages = this.error.getValidationMessages(mapped.code, mapped.args);
        return of([]);
      })
    ).subscribe((associated: any[]) => {
      if (associated && associated.length) {
        associated.forEach(tt => this.selectedNames.add(tt.typeName));
      }
      this.loading = false;
    });
  }

  toggleTypeName(name: string, checked: boolean) {
    if (checked) this.selectedNames.add(name); else this.selectedNames.delete(name);
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.selectedNames.size === 0) {
      this.errorKey = 'DISCOUNT_FORM.TYPES_REQUIRED';
      this.errorArgs = null;
      this.validationMessages = [];
      return;
    }
    this.loading = true;
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];

    const payload: DiscountRequest = {
      discount: {
        id: this.id ?? undefined,
        discountPercentage: Number(this.form.value.discountPercentage),
        expiryDate: this.form.value.expiryDate,
        discountCode: this.form.value.discountCode
      },
      applicableTicketTypesNames: Array.from(this.selectedNames)
    };

    const obs = this.isEdit && this.id ? this.api.update(this.id, payload) : this.api.create(payload);
    obs.pipe(
      catchError((err) => {
        const mapped = this.error.handleError(err);
        this.errorKey = mapped.code;
        this.errorArgs = mapped.args;
        this.validationMessages = this.error.getValidationMessages(mapped.code, mapped.args);
        return of(null);
      })
    ).subscribe(res => {
      this.loading = false;
      if (res) this.router.navigate(['/discounts']);
    });
  }

  delete() {
    if (!this.isEdit || !this.id) return;
    const ok = confirm(this.translate.instant('DISCOUNT_FORM.CONFIRM_DELETE'));
    if (!ok) return;
    this.loading = true;
    this.api.delete(this.id).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/discounts']);
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
