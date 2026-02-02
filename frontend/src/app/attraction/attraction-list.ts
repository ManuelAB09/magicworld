import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Attraction, AttractionApiService } from './attraction.service';
import { AuthService } from '../auth/auth.service';
import { ErrorService } from '../error/error-service';
import { catchError, of } from 'rxjs';
import { getImageUrl, checkAdminRole, handleApiError } from '../shared/utils';

@Component({
  selector: 'app-attraction-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslatePipe, FormsModule],
  templateUrl: './attraction-list.html',
  styleUrls: ['./attraction-list.css']
})
export class AttractionList implements OnInit {
  items: Attraction[] = [];
  loading = false;
  isAdmin = false;
  errorKey: string | null = null;
  errorArgs: any = null;
  validationMessages: string[] = [];
  filters = { minHeight: 0, minWeight: 0, minAge: 0 };

  constructor(
    private api: AttractionApiService,
    private auth: AuthService,
    private error: ErrorService,
    private router: Router,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    checkAdminRole(this.auth).subscribe(v => this.isAdmin = v);
    this.load();
  }

  load(filters?: { minHeight?: number | null; minWeight?: number | null; minAge?: number | null }) {
    this.loading = true;
    this.clearError();
    const apiFilters = this.buildApiFilters(filters);

    this.api.findAll(Object.keys(apiFilters).length ? apiFilters : undefined).pipe(
      catchError(err => {
        this.setError(err);
        return of([]);
      })
    ).subscribe(list => {
      this.items = list;
      this.loading = false;
    });
  }

  private buildApiFilters(filters?: { minHeight?: number | null; minWeight?: number | null; minAge?: number | null }): any {
    const apiFilters: any = {};
    if (filters) {
      if (filters.minHeight != null) apiFilters.minHeight = filters.minHeight;
      if (filters.minWeight != null) apiFilters.minWeight = filters.minWeight;
      if (filters.minAge != null) apiFilters.minAge = filters.minAge;
    }
    return apiFilters;
  }

  applyFilters() {
    this.validationMessages = [];
    if (!this.validateFilters()) return;
    this.load(this.buildFilterParams());
  }

  private validateFilters(): boolean {
    const fields = [this.filters.minHeight, this.filters.minWeight, this.filters.minAge];
    if (fields.some(v => v != null && v < 0)) {
      this.validationMessages.push(this.translate.instant('ATTRACTIONS.FILTER.INVALID_NEGATIVE'));
      return false;
    }
    return true;
  }

  private buildFilterParams(): any {
    const f: any = {};
    if (this.filters.minHeight != null) f.minHeight = this.filters.minHeight;
    if (this.filters.minWeight != null) f.minWeight = this.filters.minWeight;
    if (this.filters.minAge != null) f.minAge = this.filters.minAge;
    return f;
  }

  clearFilters() {
    this.filters = { minHeight: 0, minWeight: 0, minAge: 0 };
    this.load();
  }

  getImageUrl(url: string | null | undefined): string | null {
    return getImageUrl(url);
  }

  delete(id: number) {
    if (!confirm(this.translate.instant('ATTRACTION_FORM.CONFIRM_DELETE'))) return;

    this.loading = true;
    this.api.delete(id).subscribe({
      next: () => {
        this.loading = false;
        this.items = this.items.filter(a => a.id !== id);
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
