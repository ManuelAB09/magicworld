import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import {
  StatisticsService,
  EmployeeHoursRankingDTO,
  EmployeeAbsenceRankingDTO,
  SalaryReportDTO,
  PositionFrequencyDTO
} from '../statistics.service';
import { EmployeeService, EmployeeDTO } from '../../admin-dashboard/employee.service';
import { CurrencyService } from '../../shared/currency.service';

type SubView = 'hours' | 'absences' | 'salary' | 'frequency';

@Component({
  selector: 'app-employee-stats',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './employee-stats.html',
  styleUrls: ['./employee-stats.css']
})
export class EmployeeStatsComponent implements OnInit, OnChanges {
  @Input() dateFrom = '';
  @Input() dateTo = '';

  subView: SubView = 'hours';
  loading = false;

  hoursRanking: EmployeeHoursRankingDTO[] = [];
  absenceRanking: EmployeeAbsenceRankingDTO[] = [];
  salaryReport: SalaryReportDTO[] = [];
  positionFrequency: PositionFrequencyDTO[] = [];

  employees: EmployeeDTO[] = [];
  selectedEmployeeId: number | null = null;

  constructor(
    private statsService: StatisticsService,
    private employeeService: EmployeeService,
    private currency: CurrencyService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
    this.loadData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['dateFrom'] || changes['dateTo']) && this.dateFrom && this.dateTo) {
      this.loadData();
    }
  }

  setSubView(view: SubView): void {
    this.subView = view;
    this.loadData();
  }

  loadData(): void {
    if (!this.dateFrom || !this.dateTo) return;
    this.loading = true;

    switch (this.subView) {
      case 'hours':
        this.statsService.getHoursRanking(this.dateFrom, this.dateTo).subscribe({
          next: data => { this.hoursRanking = data; this.loading = false; },
          error: () => this.loading = false
        });
        break;
      case 'absences':
        this.statsService.getAbsenceRanking(this.dateFrom, this.dateTo).subscribe({
          next: data => { this.absenceRanking = data; this.loading = false; },
          error: () => this.loading = false
        });
        break;
      case 'salary':
        this.statsService.getSalaryReport(this.dateFrom, this.dateTo).subscribe({
          next: data => { this.salaryReport = data; this.loading = false; },
          error: () => this.loading = false
        });
        break;
      case 'frequency':
        if (this.selectedEmployeeId) {
          this.statsService.getPositionFrequency(this.selectedEmployeeId, this.dateFrom, this.dateTo).subscribe({
            next: data => { this.positionFrequency = data; this.loading = false; },
            error: () => this.loading = false
          });
        } else {
          this.loading = false;
        }
        break;
    }
  }

  loadEmployees(): void {
    this.employeeService.getActiveEmployees().subscribe({
      next: data => this.employees = data
    });
  }

  onEmployeeSelected(): void {
    if (this.selectedEmployeeId) {
      this.loadData();
    }
  }

  getRoleLabel(role: string): string {
    return this.translate.instant(`schedule.roles.${role.toLowerCase()}`);
  }

  getCurrencySymbol(): string {
    return this.currency.getCurrencySymbol();
  }

  getPositionIcon(type: string): string {
    return type === 'ATTRACTION' ? '🎢' : type === 'ZONE' ? '📍' : '🏢';
  }

  getTotalSalary(): number {
    return this.salaryReport.reduce((sum, r) => sum + r.totalSalary, 0);
  }
}

