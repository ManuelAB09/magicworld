import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import {
  EmployeeService, WeeklyScheduleDTO, EmployeeDTO, CoverageValidationResult, CoverageIssue,
  EmployeeRole, BreakGroup, WorkShift
} from '../admin-dashboard/employee.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { switchMap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { getBackendBaseUrl } from '../config/backend';
import { ErrorService } from '../error/error-service';
import { handleApiError } from '../shared/utils/error-handling.util';

interface EmployeeRow {
  id: number;
  name: string;
  role: EmployeeRole;
  days: DayCell[];
}

interface DayCell {
  assigned: boolean;
  scheduleId?: number;
  shift?: WorkShift;
  attractionName?: string;
  zoneName?: string;
  attractionId?: number;
  zoneId?: number;
  breakGroup?: BreakGroup;
  breakTime?: string;
  isOvertime?: boolean;
}

interface AttractionSimple {
  id: number;
  name: string;
}

interface ZoneSimple {
  id: number;
  zoneName: string;
}

interface GroupedIssuesByDate {
  date: string;
  groups: GroupedIssuesByType[];
}

interface GroupedIssuesByType {
  issueType: string;
  issues: CoverageIssue[];
}

@Component({
  selector: 'app-schedule-management',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './schedule-management.html',
  styleUrls: ['./schedule-management.css']
})
export class ScheduleManagementComponent implements OnInit {
  currentWeekStart = '';
  weekSchedules: WeeklyScheduleDTO[] = [];
  employees: EmployeeDTO[] = [];
  employeeRows: EmployeeRow[] = [];
  validation: CoverageValidationResult | null = null;
  showValidationDetails = false;

  groupedIssues: GroupedIssuesByDate[] = [];

  loading = true;
  errorKey: string | null = null;
  errorArgs: any = null;

  readonly days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
  readonly roles: EmployeeRole[] = ['OPERATOR', 'SECURITY', 'MEDICAL', 'MAINTENANCE', 'GUEST_SERVICES'];

  selectedRole: EmployeeRole | '' = '';

  readonly breakTimes: Record<BreakGroup, string> = {
    'A': '12:00 - 12:30',
    'B': '12:30 - 13:00',
    'C': '13:00 - 13:30',
    'D': '13:30 - 14:00'
  };

  showAssignModal = false;
  selectedEmployeeForAssign: EmployeeRow | null = null;
  selectedDayIndex = 0;
  selectedAttractionId: number | null = null;
  selectedZoneId: number | null = null;
  selectedBreakGroup: BreakGroup = 'A';

  attractions: AttractionSimple[] = [];
  zones: ZoneSimple[] = [];

  private baseUrl = getBackendBaseUrl();

  constructor(
    private employeeService: EmployeeService,
    private translate: TranslateService,
    private http: HttpClient,
    private auth: AuthService,
    private errorService: ErrorService
  ) { }

  ngOnInit(): void {
    this.currentWeekStart = this.getMonday(new Date());
    this.loadData();
    this.loadAttractions();
    this.loadZones();
  }

  private loadAttractions(): void {
    this.http.get<AttractionSimple[]>(`${this.baseUrl}/api/v1/attractions`, { withCredentials: true })
      .subscribe(attrs => this.attractions = attrs);
  }

  private loadZones(): void {
    this.http.get<ZoneSimple[]>(`${this.baseUrl}/api/zones`, { withCredentials: true })
      .subscribe(zones => this.zones = zones);
  }

  private getMonday(d: Date): string {
    const date = new Date(d);
    const day = date.getDay();
    const diff = date.getDate() - day + (day === 0 ? -6 : 1);
    date.setDate(diff);
    return date.toISOString().split('T')[0];
  }

  clearError(): void {
    this.errorKey = null;
    this.errorArgs = null;
  }

  private setApiError(err: any): void {
    const state = handleApiError(err, this.errorService);
    // ExceptionHandlerController wraps IllegalArgumentException with code 'error.invalid_argument'
    // and passes the actual error code in args[0]. Extract and translate the inner code.
    if (state.errorKey === 'error.invalid_argument' && state.errorArgs && state.errorArgs['0']) {
      const innerCode = state.errorArgs['0'];
      this.errorKey = innerCode;
      this.errorArgs = {};
    } else {
      this.errorKey = state.errorKey;
      this.errorArgs = state.errorArgs;
    }
  }

