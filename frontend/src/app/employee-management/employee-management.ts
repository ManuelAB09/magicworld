import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { EmployeeService, EmployeeDTO, CreateEmployeeRequest, EmployeeRole, EmployeeStatus } from '../admin-dashboard/employee.service';
import { ErrorService } from '../error/error-service';
import { handleApiError } from '../shared/utils/error-handling.util';

@Component({
  selector: 'app-employee-management',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './employee-management.html',
  styleUrls: ['./employee-management.css']
})
export class EmployeeManagementComponent implements OnInit {
  employees: EmployeeDTO[] = [];
  filteredEmployees: EmployeeDTO[] = [];
  loading = true;
  errorKey: string | null = null;
  errorArgs: any = null;
  validationMessages: string[] = [];

  showForm = false;
  editingEmployee: EmployeeDTO | null = null;

  formData: CreateEmployeeRequest = {
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    role: 'OPERATOR'
  };

  roleFilter: EmployeeRole | '' = '';
  statusFilter: EmployeeStatus | '' = '';

  readonly roles: EmployeeRole[] = ['OPERATOR', 'SECURITY', 'MEDICAL', 'MAINTENANCE', 'GUEST_SERVICES'];
  readonly statuses: EmployeeStatus[] = ['ACTIVE', 'ON_LEAVE', 'TERMINATED'];

  constructor(
    private employeeService: EmployeeService,
    private translate: TranslateService,
    private errorService: ErrorService
  ) { }

  ngOnInit(): void {
    this.loadEmployees();
  }

  clearError(): void {
    this.errorKey = null;
    this.errorArgs = null;
    this.validationMessages = [];
  }

  private setApiError(err: any, fallbackKey: string): void {
    const state = handleApiError(err, this.errorService);
    this.validationMessages = state.validationMessages || [];

    if (this.validationMessages.length > 0) {
      // Validation errors with per-field messages
      this.errorKey = 'error.validation';
      this.errorArgs = null;
    } else if (state.errorKey === 'error.invalid_argument' && state.errorArgs && state.errorArgs['0']) {
      this.errorKey = state.errorArgs['0'];
      this.errorArgs = {};
    } else if (state.errorKey && state.errorKey !== 'error.unexpected') {
      this.errorKey = state.errorKey;
      this.errorArgs = state.errorArgs;
    } else {
      this.errorKey = fallbackKey;
      this.errorArgs = null;
    }
  }

  loadEmployees(): void {
    this.loading = true;
    this.clearError();
    this.employeeService.getAllEmployees().subscribe({
      next: (employees) => {
        this.employees = employees.filter(e => e.status !== 'TERMINATED');
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.setApiError(err, 'employees.errorLoading');
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredEmployees = this.employees.filter(e => {
      const matchesRole = !this.roleFilter || e.role === this.roleFilter;
      const matchesStatus = !this.statusFilter || e.status === this.statusFilter;
      return matchesRole && matchesStatus;
    });
  }

  openNewForm(): void {
    this.editingEmployee = null;
    this.formData = { firstName: '', lastName: '', email: '', phone: '', role: 'OPERATOR' };
    this.showForm = true;
    this.clearError();
  }

  openEditForm(employee: EmployeeDTO): void {
    this.editingEmployee = employee;
    this.formData = {
      firstName: employee.firstName,
      lastName: employee.lastName,
      email: employee.email,
      phone: employee.phone || '',
      role: employee.role
    };
    this.showForm = true;
    this.clearError();
  }

  closeForm(): void {
    this.showForm = false;
    this.editingEmployee = null;
    this.clearError();
  }

  saveEmployee(): void {
    this.clearError();
    if (this.editingEmployee) {
      this.employeeService.updateEmployee(this.editingEmployee.id, this.formData).subscribe({
        next: () => {
          this.closeForm();
          this.loadEmployees();
        },
        error: (err) => this.setApiError(err, 'employees.errorUpdating')
      });
    } else {
      this.employeeService.createEmployee(this.formData).subscribe({
        next: () => {
          this.closeForm();
          this.loadEmployees();
        },
        error: (err) => this.setApiError(err, 'employees.errorCreating')
      });
    }
  }

  terminateEmployee(id: number): void {
    const msg = this.translate.instant('employees.confirmTerminate');
    if (confirm(msg)) {
      this.clearError();
      this.employeeService.terminateEmployee(id).subscribe({
        next: () => this.loadEmployees(),
        error: (err) => this.setApiError(err, 'employees.errorTerminating')
      });
    }
  }

  getRoleBadgeClass(role: EmployeeRole): string {
    const classes: Record<EmployeeRole, string> = {
      OPERATOR: 'badge-operator',
      SECURITY: 'badge-security',
      MEDICAL: 'badge-medical',
      MAINTENANCE: 'badge-maintenance',
      GUEST_SERVICES: 'badge-guest'
    };
    return classes[role];
  }

  getStatusBadgeClass(status: EmployeeStatus): string {
    const classes: Record<EmployeeStatus, string> = {
      ACTIVE: 'status-active',
      ON_LEAVE: 'status-leave',
      TERMINATED: 'status-terminated'
    };
    return classes[status];
  }

  getRoleLabel(role: EmployeeRole): string {
    return this.translate.instant(`schedule.roles.${role.toLowerCase()}`);
  }

  getStatusLabel(status: EmployeeStatus): string {
    const key = status.toLowerCase();
    return this.translate.instant(`employees.statuses.${key}`);
  }
}
