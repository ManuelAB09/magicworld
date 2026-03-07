import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { CheckoutService, ParkClosureDay, SeasonalPricing } from '../checkout/services/checkout.service';
import { ErrorService } from '../error/error-service';

@Component({
  selector: 'app-park-config',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './park-config.html',
  styleUrls: ['./park-config.css']
})
export class ParkConfigComponent implements OnInit {
  // Closure days
  closureDays: ParkClosureDay[] = [];
  newClosureDate = '';
  newClosureReason = '';
  closureLoading = false;
  closureError: string | null = null;
  closureSuccess: string | null = null;

  // Seasonal pricing
  seasonalPricings: SeasonalPricing[] = [];
  newPricingName = '';
  newPricingStartDate = '';
  newPricingEndDate = '';
  newPricingMultiplier = 1.25;
  newPricingWeekdays = true;
  newPricingWeekends = true;
  newPricingAllYear = false;
  pricingLoading = false;
  pricingError: string | null = null;
  pricingSuccess: string | null = null;

  // Tab
  activeTab: 'closures' | 'pricing' = 'closures';

  // Min date for closure (2 months from now)
  minClosureDate = '';

  constructor(
    private checkoutService: CheckoutService,
    private errorService: ErrorService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    const minDate = new Date();
    minDate.setMonth(minDate.getMonth() + 2);
    this.minClosureDate = this.formatDate(minDate);
    this.loadClosureDays();
    this.loadSeasonalPricing();
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  switchTab(tab: 'closures' | 'pricing'): void {
    this.activeTab = tab;
    this.clearMessages();
  }

  private clearMessages(): void {
    this.closureError = null;
    this.closureSuccess = null;
    this.pricingError = null;
    this.pricingSuccess = null;
  }

  // === CLOSURE DAYS ===

  loadClosureDays(): void {
    this.closureLoading = true;
    this.checkoutService.getAllClosureDays().subscribe({
      next: (days) => {
        this.closureDays = days.sort((a, b) => a.closureDate.localeCompare(b.closureDate));
        this.closureLoading = false;
      },
      error: (err) => {
        const { code, args } = this.errorService.handleError(err);
        this.closureError = this.translate.instant(code, args);
        this.closureLoading = false;
      }
    });
  }

  addClosureDay(): void {
    if (!this.newClosureDate || !this.newClosureReason.trim()) return;
    this.clearMessages();
    this.checkoutService.createClosureDay({
      closureDate: this.newClosureDate,
      reason: this.newClosureReason.trim()
    }).subscribe({
      next: () => {
        this.closureSuccess = 'PARK_CONFIG.CLOSURE_ADDED';
        this.newClosureDate = '';
        this.newClosureReason = '';
        this.loadClosureDays();
      },
      error: (err) => {
        const { code, args } = this.errorService.handleError(err);
        this.closureError = this.translate.instant(code, args);
      }
    });
  }

  deleteClosureDay(id: number): void {
    this.clearMessages();
    this.checkoutService.deleteClosureDay(id).subscribe({
      next: () => {
        this.closureSuccess = 'PARK_CONFIG.CLOSURE_DELETED';
        this.loadClosureDays();
      },
      error: (err) => {
        const { code, args } = this.errorService.handleError(err);
        this.closureError = this.translate.instant(code, args);
      }
    });
  }

  // === SEASONAL PRICING ===

  onAllYearChange(): void {
    if (this.newPricingAllYear) {
      const year = new Date().getFullYear();
      this.newPricingStartDate = `${year}-01-01`;
      this.newPricingEndDate = `${year}-12-31`;
    } else {
      this.newPricingStartDate = '';
      this.newPricingEndDate = '';
    }
  }

  loadSeasonalPricing(): void {
    this.pricingLoading = true;
    this.checkoutService.getAllSeasonalPricing().subscribe({
      next: (pricings) => {
        this.seasonalPricings = pricings;
        this.pricingLoading = false;
      },
      error: (err) => {
        const { code, args } = this.errorService.handleError(err);
        this.pricingError = this.translate.instant(code, args);
        this.pricingLoading = false;
      }
    });
  }

  addSeasonalPricing(): void {
    if (!this.newPricingName.trim() || this.newPricingMultiplier < 1) return;

    let startDate = this.newPricingStartDate;
    let endDate = this.newPricingEndDate;

    if (this.newPricingAllYear) {
      const year = new Date().getFullYear();
      startDate = `${year}-01-01`;
      endDate = `${year}-12-31`;
    }

    if (!startDate || !endDate) return;

    this.clearMessages();
    this.checkoutService.createSeasonalPricing({
      name: this.newPricingName.trim(),
      startDate: startDate,
      endDate: endDate,
      multiplier: this.newPricingMultiplier,
      applyOnWeekdays: this.newPricingWeekdays,
      applyOnWeekends: this.newPricingWeekends
    }).subscribe({
      next: () => {
        this.pricingSuccess = 'PARK_CONFIG.PRICING_ADDED';
        this.newPricingName = '';
        this.newPricingStartDate = '';
        this.newPricingEndDate = '';
        this.newPricingMultiplier = 1.25;
        this.newPricingWeekdays = true;
        this.newPricingWeekends = true;
        this.newPricingAllYear = false;
        this.loadSeasonalPricing();
      },
      error: (err) => {
        const { code, args } = this.errorService.handleError(err);
        this.pricingError = this.translate.instant(code, args);
      }
    });
  }

  deleteSeasonalPricing(id: number): void {
    this.clearMessages();
    this.checkoutService.deleteSeasonalPricing(id).subscribe({
      next: () => {
        this.pricingSuccess = 'PARK_CONFIG.PRICING_DELETED';
        this.loadSeasonalPricing();
      },
      error: (err) => {
        const { code, args } = this.errorService.handleError(err);
        this.pricingError = this.translate.instant(code, args);
      }
    });
  }
}

