import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Attraction, AttractionApiService } from './attraction.service';
import { AuthService, Role } from '../auth/auth.service';
import { ErrorService } from '../error/error-service';
import { catchError, map, of } from 'rxjs';
import { getBackendBaseUrl } from '../config/backend';

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
  filters: { minHeight?: number | null; minWeight?: number | null; minAge?: number | null } = { minHeight: 0, minWeight: 0, minAge: 0 };

  private apiBase = getBackendBaseUrl();

  constructor(
    private api: AttractionApiService,
    private auth: AuthService,
    private error: ErrorService,
    private router: Router,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.checkAdmin();
    this.load();
  }

  private checkAdmin() {
    this.auth.checkRoleSecure().pipe(
      map(role => role === Role.ADMIN),
      catchError(() => of(false))
    ).subscribe(v => this.isAdmin = v);
  }

  load(filters?: { minHeight?: number | null; minWeight?: number | null; minAge?: number | null }) {
    this.loading = true;
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];
    const apiFilters: any = {};
    if (filters) {
      if (filters.minHeight != null) apiFilters.minHeight = filters.minHeight;
      if (filters.minWeight != null) apiFilters.minWeight = filters.minWeight;
      if (filters.minAge != null) apiFilters.minAge = filters.minAge;
    }

    this.api.findAll(Object.keys(apiFilters).length ? apiFilters : undefined).pipe(
      catchError(err => {
        const mapped = this.error.handleError(err);
        this.errorKey = mapped.code;
        this.errorArgs = mapped.args;
        this.validationMessages = this.error.getValidationMessages(mapped.code, mapped.args);
        return of([]);
      })
    ).subscribe(list => {
      this.items = list;
      this.loading = false;
    });
  }

  applyFilters() {
    this.validationMessages = [];
    this.validateFilters();
    if (this.validationMessages.length) return;

    const f = this.buildFilterParams();
    this.load(f);
  }

  private validateFilters(): void {
    const fields = [this.filters.minHeight, this.filters.minWeight, this.filters.minAge];
    if (fields.some(v => v != null && v < 0)) {
      this.validationMessages.push(this.translate.instant('ATTRACTIONS.FILTER.INVALID_NEGATIVE'));
    }
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
    if (!url) return null;
    if (url.startsWith('http')) return url;
    return this.apiBase + url;
  }

  delete(id: number) {
    const ok = confirm(this.translate.instant('ATTRACTION_FORM.CONFIRM_DELETE'));
    if (!ok) return;

    this.loading = true;
    this.api.delete(id).subscribe({
      next: () => {
        this.loading = false;
        this.items = this.items.filter(a => a.id !== id);
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
