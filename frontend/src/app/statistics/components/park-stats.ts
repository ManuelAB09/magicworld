import { Component, Input, OnInit, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import {
  StatisticsService,
  TicketSalesDTO,
  MonthlySalesDTO,
  AttractionPerformanceDTO
} from '../statistics.service';
import { CurrencyService } from '../../shared/currency.service';
import { Subscription } from 'rxjs';

type ParkSubView = 'sales' | 'seasonality' | 'attractions';

@Component({
  selector: 'app-park-stats',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './park-stats.html',
  styleUrls: ['./park-stats.css']
})
export class ParkStatsComponent implements OnInit, OnChanges, OnDestroy {
  @Input() dateFrom = '';
  @Input() dateTo = '';

  subView: ParkSubView = 'sales';
  loading = false;

  ticketSales: TicketSalesDTO | null = null;
  monthlySales: MonthlySalesDTO[] = [];
  attractionPerformance: AttractionPerformanceDTO[] = [];
  ticketSalesCurrency: string | null = null;
  monthlySalesCurrency: string | null = null;

  selectedYear = new Date().getFullYear();
  maxRevenue = 0;
  private langChangeSub?: Subscription;

  constructor(
    private statsService: StatisticsService,
    private currency: CurrencyService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.langChangeSub = this.translate.onLangChange.subscribe(() => {
      this.refreshCurrencyDerivedValues();
    });
  }

  ngOnDestroy(): void {
    this.langChangeSub?.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['dateFrom'] || changes['dateTo']) && this.dateFrom && this.dateTo) {
      this.loadData();
    }
  }

  setSubView(view: ParkSubView): void {
    this.subView = view;
    this.loadData();
  }

  loadData(): void {
    if (!this.dateFrom || !this.dateTo) return;
    this.loading = true;

    switch (this.subView) {
      case 'sales':
        this.statsService.getTicketSales(this.dateFrom, this.dateTo).subscribe({
          next: data => {
            this.ticketSales = data;
            this.ticketSalesCurrency = data.currency;
            this.loading = false;
          },
          error: () => this.loading = false
        });
        break;
      case 'seasonality':
        this.statsService.getSeasonality(this.selectedYear).subscribe({
          next: data => {
            this.monthlySales = data;
            this.monthlySalesCurrency = this.currency.getCurrencyCode();
            this.maxRevenue = this.getMaxRevenueConverted();
            this.loading = false;
          },
          error: () => this.loading = false
        });
        break;
      case 'attractions':
        this.statsService.getAttractionPerformance(this.dateFrom, this.dateTo).subscribe({
          next: data => { this.attractionPerformance = data; this.loading = false; },
          error: () => this.loading = false
        });
        break;
    }
  }

  onYearChange(): void {
    this.loadData();
  }

  getCurrencySymbol(): string {
    return this.currency.getCurrencySymbol();
  }

  getBarHeight(revenue: number): number {
    const convertedRevenue = this.getConvertedMonthlyRevenue(revenue);
    return this.maxRevenue > 0 ? (convertedRevenue / this.maxRevenue) * 100 : 0;
  }

  getConvertedTicketRevenue(): number {
    if (!this.ticketSales) return 0;
    return this.currency.convertFromCurrency(
      this.ticketSales.totalRevenue,
      this.resolveTicketSalesCurrency()
    );
  }

  getAverageTicketPrice(): number {
    if (!this.ticketSales || this.ticketSales.totalTicketsSold <= 0) return 0;
    return this.getConvertedTicketRevenue() / this.ticketSales.totalTicketsSold;
  }

  getConvertedMonthlyRevenue(revenue: number): number {
    return this.currency.convertFromCurrency(revenue, this.resolveMonthlySalesCurrency());
  }

  getQueueLevel(avg: number): string {
    if (avg >= 50) return 'high';
    if (avg >= 25) return 'medium';
    return 'low';
  }

  private resolveTicketSalesCurrency(): string {
    return this.ticketSalesCurrency || this.currency.getCurrencyCode();
  }

  private resolveMonthlySalesCurrency(): string {
    return this.monthlySalesCurrency || this.currency.getCurrencyCode();
  }

  private getMaxRevenueConverted(): number {
    if (!this.monthlySales.length) return 1;
    return Math.max(
      ...this.monthlySales.map(m => this.getConvertedMonthlyRevenue(m.revenue)),
      1
    );
  }

  private refreshCurrencyDerivedValues(): void {
    if (this.subView === 'seasonality' && this.monthlySales.length) {
      this.maxRevenue = this.getMaxRevenueConverted();
    }
  }
}

