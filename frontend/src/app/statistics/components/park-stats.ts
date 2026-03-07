import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
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

type ParkSubView = 'sales' | 'seasonality' | 'attractions';

@Component({
  selector: 'app-park-stats',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './park-stats.html',
  styleUrls: ['./park-stats.css']
})
export class ParkStatsComponent implements OnInit, OnChanges {
  @Input() dateFrom = '';
  @Input() dateTo = '';

  subView: ParkSubView = 'sales';
  loading = false;

  ticketSales: TicketSalesDTO | null = null;
  monthlySales: MonthlySalesDTO[] = [];
  attractionPerformance: AttractionPerformanceDTO[] = [];

  selectedYear = new Date().getFullYear();
  maxRevenue = 0;

  constructor(
    private statsService: StatisticsService,
    private currency: CurrencyService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadData();
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
          next: data => { this.ticketSales = data; this.loading = false; },
          error: () => this.loading = false
        });
        break;
      case 'seasonality':
        this.statsService.getSeasonality(this.selectedYear).subscribe({
          next: data => {
            this.monthlySales = data;
            this.maxRevenue = Math.max(...data.map(m => m.revenue), 1);
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
    return this.maxRevenue > 0 ? (revenue / this.maxRevenue) * 100 : 0;
  }

  getQueueLevel(avg: number): string {
    if (avg >= 50) return 'high';
    if (avg >= 25) return 'medium';
    return 'low';
  }
}