  loadData(): void {
    this.loading = true;
    this.clearError();
    this.employeeService.getActiveEmployees().subscribe({
      next: (emps) => {
        this.employees = emps;
        this.loadSchedule();
      },
      error: (err) => {
        this.setApiError(err);
        this.loading = false;
      }
    });
  }

  loadSchedule(): void {
    this.employeeService.getWeekSchedule(this.currentWeekStart).subscribe({
      next: (schedules) => {
        this.weekSchedules = schedules;
        this.buildEmployeeRows();
        this.validateSchedule();
        this.loading = false;
      },
      error: () => {
        this.weekSchedules = [];
        this.buildEmployeeRows();
        this.loading = false;
      }
    });
  }

  validateSchedule(): void {
    this.employeeService.validateWeekCoverage(this.currentWeekStart).subscribe({
      next: (result) => {
        this.validation = result;
        this.buildGroupedIssues();
      }
    });
  }

  private buildGroupedIssues(): void {
    if (!this.validation || this.validation.valid) {
      this.groupedIssues = [];
      return;
    }

    const byDate = new Map<string, CoverageIssue[]>();
    for (const issue of this.validation.issues) {
      const dateStr = issue.date;
      if (!byDate.has(dateStr)) {
        byDate.set(dateStr, []);
      }
      byDate.get(dateStr)!.push(issue);
    }

    this.groupedIssues = Array.from(byDate.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, issues]) => ({
        date,
        groups: this.groupByType(issues)
      }));
  }

  private groupByType(issues: CoverageIssue[]): GroupedIssuesByType[] {
    const byType = new Map<string, CoverageIssue[]>();
    for (const issue of issues) {
      const type = issue.issueType;
      if (!byType.has(type)) {
        byType.set(type, []);
      }
      byType.get(type)!.push(issue);
    }
    return Array.from(byType.entries()).map(([issueType, items]) => ({
      issueType, issues: items
    }));
  }

  getIssueGroupLabel(issueType: string): string {
    return this.translate.instant(`schedule.issueGroups.${issueType}`);
  }

  getIssueIcon(issueType: string): string {
    return this.translate.instant(`schedule.issueIcons.${issueType}`);
  }

  getIssueDescription(issue: CoverageIssue): string {
    const translated = this.translate.instant(`schedule.issues.${issue.issueType}`);
    if (issue.attractionName) {
      return `${translated}: ${issue.attractionName}`;
    }
    if (issue.zoneName) {
      return `${translated}: ${this.getTranslatedZoneName(issue.zoneName)}`;
    }
    return translated;
  }

  getDateDayLabel(dateStr: string): string {
    const date = new Date(dateStr + 'T00:00:00');
    const dayIndex = (date.getDay() + 6) % 7; // Monday=0
    const dayKeys = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'];
    const dayLabel = this.translate.instant(`schedule.days.${dayKeys[dayIndex]}`);
    return `${dayLabel} ${date.getDate()}`;
  }

  private buildEmployeeRows(): void {
    let filteredEmployees = this.employees;
    if (this.selectedRole) {
      filteredEmployees = this.employees.filter(e => e.role === this.selectedRole);
    }

    this.employeeRows = filteredEmployees.map(emp => {
      const days = this.days.map(day => {
        const schedule = this.weekSchedules.find(
          s => s.employeeId === emp.id && s.dayOfWeek === day
        );

        if (schedule) {
          return {
            assigned: true,
            scheduleId: schedule.id,
            shift: schedule.shift as WorkShift,
            attractionName: schedule.assignedAttractionName,
            zoneName: schedule.assignedZoneName,
            attractionId: schedule.assignedAttractionId,
            zoneId: schedule.assignedZoneId,
            breakGroup: schedule.breakGroup as BreakGroup,
            breakTime: this.breakTimes[schedule.breakGroup as BreakGroup],
            isOvertime: schedule.isOvertime ?? false
          };
        }
        return { assigned: false } as DayCell;
      });

      return { id: emp.id, name: emp.fullName, role: emp.role, days };
    });
  }

  previousWeek(): void {
    const date = new Date(this.currentWeekStart);
    date.setDate(date.getDate() - 7);
    this.currentWeekStart = date.toISOString().split('T')[0];
    this.loadSchedule();
  }

  nextWeek(): void {
    const date = new Date(this.currentWeekStart);
    date.setDate(date.getDate() + 7);
    this.currentWeekStart = date.toISOString().split('T')[0];
    this.loadSchedule();
  }

  copyFromPreviousWeek(): void {
    this.loading = true;
    this.clearError();
    this.employeeService.copyPreviousWeek(this.currentWeekStart).subscribe({
      next: () => this.loadSchedule(),
      error: (err) => {
        this.setApiError(err);
        this.loading = false;
      }
    });
  }

  autoAssignWeek(): void {
    this.loading = true;
    this.clearError();
    this.employeeService.autoAssignWeek(this.currentWeekStart).subscribe({
      next: () => {
        this.showValidationDetails = true;
        this.loadSchedule();
      },
      error: (err) => {
        this.setApiError(err);
        this.loading = false;
      }
    });
  }

  filterByRole(): void {
    this.buildEmployeeRows();
  }

  getWeekEndDate(): string {
    const date = new Date(this.currentWeekStart);
    date.setDate(date.getDate() + 6);
    return date.toISOString().split('T')[0];
  }

  getDayLabel(day: string): string {
    return this.translate.instant(`schedule.days.${day.toLowerCase()}`);
  }

  getDayDate(dayIndex: number): string {
    const date = new Date(this.currentWeekStart);
    date.setDate(date.getDate() + dayIndex);
    return date.getDate().toString();
  }

  getRoleBadgeClass(role: EmployeeRole): string {
    return `role-${role.toLowerCase().replace('_', '-')}`;
  }

  getShiftLabel(shift: WorkShift): string {
    return this.translate.instant(`schedule.shifts.${shift.toLowerCase()}`);
  }

  getAssignedCount(): number {
    return this.employeeRows.filter(r => r.days.some(d => d.assigned)).length;
  }

  hasIssues(): boolean {
    return this.validation !== null && !this.validation.valid;
  }

  openAssignModal(row: EmployeeRow, dayIndex: number): void {
    this.selectedEmployeeForAssign = row;
    this.selectedDayIndex = dayIndex;
    this.selectedAttractionId = null;
    this.selectedZoneId = null;
    this.selectedBreakGroup = 'A';
    this.showAssignModal = true;
  }

  closeAssignModal(): void {
    this.showAssignModal = false;
    this.selectedEmployeeForAssign = null;
  }

  canConfirmAssignment(): boolean {
    if (!this.selectedEmployeeForAssign) return false;
    const role = this.selectedEmployeeForAssign.role;
    if (role === 'OPERATOR') return this.selectedAttractionId !== null;
    if (role === 'SECURITY') return this.selectedZoneId !== null;
    return true;
  }

  confirmAssignment(): void {
    if (!this.selectedEmployeeForAssign) return;
    this.clearError();

    const request = {
      employeeId: this.selectedEmployeeForAssign.id,
      weekStartDate: this.currentWeekStart,
      dayOfWeek: this.days[this.selectedDayIndex],
      shift: 'FULL_DAY',
      breakGroup: this.selectedBreakGroup,
      assignedAttractionId: this.selectedAttractionId,
      assignedZoneId: this.selectedZoneId
    };

    const headers = new HttpHeaders();
    this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.post(`${this.baseUrl}/api/schedules`, request, { withCredentials: true, headers: h }))
    ).subscribe({
      next: () => {
        this.closeAssignModal();
        this.loadSchedule();
      },
      error: (err) => {
        this.setApiError(err);
      }
    });
  }

  removeAssignment(row: EmployeeRow, dayIndex: number): void {
    const cell = row.days[dayIndex];
    if (!cell.scheduleId) return;
    this.clearError();

    const headers = new HttpHeaders();
    this.auth.ensureCsrfToken(headers).pipe(
      switchMap(h => this.http.delete(`${this.baseUrl}/api/schedules/${cell.scheduleId}`, { withCredentials: true, headers: h }))
    ).subscribe({
      next: () => this.loadSchedule(),
      error: (err) => {
        this.setApiError(err);
      }
    });
  }

  getTranslatedZoneName(zoneName: string | undefined): string {
    if (!zoneName) return '';
    return this.translate.instant(`zones.${zoneName}`);
  }

  getTotalIssueCount(): number {
    return this.validation?.issues?.length ?? 0;
  }
}
