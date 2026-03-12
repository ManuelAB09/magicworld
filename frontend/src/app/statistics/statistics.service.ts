import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { getBackendBaseUrl } from '../config/backend';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from '../auth/auth.service';

export interface EmployeeHoursRankingDTO {
  employeeId: number;
  fullName: string;
  role: string;
  normalHours: number;
  overtimeHours: number;
  totalHours: number;
}

export interface EmployeeAbsenceRankingDTO {
  employeeId: number;
  fullName: string;
  role: string;
  absenceCount: number;
  scheduledDays: number;
}

export interface PositionFrequencyDTO {
  positionName: string;
  positionType: string;
  assignmentCount: number;
}

export interface SalaryReportDTO {
  employeeId: number;
  fullName: string;
  role: string;
  normalHours: number;
  overtimeHours: number;
  hourlyRate: number;
  overtimeRate: number;
  normalPay: number;
  overtimePay: number;
  totalSalary: number;
  currency: string;
}

export interface TicketSalesDTO {
  totalTicketsSold: number;
  totalRevenue: number;
  currency: string;
}

export interface MonthlySalesDTO {
  month: number;
  monthName: string;
  ticketsSold: number;
  revenue: number;
}

export interface AttractionPerformanceDTO {
  attractionId: number;
  attractionName: string;
  totalQueueEvents: number;
  maxQueueSize: number;
  avgQueueSize: number;
}

export interface EmployeeHoursSummaryDTO {
  employeeId: number;
  employeeName: string;
  role: string;
  scheduledHours: number;
  normalHoursWorked: number;
  overtimeHours: number;
  totalHoursWorked: number;
  absences: number;
  scheduledDays: number;
  workedDays: number;
  reinforcementDays: number;
  adjustments: WorkLogEntryDTO[];
}

export interface WorkLogEntryDTO {
  id: number;
  employeeId: number;
  employeeName: string;
  targetDate: string;
  action: string;
  hoursAffected: number;
  isOvertime: boolean;
  reason: string;
  performedBy: string;
  createdAt: string;
}

export interface WorkLogEntryRequest {
  employeeId: number;
  targetDate: string;
  action: string;
  hoursAffected: number;
  isOvertime?: boolean;
  reason: string;
}

@Injectable({ providedIn: 'root' })
export class StatisticsService {
  private readonly baseUrl = getBackendBaseUrl();

  constructor(
    private http: HttpClient,
    private translate: TranslateService,
    private auth: AuthService
  ) { }

  private withCsrf<T>(op: (h: HttpHeaders) => Observable<T>): Observable<T> {
    return this.auth.ensureCsrfToken(new HttpHeaders()).pipe(switchMap(op));
  }

  private get locale(): string {
    return this.translate.currentLang || this.translate.defaultLang || 'es';
  }

  // ── Employee Stats ──

  getHoursRanking(from: string, to: string): Observable<EmployeeHoursRankingDTO[]> {
    return this.http.get<EmployeeHoursRankingDTO[]>(
      `${this.baseUrl}/api/v1/statistics/employees/hours-ranking?from=${from}&to=${to}`,
      { withCredentials: true }
    );
  }

  getAbsenceRanking(from: string, to: string): Observable<EmployeeAbsenceRankingDTO[]> {
    return this.http.get<EmployeeAbsenceRankingDTO[]>(
      `${this.baseUrl}/api/v1/statistics/employees/absence-ranking?from=${from}&to=${to}`,
      { withCredentials: true }
    );
  }

  getPositionFrequency(employeeId: number, from: string, to: string): Observable<PositionFrequencyDTO[]> {
    return this.http.get<PositionFrequencyDTO[]>(
      `${this.baseUrl}/api/v1/statistics/employees/position-frequency/${employeeId}?from=${from}&to=${to}`,
      { withCredentials: true }
    );
  }

  getSalaryReport(from: string, to: string): Observable<SalaryReportDTO[]> {
    return this.http.get<SalaryReportDTO[]>(
      `${this.baseUrl}/api/v1/statistics/employees/salary?from=${from}&to=${to}&locale=${this.locale}`,
      { withCredentials: true }
    );
  }

  // ── Park Stats ──

  getTicketSales(from: string, to: string): Observable<TicketSalesDTO> {
    return this.http.get<TicketSalesDTO>(
      `${this.baseUrl}/api/v1/statistics/park/ticket-sales?from=${from}&to=${to}&locale=${this.locale}`,
      { withCredentials: true }
    );
  }

  getSeasonality(year: number): Observable<MonthlySalesDTO[]> {
    return this.http.get<MonthlySalesDTO[]>(
      `${this.baseUrl}/api/v1/statistics/park/seasonality?year=${year}&locale=${this.locale}`,
      { withCredentials: true }
    );
  }

  getAttractionPerformance(from: string, to: string): Observable<AttractionPerformanceDTO[]> {
    return this.http.get<AttractionPerformanceDTO[]>(
      `${this.baseUrl}/api/v1/statistics/park/attraction-performance?from=${from}&to=${to}`,
      { withCredentials: true }
    );
  }

  // ── WorkLog ──

  getEmployeeSummary(employeeId: number, from: string, to: string): Observable<EmployeeHoursSummaryDTO> {
    return this.http.get<EmployeeHoursSummaryDTO>(
      `${this.baseUrl}/api/v1/worklog/summary/${employeeId}?from=${from}&to=${to}`,
      { withCredentials: true }
    );
  }

  getWorkLogHistory(employeeId: number, from: string, to: string): Observable<WorkLogEntryDTO[]> {
    return this.http.get<WorkLogEntryDTO[]>(
      `${this.baseUrl}/api/v1/worklog/history/${employeeId}?from=${from}&to=${to}`,
      { withCredentials: true }
    );
  }

  addWorkLogEntry(request: WorkLogEntryRequest): Observable<WorkLogEntryDTO> {
    return this.withCsrf(h =>
      this.http.post<WorkLogEntryDTO>(
        `${this.baseUrl}/api/v1/worklog/entry`, request,
        { withCredentials: true, headers: h }
      )
    );
  }
}

