import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { MonitoringService, SimulatorStatus } from '../monitoring.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-simulator-control',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './simulator-control.html',
  styleUrls: ['./simulator-control.css']
})
export class SimulatorControlComponent implements OnInit, OnDestroy {
  status: SimulatorStatus | null = null;
  loading = false;
  private refreshSub?: Subscription;

  constructor(private monitoringService: MonitoringService) {}

  ngOnInit(): void {
    this.loadStatus();
    this.refreshSub = interval(2000).subscribe(() => {
      if (this.status?.running) {
        this.loadStatus();
      }
    });
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  loadStatus(): void {
    this.monitoringService.getSimulatorStatus().subscribe(s => this.status = s);
  }

  start(): void {
    this.loading = true;
    this.monitoringService.startSimulator().subscribe({
      next: (s) => {
        this.status = s;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  stop(): void {
    this.loading = true;
    this.monitoringService.stopSimulator().subscribe({
      next: (s) => {
        this.status = s;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }
}
