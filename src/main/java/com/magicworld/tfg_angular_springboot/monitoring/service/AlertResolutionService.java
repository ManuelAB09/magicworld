package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.employee.dto.AvailableEmployeesResponse;
import com.magicworld.tfg_angular_springboot.employee.service.DailyOperationsService;
import com.magicworld.tfg_angular_springboot.monitoring.alert.*;
import com.magicworld.tfg_angular_springboot.monitoring.dto.ResolutionResult;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import com.magicworld.tfg_angular_springboot.monitoring.simulator.ParkSimulatorService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("PMD.UnusedFormalParameter")
public class AlertResolutionService {

    private final ParkAlertRepository alertRepository;
    private final AttractionRepository attractionRepository;
    private final DailyAssignmentRepository dailyAssignmentRepository;
    private final DailyOperationsService dailyOperationsService;
    private final DashboardService dashboardService;
    private final ParkSimulatorService simulatorService;

    public AlertResolutionService(
            ParkAlertRepository alertRepository,
            AttractionRepository attractionRepository,
            DailyAssignmentRepository dailyAssignmentRepository,
            DailyOperationsService dailyOperationsService,
            @Lazy DashboardService dashboardService,
            @Lazy ParkSimulatorService simulatorService) {
        this.alertRepository = alertRepository;
        this.attractionRepository = attractionRepository;
        this.dailyAssignmentRepository = dailyAssignmentRepository;
        this.dailyOperationsService = dailyOperationsService;
        this.dashboardService = dashboardService;
        this.simulatorService = simulatorService;
    }

    @Transactional
    public ResolutionResult resolveAlert(Long alertId, String optionId, Long employeeId) {
        ParkAlert alert = alertRepository.findById(alertId)
                .orElse(null);

        if (alert == null) {
            return failResult("alerts.resolution.alert_not_found", new Object[] { alertId }, "Alert not found");
        }

        ResolutionResult result = executeResolution(alert, optionId, employeeId);

        if (result.isSuccess()) {
            markAlertResolved(alert);
        }

        return result;
    }

    private ResolutionResult executeResolution(ParkAlert alert, String optionId, Long employeeId) {
        return switch (optionId) {
            case "add_staff" -> handleAddStaff(alert, employeeId, EmployeeRole.OPERATOR);
            case "temporary_close" -> handleTemporaryClose(alert);
            case "send_medical" -> handleAddStaff(alert, employeeId, EmployeeRole.MEDICAL);
            case "call_ambulance" -> handleCallAmbulance(alert);
            case "security_patrol" -> handleAddStaff(alert, employeeId, EmployeeRole.SECURITY);
            case "activate_search" -> handleActivateSearch(alert, employeeId);
            case "offer_compensation", "offer_fastpass" -> handleCompensation(alert, optionId);
            case "schedule_maintenance" -> handleScheduleMaintenance(alert);
            case "immediate_maintenance" -> handleImmediateMaintenance(alert, employeeId);
            case "assign_guest_services" -> handleAddStaff(alert, employeeId, EmployeeRole.GUEST_SERVICES);
            case "announce_pa" -> handleAnnouncement(alert);
            case "acknowledge" -> handleAcknowledge(alert);
            default ->
                failResult("alerts.resolution.unknown_option", new Object[] { optionId }, "Unknown resolution option");
        };
    }

    private ResolutionResult handleAddStaff(ParkAlert alert, Long employeeId, EmployeeRole requiredRole) {
        if (employeeId == null) {
            return failResult("alerts.resolution.no_employee_selected", null, "No employee selected");
        }

        AvailableEmployeesResponse available = dailyOperationsService.getAvailableEmployees(requiredRole);
        boolean isAvailable = available.getEmployees().stream()
                .anyMatch(e -> e.getId().equals(employeeId));

        if (!isAvailable) {
            return failResult("alerts.resolution.employee_not_available", new Object[] { employeeId },
                    "Selected employee is not available");
        }

        dailyOperationsService.assignEmployeeToAlert(employeeId, alert.getId());

        String employeeName = available.getEmployees().stream()
                .filter(e -> e.getId().equals(employeeId))
                .findFirst()
                .map(AvailableEmployeesResponse.AvailableEmployee::getName)
                .orElse("Unknown");

        Map<String, Object> resources = new HashMap<>();
        resources.put("employeeId", employeeId);
        resources.put("employeeName", employeeName);
        resources.put("role", requiredRole.name());

        return successResult("alerts.resolution.staff_assigned", new Object[] { employeeName, requiredRole.name() },
                "Employee assigned to handle the alert", "STAFF_ASSIGNED", resources);
    }

    private ResolutionResult handleTemporaryClose(ParkAlert alert) {
        if (alert.getAttractionId() == null) {
            return failResult("alerts.resolution.no_attraction", null, "No attraction associated with this alert");
        }

        Attraction attraction = attractionRepository.findById(alert.getAttractionId())
                .orElse(null);

        if (attraction == null) {
            return failResult("alerts.resolution.attraction_not_found", new Object[] { alert.getAttractionId() },
                    "Attraction not found");
        }

        if (!attraction.getIsActive()) {
            return failResult("alerts.resolution.attraction_already_closed", new Object[] { attraction.getName() },
                    "Attraction is already closed");
        }

        attraction.setIsActive(false);
        attraction.setMaintenanceStatus(MaintenanceStatus.UNDER_MAINTENANCE);
        attractionRepository.save(attraction);

        dashboardService.updateAttractionState(attraction.getId(), ParkEventType.ATTRACTION_CLOSE, null);
        simulatorService.closeAttraction(attraction.getId());

        Map<String, Object> resources = new HashMap<>();
        resources.put("attractionId", attraction.getId());
        resources.put("attractionName", attraction.getName());

        return successResult("alerts.resolution.attraction_closed", new Object[] { attraction.getName() },
                "Attraction temporarily closed for inspection", "ATTRACTION_CLOSED", resources);
    }

