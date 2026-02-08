import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { AlertDTO, ResolutionOption, ResolutionResult, MonitoringService } from '../monitoring.service';

@Component({
  selector: 'app-alert-list',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './alert-list.html',
  styleUrls: ['./alert-list.css']
})
export class AlertListComponent {
  @Input() alerts: AlertDTO[] = [];
  @Output() resolveAlert = new EventEmitter<{alertId: number, optionId: string}>();
  @Output() alertResolved = new EventEmitter<ResolutionResult>();

  expandedAlertId: number | null = null;
  resolvingId: number | null = null;
  resolutionFeedback: ResolutionResult | null = null;

  constructor(private monitoringService: MonitoringService) {}

  resolveWithOption(alert: AlertDTO, option: ResolutionOption): void {
    this.resolvingId = alert.id;

    this.monitoringService.resolveAlert(alert.id, option.id).subscribe({
      next: (result) => {
        this.resolutionFeedback = result;
        this.resolvingId = null;
        this.expandedAlertId = null;
        this.resolveAlert.emit({ alertId: alert.id, optionId: option.id });
        this.alertResolved.emit(result);

        setTimeout(() => this.resolutionFeedback = null, 5000);
      },
      error: () => {
        this.resolvingId = null;
      }
    });
  }

  getAlertTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      HIGH_QUEUE: 'Cola Alta', LOW_STAFF: 'Personal Bajo',
      ATTRACTION_DOWN: 'Atraccion Cerrada', CAPACITY_WARNING: 'Capacidad',
      SAFETY_CONCERN: 'Seguridad', TECHNICAL_ISSUE: 'Problema Tecnico',
      MEDICAL_EMERGENCY: 'Emergencia Medica', WEATHER_WARNING: 'Alerta Clima',
      MAINTENANCE_REQUIRED: 'Mantenimiento', GUEST_COMPLAINT: 'Queja Visitante',
      LOST_CHILD: 'Menor Perdido'
    };
    return labels[type] || type;
  }


  formatTime(timestamp: string): string {
    return new Date(timestamp).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
  }
}
