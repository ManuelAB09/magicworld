import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { TranslatePipe } from '@ngx-translate/core';
import { getBackendBaseUrl } from '../../config/backend';
import { interval, Subscription } from 'rxjs';

interface MetricsPoint {
  timestamp: string;
  visitors: number;
  queueSize: number;
  waitTimeMinutes: number;
}

interface MetricsHistory {
  startTime: string;
  endTime: string;
  dataPoints: MetricsPoint[];
}

@Component({
  selector: 'app-metrics-history',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './metrics-history.html',
  styleUrls: ['./metrics-history.css']
})
export class MetricsHistoryComponent implements OnInit, OnDestroy {
  Math = Math;
  private baseUrl = `${getBackendBaseUrl()}/api/v1/monitoring/metrics`;

  selectedRange = '1h';
  autoRefresh = true;
  loading = false;
  metrics: MetricsHistory | null = null;

  maxVisitors = 0;
  maxQueue = 0;
  avgQueue = 0;
  avgWaitTime = 0;

  visitorsLinePath = '';
  visitorsAreaPath = '';
  queueLinePath = '';
  queueAreaPath = '';
  timeLabels: string[] = [];
  recentPoints: MetricsPoint[] = [];


  private refreshSub?: Subscription;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadMetrics();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  setRange(range: string): void {
    this.selectedRange = range;
    this.loadMetrics();
  }

  toggleAutoRefresh(): void {
    if (this.autoRefresh) {
      this.startAutoRefresh();
    } else {
      this.refreshSub?.unsubscribe();
    }
  }

  private startAutoRefresh(): void {
    this.refreshSub?.unsubscribe();
    if (this.autoRefresh) {
      this.refreshSub = interval(5000).subscribe(() => this.loadMetrics());
    }
  }

  loadMetrics(): void {
    this.loading = true;
    const { start, end } = this.getDateRange();

    this.http.get<MetricsHistory>(`${this.baseUrl}/global?start=${start}&end=${end}`, { withCredentials: true })
      .subscribe({
        next: (data) => {
          this.metrics = data;
          this.processMetrics();
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  private getDateRange(): { start: string; end: string } {
    const now = new Date();
    const hours = { '1h': 1, '3h': 3, '6h': 6, '24h': 24 }[this.selectedRange] || 1;
    const start = new Date(now.getTime() - hours * 60 * 60 * 1000);
    return {
      start: start.toISOString(),
      end: now.toISOString()
    };
  }

  private processMetrics(): void {
    if (!this.metrics?.dataPoints?.length) {
      this.resetChartData();
      return;
    }

    const points = this.metrics.dataPoints;

    this.maxVisitors = Math.max(...points.map(p => p.visitors), 1);
    this.maxQueue = Math.max(...points.map(p => p.queueSize), 1);
    this.avgQueue = Math.round(points.reduce((sum, p) => sum + p.queueSize, 0) / points.length);
    this.avgWaitTime = Math.round(points.reduce((sum, p) => sum + p.waitTimeMinutes, 0) / points.length);

    this.buildChartPaths(points);
    this.buildTimeLabels(points);
    this.recentPoints = points.slice(-10).reverse();
  }

  private resetChartData(): void {
    this.maxVisitors = 0;
    this.maxQueue = 0;
    this.avgQueue = 0;
    this.avgWaitTime = 0;
    this.visitorsLinePath = '';
    this.visitorsAreaPath = '';
    this.queueLinePath = '';
    this.queueAreaPath = '';
    this.timeLabels = [];
    this.recentPoints = [];
  }

  private buildChartPaths(points: MetricsPoint[]): void {
    if (points.length < 2) {
      this.resetChartData();
      return;
    }

    const maxY = Math.max(this.maxVisitors, this.maxQueue, 1);

    const visitorsPoints = points.map((p, i) => ({
      x: (i / (points.length - 1)) * 100,
      y: 50 - (p.visitors / maxY) * 48
    }));

    const queuePoints = points.map((p, i) => ({
      x: (i / (points.length - 1)) * 100,
      y: 50 - (p.queueSize / maxY) * 48
    }));

    this.visitorsLinePath = this.buildLinePath(visitorsPoints);
    this.visitorsAreaPath = this.buildAreaPath(visitorsPoints);
    this.queueLinePath = this.buildLinePath(queuePoints);
    this.queueAreaPath = this.buildAreaPath(queuePoints);
  }

  private buildLinePath(points: { x: number; y: number }[]): string {
    return points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
  }

  private buildAreaPath(points: { x: number; y: number }[]): string {
    const line = this.buildLinePath(points);
    return `${line} L ${points[points.length - 1].x} 50 L ${points[0].x} 50 Z`;
  }

  private buildTimeLabels(points: MetricsPoint[]): void {
    if (points.length < 2) {
      this.timeLabels = [];
      return;
    }

    const indices = [0, Math.floor(points.length / 2), points.length - 1];
    this.timeLabels = indices.map(i => this.formatTime(points[i].timestamp));
  }

  exportCsv(): void {
    const { start, end } = this.getDateRange();
    this.http.get(`${this.baseUrl}/export/csv?start=${start}&end=${end}`, {
      withCredentials: true,
      responseType: 'blob'
    }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `metricas_${new Date().toISOString().slice(0,10)}.csv`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  formatTime(timestamp: string): string {
    return new Date(timestamp).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  }
}
