import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DiscountApiService, Discount } from './discount.service';
import { AuthService, Role } from '../auth/auth-service';
import { catchError, map, of } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';

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

  constructor(private api: DiscountApiService, private auth: AuthService, private error: ErrorService) {}

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

  load() {
    this.loading = true;
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];
    this.discounts = [];
    this.api.findAll().pipe(
      catchError((err) => {
        const mapped = this.error.handleError(err);
        this.errorKey = mapped.code;
        this.errorArgs = mapped.args;
        this.validationMessages = this.error.getValidationMessages(mapped.code, mapped.args);
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
}
