import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { TicketType, TicketTypeApiService } from './ticket-type.service';
import { AuthService, Role } from '../auth/auth-service';
import { ErrorService } from '../error/error-service';
import { catchError, map, of } from 'rxjs';

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
    private error: ErrorService
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

  load() {
    this.loading = true;
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];
    this.api.findAll().pipe(
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
}

