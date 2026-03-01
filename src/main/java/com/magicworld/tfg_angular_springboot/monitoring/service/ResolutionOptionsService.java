package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.employee.EmployeeRole;
import com.magicworld.tfg_angular_springboot.employee.dto.AvailableEmployeesResponse;
import com.magicworld.tfg_angular_springboot.employee.service.DailyOperationsService;
import com.magicworld.tfg_angular_springboot.monitoring.alert.*;
import com.magicworld.tfg_angular_springboot.monitoring.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResolutionOptionsService {

    private final AttractionRepository attractionRepository;
    private final DailyOperationsService dailyOperationsService;

    public List<ResolutionOption> getResolutionOptions(ParkAlert alert) {
        AlertType type = alert.getAlertType();
        return switch (type) {
            case HIGH_QUEUE -> getQueueOptions(alert);
            case MEDICAL_EMERGENCY -> getMedicalOptions(alert);
            case GUEST_COMPLAINT -> getComplaintOptions();
            case SAFETY_CONCERN, TECHNICAL_ISSUE -> getTechnicalOptions(alert);
            case MAINTENANCE_REQUIRED -> getMaintenanceOptions(alert);
            case LOST_CHILD -> getLostChildOptions(alert);
            case LOW_STAFF -> getStaffOptions(alert);
            case ATTRACTION_DOWN -> getAttractionDownOptions(alert);
            default -> getDefaultOptions();
        };
    }

    private List<ResolutionOption> getQueueOptions(ParkAlert alert) {
        return List.of(
                buildOption("add_staff", "Assign additional staff",
                        "Assign an available operator", true),
                buildOption("offer_fastpass", "Offer FastPass",
                        "Give FastPass to waiting visitors", true),
                buildOption("announce_pa", "Make announcement",
                        "Inform visitors via PA system", true));
    }

    private List<ResolutionOption> getMedicalOptions(ParkAlert alert) {
        AvailableEmployeesResponse medical = dailyOperationsService.checkAvailableEmployees(EmployeeRole.MEDICAL);

        return List.of(
                buildOption("send_medical", "Send medical team",
                        "Assign park medical staff", medical.isHasAvailable()),
                buildOption("call_ambulance", "Call ambulance",
                        "Contact emergency services", true));
    }

    private List<ResolutionOption> getComplaintOptions() {
        AvailableEmployeesResponse guestServices = dailyOperationsService
                .checkAvailableEmployees(EmployeeRole.GUEST_SERVICES);

        return List.of(
                buildOption("assign_guest_services", "Assign guest services",
                        "Send staff to handle complaint", guestServices.isHasAvailable()),
                buildOption("offer_compensation", "Offer compensation",
                        "Provide voucher or discount", true),
                buildOption("offer_fastpass", "Offer FastPass",
                        "Priority access to another attraction", true));
    }

    private List<ResolutionOption> getTechnicalOptions(ParkAlert alert) {
        AvailableEmployeesResponse security = dailyOperationsService.checkAvailableEmployees(EmployeeRole.SECURITY);
        AvailableEmployeesResponse maintenance = dailyOperationsService
                .checkAvailableEmployees(EmployeeRole.MAINTENANCE);
        boolean canClose = canCloseAttraction(alert.getAttractionId());

        return List.of(
                buildOption("security_patrol", "Send security",
                        "Patrol to evaluate situation", security.isHasAvailable()),
                buildOption("immediate_maintenance", "Immediate maintenance",
                        "Close and assign technician", maintenance.isHasAvailable() && canClose),
                buildOption("temporary_close", "Temporary closure",
                        "Close for inspection", canClose),
                buildOption("announce_pa", "PA announcement",
                        "Inform via loudspeaker", true));
    }

    private List<ResolutionOption> getMaintenanceOptions(ParkAlert alert) {
        AvailableEmployeesResponse maintenance = dailyOperationsService
                .checkAvailableEmployees(EmployeeRole.MAINTENANCE);
        boolean canClose = canCloseAttraction(alert.getAttractionId());

        return List.of(
                buildOption("schedule_maintenance", "Schedule maintenance",
                        "Plan for next closing", true),
                buildOption("immediate_maintenance", "Immediate maintenance",
                        "Close now and repair", maintenance.isHasAvailable() && canClose));
    }

    private List<ResolutionOption> getLostChildOptions(ParkAlert alert) {
        AvailableEmployeesResponse security = dailyOperationsService.checkAvailableEmployees(EmployeeRole.SECURITY);

        return List.of(
                buildOption("activate_search", "Activate search protocol",
                        "Full search with security", security.isHasAvailable()),
                buildOption("announce_pa", "PA announcement",
                        "Broadcast description", true),
                buildOption("security_patrol", "Alert security",
                        "Notify guards", security.isHasAvailable()));
    }

    private List<ResolutionOption> getStaffOptions(ParkAlert alert) {
        AvailableEmployeesResponse operators = dailyOperationsService.checkAvailableEmployees(EmployeeRole.OPERATOR);

        return List.of(
                buildOption("add_staff", "Assign available staff",
                        "Assign from available pool", operators.isHasAvailable()),
                buildOption("announce_pa", "Request via PA",
                        "Call for staff assistance", true));
    }

    private List<ResolutionOption> getAttractionDownOptions(ParkAlert alert) {
        AvailableEmployeesResponse maintenance = dailyOperationsService
                .checkAvailableEmployees(EmployeeRole.MAINTENANCE);

        return List.of(
                buildOption("immediate_maintenance", "Assign maintenance",
                        "Send technician to repair", maintenance.isHasAvailable()),
                buildOption("schedule_maintenance", "Schedule repair",
                        "Plan repair for later", true),
                buildOption("announce_pa", "Inform visitors",
                        "Announce temporary closure", true));
    }

    private List<ResolutionOption> getDefaultOptions() {
        return List.of(
                buildOption("acknowledge", "Acknowledge",
                        "Mark as seen", true));
    }

    private boolean canCloseAttraction(Long attractionId) {
        if (attractionId == null)
            return false;
        return attractionRepository.findById(attractionId)
                .map(Attraction::getIsActive)
                .orElse(false);
    }

    private ResolutionOption buildOption(String id, String label, String desc, boolean enabled) {
        return ResolutionOption.builder()
                .id(id)
                .label(label)
                .description(desc)
                .enabled(enabled)
                .build();
    }
}
