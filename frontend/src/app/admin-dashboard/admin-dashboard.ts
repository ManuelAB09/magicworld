import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { MonitoringService, DashboardSnapshot, AlertDTO } from './monitoring.service';
import { DashboardStatsComponent } from './components/dashboard-stats';
import { AttractionQueueListComponent } from './components/attraction-queue-list';
import { AlertListComponent } from './components/alert-list';
import { SimulatorControlComponent } from './components/simulator-control';
import { HeatmapOverlayComponent } from './components/heatmap-overlay';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    TranslatePipe,
    DashboardStatsComponent,
    AttractionQueueListComponent,
    AlertListComponent,
    SimulatorControlComponent,
    HeatmapOverlayComponent
  ],
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  snapshot: DashboardSnapshot | null = null;
  loading = true;
  error = false;

  private subscriptions: Subscription[] = [];

  constructor(private monitoringService: MonitoringService) {}

  ngOnInit(): void {
    this.loadDashboard();
    this.monitoringService.connectWebSocket();
    this.subscribeToUpdates();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.monitoringService.disconnectWebSocket();
  }

  private loadDashboard(): void {
    this.monitoringService.getDashboard().subscribe({
      next: (data) => {
        this.snapshot = data;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  private subscribeToUpdates(): void {
    this.subscriptions.push(
      this.monitoringService.dashboard$.subscribe(snapshot => {
        this.snapshot = snapshot;
      }),
      this.monitoringService.alerts$.subscribe(alert => {
        if (this.snapshot && alert.isActive) {
          this.snapshot.activeAlerts = [alert, ...this.snapshot.activeAlerts];
        }
      })
    );
  }

  onAlertResolved(event: {alertId: number, optionId: string}): void {
    if (this.snapshot) {
      this.snapshot.activeAlerts = this.snapshot.activeAlerts.filter(a => a.id !== event.alertId);
    }
  }

  refreshDashboard(): void {
    this.loadDashboard();
  }
}
