import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { AlertDTO, ResolutionOption, ResolutionResult, MonitoringService } from '../monitoring.service';
import { EmployeeService, AvailableEmployee, ReinforcementCandidate, EmployeeRole } from '../employee.service';

@Component({
  selector: 'app-alert-list',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './alert-list.html',
  styleUrls: ['./alert-list.css']
})
export class AlertListComponent {
  @Input() alerts: AlertDTO[] = [];
  @Output() resolveAlert = new EventEmitter<{ alertId: number, optionId: string }>();
  @Output() alertResolved = new EventEmitter<ResolutionResult>();

  expandedAlertId: number | null = null;
  resolvingId: number | null = null;
  resolutionFeedback: ResolutionResult | null = null;

  selectedOption: ResolutionOption | null = null;
  selectedAlertForResolution: AlertDTO | null = null;
  selectedEmployeeId: number | null = null;
  availableEmployees: AvailableEmployee[] = [];
  reinforcements: ReinforcementCandidate[] = [];
  loadingEmployees = false;
  callingReinforcementId: number | null = null;

  private readonly STAFF_OPTIONS = ['add_staff', 'send_medical', 'security_patrol', 'activate_search',
    'immediate_maintenance', 'assign_guest_services'];

  constructor(
    private monitoringService: MonitoringService,
    private employeeService: EmployeeService,
    private translate: TranslateService
  ) { }

  selectOption(alert: AlertDTO, option: ResolutionOption): void {
    if (!option.enabled) return;

    this.selectedOption = option;
    this.selectedAlertForResolution = alert;

    if (this.needsEmployeeSelection(option)) {
      this.loadAvailableEmployees(option.id);
    } else {
      this.executeResolution(alert, option.id, undefined);
    }
  }

  needsEmployeeSelection(option: ResolutionOption | null): boolean {
    return option !== null && this.STAFF_OPTIONS.includes(option.id);
  }

  private loadAvailableEmployees(optionId: string): void {
    this.loadingEmployees = true;
    const role = this.getRequiredRole(optionId);

    this.employeeService.getAvailableEmployees(role).subscribe({
      next: (response) => {
        const isQueueAlert = this.selectedAlertForResolution?.alertType === 'HIGH_QUEUE';
        this.availableEmployees = isQueueAlert ? [] : response.employees;
        this.reinforcements = response.reinforcements;
        this.loadingEmployees = false;
      },
      error: () => {
        this.loadingEmployees = false;
        this.availableEmployees = [];
      }
    });
  }

  private getRequiredRole(optionId: string): EmployeeRole {
    switch (optionId) {
      case 'add_staff': return 'OPERATOR';
      case 'send_medical': return 'MEDICAL';
      case 'security_patrol':
      case 'activate_search': return 'SECURITY';
      case 'immediate_maintenance': return 'MAINTENANCE';
      case 'assign_guest_services': return 'GUEST_SERVICES';
      default: return 'OPERATOR';
    }
  }

  selectEmployee(employeeId: number): void {
    this.selectedEmployeeId = employeeId;
  }

  confirmResolution(alert: AlertDTO): void {
    if (!this.selectedOption || !this.selectedEmployeeId) return;
    this.executeResolution(alert, this.selectedOption.id, this.selectedEmployeeId);
  }

  cancelEmployeeSelection(): void {
    this.selectedOption = null;
    this.selectedEmployeeId = null;
    this.availableEmployees = [];
    this.reinforcements = [];
    this.callingReinforcementId = null;
  }

  callReinforcement(employeeId: number): void {
    if (!this.selectedAlertForResolution) return;
    this.callingReinforcementId = employeeId;

    this.employeeService.callReinforcement(employeeId, this.selectedAlertForResolution.id).subscribe({
      next: (result: any) => {
        this.callingReinforcementId = null;
        const accepted = result.status === 'ACCEPTED';
        const name = result.employee?.fullName || '';

        this.resolutionFeedback = {
          success: accepted,
          message: accepted
            ? this.translate.instant('dashboard.reinforcementAccepted', { name })
            : this.translate.instant('dashboard.reinforcementRejected', { name }),
          actionTaken: accepted ? 'REINFORCEMENT_ACCEPTED' : 'REINFORCEMENT_REJECTED',
          resourcesUsed: {}
        };

        if (accepted) {
          this.resetSelectionState();
          this.expandedAlertId = null;
          this.resolveAlert.emit({ alertId: this.selectedAlertForResolution!.id, optionId: this.selectedOption?.id || 'add_staff' });
        }

        setTimeout(() => this.resolutionFeedback = null, 5000);
      },
      error: () => {
        this.callingReinforcementId = null;
        this.resolutionFeedback = {
          success: false,
          message: this.translate.instant('dashboard.errorCallingReinforcement'),
          actionTaken: '',
          resourcesUsed: {}
        };
        setTimeout(() => this.resolutionFeedback = null, 5000);
      }
    });
  }

  private executeResolution(alert: AlertDTO, optionId: string, employeeId: number | undefined): void {
    this.resolvingId = alert.id;

    this.monitoringService.resolveAlert(alert.id, optionId, employeeId).subscribe({
      next: (result) => {
        this.resolutionFeedback = result;
        this.resolvingId = null;
        this.expandedAlertId = null;
        this.resetSelectionState();

        this.resolveAlert.emit({ alertId: alert.id, optionId });
        this.alertResolved.emit(result);

        setTimeout(() => this.resolutionFeedback = null, 5000);
      },
      error: () => {
        this.resolvingId = null;
        this.resolutionFeedback = {
          success: false,
          message: this.translate.instant('dashboard.errorResolvingAlert'),
          actionTaken: '',
          resourcesUsed: {},
          failureReason: this.translate.instant('dashboard.networkError')
        };
        setTimeout(() => this.resolutionFeedback = null, 5000);
      }
    });
  }

  private resetSelectionState(): void {
    this.selectedOption = null;
    this.selectedAlertForResolution = null;
    this.selectedEmployeeId = null;
    this.availableEmployees = [];
    this.reinforcements = [];
  }

  getAlertTypeLabel(type: string): string {
    return this.translate.instant(`dashboard.alertTypes.${type}`);
  }

  getOptionLabel(optionId: string): string {
    return this.translate.instant(`dashboard.resolutions.${optionId}`);
  }

  getOptionDesc(optionId: string): string {
    return this.translate.instant(`dashboard.resolutions.${optionId}_desc`);
  }

  translateMessage(message: string): string {
    if (message && message.includes('.')) {
      const translated = this.translate.instant(message);
      if (translated !== message) {
        return translated;
      }
    }
    return message;
  }

  translateFeedback(result: ResolutionResult): string {
    if (result.code) {
      const translated = this.translate.instant(result.code, result.args);
      if (translated !== result.code) return translated;
    }
    return this.translateMessage(result.message);
  }

  translateFailureReason(result: ResolutionResult): string {
    if (result.failureReason && result.failureReason.includes('.')) {
      const translated = this.translate.instant(result.failureReason);
      if (translated !== result.failureReason) return translated;
    }
    return result.failureReason || '';
  }

  formatTime(timestamp: string): string {
    return new Date(timestamp).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  }
}
