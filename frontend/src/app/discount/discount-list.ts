import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DiscountApiService, Discount } from './discount.service';
import { AuthService } from '../auth/auth.service';
import { catchError, of } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';
import { checkAdminRole, handleApiError } from '../shared/utils';

@Component({
  selector: 'app-discount-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslatePipe],
  templateUrl: './discount-list.html',
  styleUrls: ['./discount-list.css']
})
export class DiscountList implements OnInit {
  discounts: Discount[] = [];
  isAdmin = false;
  loading = false;
  errorKey: string | null = null;
  errorArgs: any = null;
  validationMessages: string[] = [];
  appliedTypesMap: Record<number, string[]> = {};
  loadingTypes: Record<number, boolean> = {};
  deleting: Record<number, boolean> = {};

  constructor(private api: DiscountApiService, private auth: AuthService, private error: ErrorService) {}

  ngOnInit(): void {
    checkAdminRole(this.auth).subscribe(v => this.isAdmin = v);
    this.load();
  }

  load() {
    this.loading = true;
    this.clearError();
    this.discounts = [];
    this.appliedTypesMap = {};
    this.loadingTypes = {};
    this.api.findAll().pipe(
      catchError((err) => {
        this.setError(err);
        return of([]);
      })
    ).subscribe(list => {
      this.discounts = list;
      this.loading = false;
      this.discounts.forEach(d => this.loadAppliedTypes(d));
    });
  }

  private loadAppliedTypes(d: Discount) {
    if (!d.id) return;
    this.loadingTypes[d.id] = true;
    this.api.getTicketTypesByDiscount(d.id).pipe(
      catchError(() => of([]))
    ).subscribe((arr: any[]) => {
      this.appliedTypesMap[d.id!] = (arr || []).map(x => x.typeName);
      this.loadingTypes[d.id!] = false;
    });
  }

  onDelete(d: Discount) {
    if (!d.id) return;
    if (!window.confirm(`¿Eliminar el descuento "${d.discountCode}"?`)) return;

    this.clearError();
    this.deleting[d.id] = true;

    this.api.delete(d.id).subscribe({
      next: () => {
        this.discounts = this.discounts.filter(x => x.id !== d.id);
        delete this.deleting[d.id!];
      },
      error: (err) => {
        this.setError(err);
        this.deleting[d.id!] = false;
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
