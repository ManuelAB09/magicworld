import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { getBackendBaseUrl } from '../config/backend';

export type EmployeeRole = 'OPERATOR' | 'SECURITY' | 'MEDICAL' | 'MAINTENANCE' | 'GUEST_SERVICES';
export type EmployeeStatus = 'ACTIVE' | 'ON_LEAVE' | 'TERMINATED';
export type DailyStatus = 'NOT_STARTED' | 'WORKING' | 'ON_BREAK' | 'ASSIGNED_TO_ALERT' | 'FINISHED';
export type WorkShift = 'MORNING' | 'AFTERNOON' | 'FULL_DAY';
export type BreakGroup = 'A' | 'B' | 'C' | 'D';

export interface EmployeeDTO {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone: string;
  role: EmployeeRole;
  status: EmployeeStatus;
  hireDate: string;
}

export interface CreateEmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  role: EmployeeRole;
}

export interface DailyAssignmentDTO {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeRole: EmployeeRole;
  assignmentDate: string;
  currentStatus: DailyStatus;
  currentZoneId?: number;
  currentZoneName?: string;
  currentAttractionId?: number;
  currentAttractionName?: string;
  assignedAlertId?: number;
  breakStartTime?: string;
  breakEndTime?: string;
}

export interface AvailableEmployee {
  id: number;
  name: string;
  role: EmployeeRole;
  currentLocation: string;
}

export interface ReinforcementCandidate {
  id: number;
  name: string;
  role: EmployeeRole;
  phone: string;
}

export interface AvailableEmployeesResponse {
  employees: AvailableEmployee[];
  reinforcements: ReinforcementCandidate[];
  hasAvailable: boolean;
  hasReinforcements: boolean;
}

export interface WeeklyScheduleDTO {
  id: number;
  employeeId: number;
  employeeName: string;
  weekStartDate: string;
  dayOfWeek: string;
  shift: WorkShift;
  assignedZoneId?: number;
  assignedZoneName?: string;
  assignedAttractionId?: number;
  assignedAttractionName?: string;
  breakGroup: BreakGroup;
  isOvertime?: boolean;
}

export interface CoverageIssue {
  date: string;
  issueType: string;
  description: string;
  attractionId?: number;
  attractionName?: string;
  zoneId?: number;
  zoneName?: string;
}

export interface CoverageValidationResult {
  valid: boolean;
  weekStartDate: string;
  issues: CoverageIssue[];
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private baseUrl = getBackendBaseUrl();

  constructor(private http: HttpClient, private auth: AuthService) { }

  private withCsrf<T>(op: (h: HttpHeaders) => Observable<T>): Observable<T> {
    return this.auth.ensureCsrfToken(new HttpHeaders()).pipe(switchMap(op));
  }

  getAllEmployees(): Observable<EmployeeDTO[]> {
    return this.http.get<EmployeeDTO[]>(`${this.baseUrl}/api/employees`, { withCredentials: true });
  }

  getActiveEmployees(): Observable<EmployeeDTO[]> {
    return this.http.get<EmployeeDTO[]>(`${this.baseUrl}/api/employees/active`, { withCredentials: true });
  }

  getEmployeesByRole(role: EmployeeRole): Observable<EmployeeDTO[]> {
    return this.http.get<EmployeeDTO[]>(`${this.baseUrl}/api/employees/role/${role}`, { withCredentials: true });
  }

  createEmployee(request: CreateEmployeeRequest): Observable<EmployeeDTO> {
    return this.withCsrf(h =>
      this.http.post<EmployeeDTO>(`${this.baseUrl}/api/employees`, request, { withCredentials: true, headers: h })
    );
  }

  updateEmployee(id: number, request: CreateEmployeeRequest): Observable<EmployeeDTO> {
    return this.withCsrf(h =>
      this.http.put<EmployeeDTO>(`${this.baseUrl}/api/employees/${id}`, request, { withCredentials: true, headers: h })
    );
  }

  terminateEmployee(id: number): Observable<void> {
    return this.withCsrf(h =>
      this.http.post<void>(`${this.baseUrl}/api/employees/${id}/terminate`, {}, { withCredentials: true, headers: h })
    );
  }

  getTodayAssignments(): Observable<DailyAssignmentDTO[]> {
    return this.http.get<DailyAssignmentDTO[]>(`${this.baseUrl}/api/daily-operations/today`, { withCredentials: true });
  }

  getAvailableEmployees(role: EmployeeRole): Observable<AvailableEmployeesResponse> {
    return this.http.get<AvailableEmployeesResponse>(
      `${this.baseUrl}/api/daily-operations/available?role=${role}`,
      { withCredentials: true }
    );
  }

  callReinforcement(employeeId: number, alertId: number): Observable<any> {
    return this.withCsrf(h =>
      this.http.post<any>(
        `${this.baseUrl}/api/daily-operations/call-reinforcement?employeeId=${employeeId}&alertId=${alertId}`,
        {},
        { withCredentials: true, headers: h }
      )
    );
  }

  initializeDay(date: string): Observable<void> {
    return this.withCsrf(h =>
      this.http.post<void>(`${this.baseUrl}/api/daily-operations/initialize?date=${date}`, {},
        { withCredentials: true, headers: h })
    );
  }

  getWeekSchedule(weekStart: string): Observable<WeeklyScheduleDTO[]> {
    return this.http.get<WeeklyScheduleDTO[]>(
      `${this.baseUrl}/api/schedules/week?weekStart=${weekStart}`,
      { withCredentials: true }
    );
  }

  validateWeekCoverage(weekStart: string): Observable<CoverageValidationResult> {
    return this.http.get<CoverageValidationResult>(
      `${this.baseUrl}/api/schedules/validate?weekStart=${weekStart}`,
      { withCredentials: true }
    );
  }

  copyPreviousWeek(targetWeekStart: string): Observable<void> {
    return this.withCsrf(h =>
      this.http.post<void>(
        `${this.baseUrl}/api/schedules/copy-week?targetWeekStart=${targetWeekStart}`,
        {},
        { withCredentials: true, headers: h }
      )
    );
  }

  autoAssignWeek(weekStart: string): Observable<void> {
    return this.withCsrf(h =>
      this.http.post<void>(
        `${this.baseUrl}/api/schedules/auto-assign?weekStart=${weekStart}`,
        {},
        { withCredentials: true, headers: h }
      )
    );
  }
}

