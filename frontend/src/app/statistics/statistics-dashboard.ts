import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { EmployeeStatsComponent } from './components/employee-stats';
import { ParkStatsComponent } from './components/park-stats';
import { WorkLogComponent } from './components/work-log';

type ActiveTab = 'employees' | 'park' | 'worklog';

@Component({
  selector: 'app-statistics-dashboard',
  standalone: true,
  imports: [
    CommonModule, FormsModule, TranslatePipe,
    EmployeeStatsComponent, ParkStatsComponent, WorkLogComponent
  ],
  templateUrl: './statistics-dashboard.html',
  styleUrls: ['./statistics-dashboard.css']
})
export class StatisticsDashboardComponent implements OnInit {
  activeTab: ActiveTab = 'employees';
  dateFrom = '';
  dateTo = '';

  constructor() {}

  ngOnInit(): void {
    const today = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);

    this.dateFrom = this.formatDate(monday);
    this.dateTo = this.formatDate(sunday);
  }

  setTab(tab: ActiveTab): void {
    this.activeTab = tab;
  }

  private formatDate(d: Date): string {
    return d.toISOString().split('T')[0];
  }
}