    private ResolutionResult handleCallAmbulance(ParkAlert alert) {
        Map<String, Object> resources = new HashMap<>();
        resources.put("service", "Emergency Services");
        resources.put("callTime", LocalDateTime.now().toString());

        return successResult("alerts.resolution.ambulance_called", null, "Emergency services have been contacted",
                "AMBULANCE_CALLED", resources);
    }

    private ResolutionResult handleActivateSearch(ParkAlert alert, Long employeeId) {
        ResolutionResult staffResult = handleAddStaff(alert, employeeId, EmployeeRole.SECURITY);
        if (!staffResult.isSuccess()) {
            return staffResult;
        }

        Map<String, Object> resources = new HashMap<>(staffResult.getResourcesUsed());
        resources.put("protocol", "LOST_CHILD_SEARCH");
        resources.put("activatedAt", LocalDateTime.now().toString());

        return successResult("alerts.resolution.search_activated", null,
                "Search protocol activated with security assigned", "SEARCH_ACTIVATED", resources);
    }

    private ResolutionResult handleCompensation(ParkAlert alert, String type) {
        String compensationType = type.equals("offer_fastpass") ? "FastPass" : "Voucher";

        Map<String, Object> resources = new HashMap<>();
        resources.put("compensationType", compensationType);
        resources.put("issuedAt", LocalDateTime.now().toString());

        return successResult("alerts.resolution.compensation_issued", new Object[] { compensationType },
                compensationType + " issued to affected visitors", "COMPENSATION_ISSUED", resources);
    }

    private ResolutionResult handleScheduleMaintenance(ParkAlert alert) {
        if (alert.getAttractionId() == null) {
            return failResult("alerts.resolution.no_attraction", null, "No attraction associated with this alert");
        }

        Attraction attraction = attractionRepository.findById(alert.getAttractionId())
                .orElse(null);

        if (attraction == null) {
            return failResult("alerts.resolution.attraction_not_found", new Object[] { alert.getAttractionId() },
                    "Attraction not found");
        }

        attraction.setMaintenanceStatus(MaintenanceStatus.NEEDS_MAINTENANCE);
        attractionRepository.save(attraction);

        Map<String, Object> resources = new HashMap<>();
        resources.put("attractionId", attraction.getId());
        resources.put("attractionName", attraction.getName());
        resources.put("scheduledFor", "Next closing time");

        return successResult("alerts.resolution.maintenance_scheduled", new Object[] { attraction.getName() },
                "Maintenance scheduled for " + attraction.getName(), "MAINTENANCE_SCHEDULED", resources);
    }

    private ResolutionResult handleImmediateMaintenance(ParkAlert alert, Long employeeId) {
        ResolutionResult closeResult = handleTemporaryClose(alert);
        if (!closeResult.isSuccess()) {
            return closeResult;
        }

        if (employeeId != null) {
            ResolutionResult staffResult = handleAddStaff(alert, employeeId, EmployeeRole.MAINTENANCE);
            if (staffResult.isSuccess()) {
                Map<String, Object> resources = new HashMap<>(closeResult.getResourcesUsed());
                resources.putAll(staffResult.getResourcesUsed());
                return successResult("alerts.resolution.immediate_maintenance", null,
                        "Attraction closed and maintenance technician assigned", "IMMEDIATE_MAINTENANCE", resources);
            }
        }

        return successResult("alerts.resolution.immediate_maintenance", null,
                "Attraction closed for immediate maintenance", "IMMEDIATE_MAINTENANCE", closeResult.getResourcesUsed());
    }

    private ResolutionResult handleAnnouncement(ParkAlert alert) {
        Map<String, Object> resources = new HashMap<>();
        resources.put("announcementTime", LocalDateTime.now().toString());
        resources.put("channel", "PA System");

        return successResult("alerts.resolution.announcement_made", null, "Public announcement made through PA system",
                "ANNOUNCEMENT_MADE", resources);
    }

    private ResolutionResult handleAcknowledge(ParkAlert alert) {
        Map<String, Object> resources = new HashMap<>();
        resources.put("acknowledgedAt", LocalDateTime.now().toString());

        return successResult("alerts.resolution.acknowledged", null, "Alert acknowledged and marked as handled",
                "ACKNOWLEDGED", resources);
    }

    private void markAlertResolved(ParkAlert alert) {
        alert.setIsActive(false);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);

        releaseAssignedEmployees(alert.getId());
    }

    private void releaseAssignedEmployees(Long alertId) {
        List<DailyAssignment> assigned = dailyAssignmentRepository
                .findByDateAndAlert(LocalDate.now(), alertId);

        for (DailyAssignment assignment : assigned) {
            assignment.setCurrentStatus(DailyStatus.WORKING);
            assignment.setAssignedAlert(null);
            dailyAssignmentRepository.save(assignment);
        }
    }

    private ResolutionResult successResult(String code, Object[] args, String message, String action,
            Map<String, Object> resources) {
        return ResolutionResult.builder()
                .success(true)
                .actionTaken(action)
                .message(message)
                .code(code)
                .args(args)
                .resourcesUsed(resources)
                .build();
    }

    private ResolutionResult failResult(String code, Object[] args, String reason) {
        return ResolutionResult.builder()
                .success(false)
                .message("Resolution failed")
                .failureReason(reason)
                .code(code)
                .args(args)
                .build();
    }
}
