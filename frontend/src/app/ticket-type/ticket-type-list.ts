import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { TicketType, TicketTypeApiService } from './ticket-type.service';
import { AuthService } from '../auth/auth.service';
import { ErrorService } from '../error/error-service';
import { catchError, of } from 'rxjs';
import { getImageUrl, checkAdminRole, handleApiError } from '../shared/utils';
import { CurrencyService } from '../shared/currency.service';

@Component({
  selector: 'app-ticket-type-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslatePipe],
  templateUrl: './ticket-type-list.html',
  styleUrls: ['./ticket-type-list.css']
})
export class TicketTypeList implements OnInit {
  items: TicketType[] = [];
  loading = false;
  isAdmin = false;
  errorKey: string | null = null;
  errorArgs: any = null;
  validationMessages: string[] = [];

  constructor(
    private api: TicketTypeApiService,
    private auth: AuthService,
    private error: ErrorService,
    private router: Router,
    private translate: TranslateService,
    public currency: CurrencyService
  ) {}

  ngOnInit(): void {
    checkAdminRole(this.auth).subscribe(v => this.isAdmin = v);
    this.load();
  }

  load() {
    this.loading = true;
    this.clearError();
    this.api.findAll().pipe(
      catchError(err => {
        this.setError(err);
        return of([]);
      })
    ).subscribe(list => {
      this.items = list;
      this.loading = false;
    });
  }

  getImageUrl(url: string | null | undefined): string | null {
    return getImageUrl(url);
  }

  delete(id: number) {
    if (!confirm(this.translate.instant('TICKET_TYPE_FORM.CONFIRM_DELETE'))) return;

    this.loading = true;
    this.api.delete(id).subscribe({
      next: () => {
        this.loading = false;
        this.items = this.items.filter(tt => tt.id !== id);
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
