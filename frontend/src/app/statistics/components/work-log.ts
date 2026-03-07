import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import {
  StatisticsService,
  EmployeeHoursSummaryDTO,
  WorkLogEntryDTO,
  WorkLogEntryRequest
} from '../statistics.service';
import { EmployeeService, EmployeeDTO } from '../../admin-dashboard/employee.service';

@Component({
  selector: 'app-work-log',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './work-log.html',
  styleUrls: ['./work-log.css']
})
export class WorkLogComponent implements OnInit, OnChanges {
  @Input() dateFrom = '';
  @Input() dateTo = '';

  employees: EmployeeDTO[] = [];
  selectedEmployeeId: number | null = null;

  summary: EmployeeHoursSummaryDTO | null = null;
  history: WorkLogEntryDTO[] = [];
  loading = false;
  showForm = false;
  successMessage = '';

  formData: WorkLogEntryRequest = {
    employeeId: 0,
    targetDate: '',
    action: 'ADD_OVERTIME_HOURS',
    hoursAffected: 0,
    isOvertime: false,
    reason: ''
  };

  errorMessage = '';
  readonly todayStr = new Date().toISOString().split('T')[0];

  readonly actions = [
    'ADD_OVERTIME_HOURS',
    'REMOVE_SCHEDULED_DAY',
    'ADD_ABSENCE',
    'REMOVE_ABSENCE',
    'PARTIAL_ABSENCE'
  ];

  constructor(
    private statsService: StatisticsService,
    private employeeService: EmployeeService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.employeeService.getActiveEmployees().subscribe({
      next: data => this.employees = data
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['dateFrom'] || changes['dateTo']) && this.selectedEmployeeId) {
      this.loadSummary();
    }
  }

  onEmployeeSelected(): void {
    if (this.selectedEmployeeId) {
      this.loadSummary();
    } else {
      this.summary = null;
      this.history = [];
    }
  }

  loadSummary(): void {
    if (!this.selectedEmployeeId || !this.dateFrom || !this.dateTo) return;
    this.loading = true;
    this.successMessage = '';

    this.statsService.getEmployeeSummary(this.selectedEmployeeId, this.dateFrom, this.dateTo).subscribe({
      next: data => {
        this.summary = data;
        this.history = data.adjustments || [];
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openForm(): void {
    if (!this.selectedEmployeeId) return;
    this.formData = {
      employeeId: this.selectedEmployeeId,
      targetDate: this.todayStr,
      action: 'ADD_OVERTIME_HOURS',
      hoursAffected: 2,
      isOvertime: true,
      reason: ''
    };
    this.showForm = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  closeForm(): void {
    this.showForm = false;
    this.errorMessage = '';
  }

  onActionChange(): void {
    this.errorMessage = '';
    if (this.formData.action === 'ADD_OVERTIME_HOURS') {
      this.formData.isOvertime = true;
      this.formData.hoursAffected = 2;
    } else if (this.formData.action === 'ADD_ABSENCE' || this.formData.action === 'REMOVE_ABSENCE') {
      this.formData.hoursAffected = 8;
      this.formData.isOvertime = false;
    } else if (this.formData.action === 'PARTIAL_ABSENCE') {
      this.formData.hoursAffected = 1;
      this.formData.isOvertime = false;
    } else {
      this.formData.hoursAffected = 8;
      this.formData.isOvertime = false;
    }
  }

  submitEntry(): void {
    this.errorMessage = '';
    this.statsService.addWorkLogEntry(this.formData).subscribe({
      next: () => {
        this.showForm = false;
        this.successMessage = this.translate.instant('stats.worklog.entryAdded');
        this.errorMessage = '';
        this.loadSummary();
      },
      error: (err) => {
        const args = err?.error?.args;
        const code = (args && args.length > 0) ? args[0] : (err?.error?.code || '');
        this.errorMessage = code ? this.translate.instant(code) : this.translate.instant('stats.worklog.error');
        this.successMessage = '';
      }
    });
  }

  getRoleLabel(role: string): string {
    return this.translate.instant(`schedule.roles.${role.toLowerCase()}`);
  }

  getActionLabel(action: string): string {
    return this.translate.instant(`stats.worklog.actions.${action}`);
  }

  getActionIcon(action: string): string {
    const icons: Record<string, string> = {
      'ADD_OVERTIME_HOURS': '⏰',
      'REMOVE_SCHEDULED_DAY': '📅',
      'ADD_ABSENCE': '❌',
      'REMOVE_ABSENCE': '✅',
      'PARTIAL_ABSENCE': '⏳'
    };
    return icons[action] || '📝';
  }
}
