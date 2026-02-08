import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { DashboardSnapshot } from '../monitoring.service';

@Component({
  selector: 'app-dashboard-stats',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './dashboard-stats.html',
  styleUrls: ['./dashboard-stats.css']
})
export class DashboardStatsComponent {
  @Input() snapshot!: DashboardSnapshot;
}
