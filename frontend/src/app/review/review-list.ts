import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService, ReviewDTO, ReviewRequest } from './review.service';
import { ProfileService, PurchaseDTO } from '../profile/profile.service';
import { AuthService } from '../auth/auth.service';
import { TranslatePipe } from '@ngx-translate/core';
import { ErrorService } from '../error/error-service';
import { handleApiError } from '../shared/utils';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-review-list',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './review-list.html',
  styleUrls: ['./review-list.css']
})
export class ReviewListComponent implements OnInit {
  reviews: ReviewDTO[] = [];
  loading = false;
  errorKey: string | null = null;
  errorArgs: any = null;
  validationMessages: string[] = [];

  // Pagination
  currentPage = 0;
  pageSize = 5;
  pageSizeOptions = [5, 10, 20, 50];
  totalPages = 0;
  totalElements = 0;

  // User state
  isAuthenticated = false;
  availablePurchases: PurchaseDTO[] = [];

  // Form state
  showForm = false;
  selectedPurchaseId: number | null = null;
  selectedVisitDate: string = '';
  formStars = 5;
  formDescription = '';
  submitting = false;

  constructor(
    private reviewService: ReviewService,
    private profileService: ProfileService,
    private auth: AuthService,
    private error: ErrorService
  ) {}

  ngOnInit(): void {
    this.checkAuthAndLoadReviews();
  }

  private checkAuthAndLoadReviews(): void {
    this.auth.checkRoleSecure().pipe(
      catchError(() => of(null))
    ).subscribe(role => {
      this.isAuthenticated = role !== null;
      this.loadReviews();
      if (this.isAuthenticated) {
        this.loadAvailablePurchases();
      }
    });
  }

  private loadAvailablePurchases(): void {
    this.reviewService.getAvailablePurchases().pipe(
      catchError(() => of([]))
    ).subscribe((purchaseIds: number[]) => {
      if (purchaseIds.length > 0) {
        this.profileService.getMyPurchases().pipe(
          catchError(() => of([]))
        ).subscribe((purchases: PurchaseDTO[]) => {
          this.availablePurchases = purchases.filter(p => purchaseIds.includes(p.id));
        });
      }
    });
  }

  loadReviews(): void {
    this.loading = true;
    this.clearError();
    this.reviewService.findAllPaginated(this.currentPage, this.pageSize).pipe(
      catchError(err => {
        this.setError(err);
        return of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: this.pageSize });
      })
    ).subscribe(page => {
      this.reviews = page.content;
      this.totalElements = page.totalElements;
      this.totalPages = page.totalPages;
      this.currentPage = page.number;
      this.loading = false;
    });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadReviews();
    }
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadReviews();
  }

  openCreateForm(): void {
    this.showForm = true;
    this.selectedPurchaseId = null;
    this.selectedVisitDate = '';
    this.formStars = 5;
    this.formDescription = '';
    this.clearError();
  }

  closeForm(): void {
    this.showForm = false;
    this.clearError();
  }

  onPurchaseSelect(purchaseId: number): void {
    this.selectedPurchaseId = purchaseId;
    const purchase = this.availablePurchases.find(p => p.id === purchaseId);
    if (purchase && purchase.lines.length > 0) {
      this.selectedVisitDate = purchase.lines[0].validDate;
    }
  }

  setStars(stars: number): void {
    this.formStars = stars;
  }

  submitReview(): void {
    if (!this.formDescription.trim() || !this.selectedPurchaseId || !this.selectedVisitDate) {
      return;
    }

    this.submitting = true;
    this.clearError();

    const request: ReviewRequest = {
      purchaseId: this.selectedPurchaseId,
      visitDate: this.selectedVisitDate,
      stars: this.formStars,
      description: this.formDescription.trim()
    };

    this.reviewService.create(request).subscribe({
      next: () => {
        this.submitting = false;
        this.showForm = false;
        this.currentPage = 0;
        this.loadReviews();
        this.loadAvailablePurchases();
      },
      error: (err) => {
        this.submitting = false;
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

  getStarsArray(stars: number): number[] {
    return Array(Math.round(stars)).fill(0);
  }

  getEmptyStarsArray(stars: number): number[] {
    return Array(5 - Math.round(stars)).fill(0);
  }
}
