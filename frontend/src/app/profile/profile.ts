import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProfileService, PurchaseDTO, UpdateProfileRequest } from './profile.service';
import { AuthService, UserProfile } from '../auth/auth.service';
import { TranslatePipe } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';
import { handleApiError } from '../shared/utils';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  purchases: PurchaseDTO[] = [];

  loading = true;
  loadingPurchases = false;
  saving = false;
  deleting = false;

  errorKey: string | null = null;
  errorArgs: any = null;
  validationMessages: string[] = [];
  successMessage: string | null = null;

  // Edit form
  showEditForm = false;
  editFirstname = '';
  editLastname = '';
  editEmail = '';
  editPassword = '';

  // Tabs
  activeTab: 'profile' | 'purchases' = 'profile';

  constructor(
    private profileService: ProfileService,
    private auth: AuthService,
    private error: ErrorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  private loadProfile(): void {
    this.loading = true;
    this.auth.getProfile().pipe(
      catchError(err => {
        this.setError(err);
        return of(null);
      })
    ).subscribe(profile => {
      this.profile = profile;
      this.loading = false;
    });
  }

  switchTab(tab: 'profile' | 'purchases'): void {
    this.activeTab = tab;
    this.clearMessages();
    if (tab === 'purchases' && this.purchases.length === 0) {
      this.loadPurchases();
    }
  }

  private loadPurchases(): void {
    this.loadingPurchases = true;
    this.profileService.getMyPurchases().pipe(
      catchError(err => {
        this.setError(err);
        return of([]);
      })
    ).subscribe(purchases => {
      this.purchases = purchases;
      this.loadingPurchases = false;
    });
  }

  openEditForm(): void {
    if (this.profile) {
      this.editFirstname = this.profile.firstname;
      this.editLastname = this.profile.lastname;
      this.editEmail = this.profile.email;
      this.editPassword = '';
      this.showEditForm = true;
      this.clearMessages();
    }
  }

  closeEditForm(): void {
    this.showEditForm = false;
    this.clearMessages();
  }

  saveProfile(): void {
    if (!this.editFirstname.trim() || !this.editLastname.trim() || !this.editEmail.trim()) {
      return;
    }

    this.saving = true;
    this.clearMessages();

    const request: UpdateProfileRequest = {
      firstname: this.editFirstname.trim(),
      lastname: this.editLastname.trim(),
      email: this.editEmail.trim(),
      password: this.editPassword.trim() || undefined
    };

    this.profileService.updateProfile(request).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.saving = false;
        this.showEditForm = false;
        this.successMessage = 'PROFILE.UPDATE_SUCCESS';
      },
      error: (err) => {
        this.saving = false;
        this.setError(err);
      }
    });
  }

  confirmDelete(): void {
    this.clearMessages();
    if (!confirm('¿Estás seguro de que quieres eliminar tu cuenta? Esta acción no se puede deshacer.')) {
      return;
    }

    this.deleting = true;
    this.profileService.deleteProfile().subscribe({
      next: () => {
        this.auth.notifyAuthChanged(false);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.deleting = false;
        this.setError(err);
      }
    });
  }

  private clearMessages(): void {
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];
    this.successMessage = null;
  }

  private setError(err: any): void {
    const state = handleApiError(err, this.error);
    this.errorKey = state.errorKey;
    this.errorArgs = state.errorArgs;
    this.validationMessages = state.validationMessages;
  }

  getTotalCost(purchase: PurchaseDTO): number {
    return purchase.lines.reduce((sum, line) => sum + line.totalCost, 0);
  }
}
