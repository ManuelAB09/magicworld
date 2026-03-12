package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.monitoring.alert.*;
import com.magicworld.tfg_angular_springboot.monitoring.dto.ResolutionResult;
import com.magicworld.tfg_angular_springboot.monitoring.service.AlertResolutionService;
import com.magicworld.tfg_angular_springboot.monitoring.service.MonitoringWebSocketService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Monitorización del Parque")
@Feature("Servicio de Resolución de Alertas")
public class AlertResolutionServiceTests {

    private static final String CODE_ALERT_NOT_FOUND = "alerts.resolution.alert_not_found";
    private static final String CODE_NO_EMPLOYEE = "alerts.resolution.no_employee_selected";
    private static final String CODE_EMPLOYEE_NOT_AVAILABLE = "alerts.resolution.employee_not_available";
    private static final String CODE_NO_ATTRACTION = "alerts.resolution.no_attraction";
    private static final String CODE_ALREADY_CLOSED = "alerts.resolution.attraction_already_closed";
    private static final String CODE_UNKNOWN_OPTION = "alerts.resolution.unknown_option";
    private static final String ACTION_ACKNOWLEDGED = "ACKNOWLEDGED";
    private static final String ACTION_ANNOUNCEMENT = "ANNOUNCEMENT_MADE";
    private static final String ACTION_ATTRACTION_CLOSED = "ATTRACTION_CLOSED";
    private static final String ACTION_STAFF_ASSIGNED = "STAFF_ASSIGNED";
    private static final String ACTION_COMPENSATION = "COMPENSATION_ISSUED";
    private static final String OPTION_ACKNOWLEDGE = "acknowledge";
    private static final String OPTION_ANNOUNCE = "announce_pa";
    private static final String OPTION_TEMP_CLOSE = "temporary_close";
    private static final String OPTION_ADD_STAFF = "add_staff";
    private static final String OPTION_CALL_AMBULANCE = "call_ambulance";
    private static final String OPTION_OFFER_COMPENSATION = "offer_compensation";
    private static final String OPTION_OFFER_FASTPASS = "offer_fastpass";
    private static final String OPTION_SCHEDULE_MAINTENANCE = "schedule_maintenance";
    private static final String OPTION_IMMEDIATE_MAINTENANCE = "immediate_maintenance";
    private static final String OPTION_ACTIVATE_SEARCH = "activate_search";

    @Autowired
    private AlertResolutionService alertResolutionService;

    @Autowired
    private ParkAlertRepository alertRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DailyAssignmentRepository dailyAssignmentRepository;

    @MockitoBean
    private MonitoringWebSocketService webSocketService;

    private Attraction testAttraction;
    private Employee testOperator;

    @BeforeEach
    void setUp() {
        dailyAssignmentRepository.deleteAll();
        alertRepository.deleteAll();

        testAttraction = attractionRepository.save(Attraction.builder()
                .name("Resolution Test Coaster").description("Test").photoUrl("http://test.com/img.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(5.0).mapPositionY(5.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        testOperator = employeeRepository.save(Employee.builder()
                .firstName("Test").lastName("Operator").email("test.operator@magicworld.com")
                .phone("123456789").role(EmployeeRole.OPERATOR)
                .status(EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2024, 1, 1))
                .build());
    }

    private ParkAlert createActiveAlert(AlertType type, Long attractionId) {
        return alertRepository.save(ParkAlert.builder()
                .alertType(type).severity(AlertSeverity.WARNING)
                .message("Test alert").suggestion("Fix it")
                .attractionId(attractionId)
                .timestamp(LocalDateTime.now()).isActive(true)
                .build());
    }

    private DailyAssignment createWorkingAssignment(Employee employee) {
        return dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(employee).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.WORKING).currentAttraction(testAttraction)
                .build());
    }

    @Test
    @Story("Resolver Alerta")
    @Description("Verifica que resolver alerta inexistente retorna fallo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("resolveAlert con ID inexistente retorna fallo")
    void testResolveAlertNotFound() {
        ResolutionResult result = alertResolutionService.resolveAlert(999L, OPTION_ACKNOWLEDGE, null);
        assertFalse(result.isSuccess());
        assertEquals(CODE_ALERT_NOT_FOUND, result.getCode());
    }

    @Test
    @Story("Acknowledge")
    @Description("Verifica que acknowledge resuelve la alerta correctamente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("acknowledge resuelve alerta exitosamente")
    void testResolveAlertAcknowledge() {
        ParkAlert alert = createActiveAlert(AlertType.HIGH_QUEUE, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_ACKNOWLEDGE, null);

        assertTrue(result.isSuccess());
        assertEquals(ACTION_ACKNOWLEDGED, result.getActionTaken());
        assertFalse(alertRepository.findById(alert.getId()).get().getIsActive());
    }

    @Test
    @Story("Anuncio PA")
    @Description("Verifica que anuncio PA resuelve la alerta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("announce_pa resuelve alerta exitosamente")
    void testResolveAlertAnnouncement() {
        ParkAlert alert = createActiveAlert(AlertType.HIGH_QUEUE, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_ANNOUNCE, null);

        assertTrue(result.isSuccess());
        assertEquals(ACTION_ANNOUNCEMENT, result.getActionTaken());
    }

    @Test
    @Story("Cierre Temporal")
    @Description("Verifica que cierre temporal desactiva la atracción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("temporary_close desactiva atracción")
    void testResolveAlertTemporaryCloseSuccess() {
        ParkAlert alert = createActiveAlert(AlertType.TECHNICAL_ISSUE, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_TEMP_CLOSE, null);

        assertTrue(result.isSuccess());
        assertEquals(ACTION_ATTRACTION_CLOSED, result.getActionTaken());
        Attraction updated = attractionRepository.findById(testAttraction.getId()).get();
        assertFalse(updated.getIsActive());
        assertEquals(MaintenanceStatus.UNDER_MAINTENANCE, updated.getMaintenanceStatus());
    }

    @Test
    @Story("Cierre Temporal")
    @Description("Verifica que cierre temporal sin atracción falla")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("temporary_close sin atracción retorna fallo")
    void testResolveAlertTemporaryCloseNoAttraction() {
        ParkAlert alert = createActiveAlert(AlertType.LOST_CHILD, null);
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_TEMP_CLOSE, null);

        assertFalse(result.isSuccess());
        assertEquals(CODE_NO_ATTRACTION, result.getCode());
    }

    @Test
    @Story("Cierre Temporal")
    @Description("Verifica que cierre temporal de atracción ya cerrada falla")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("temporary_close atracción ya cerrada falla")
    void testResolveAlertTemporaryCloseAlreadyClosed() {
        testAttraction.setIsActive(false);
        attractionRepository.save(testAttraction);
        ParkAlert alert = createActiveAlert(AlertType.TECHNICAL_ISSUE, testAttraction.getId());

        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_TEMP_CLOSE, null);
        assertFalse(result.isSuccess());
        assertEquals(CODE_ALREADY_CLOSED, result.getCode());
    }

    @Test
    @Story("Asignar Personal")
    @Description("Verifica que asignar personal sin ID de empleado falla")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("add_staff sin employeeId retorna fallo")
    void testResolveAlertAddStaffNoEmployee() {
        ParkAlert alert = createActiveAlert(AlertType.HIGH_QUEUE, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_ADD_STAFF, null);

        assertFalse(result.isSuccess());
        assertEquals(CODE_NO_EMPLOYEE, result.getCode());
    }

    @Test
    @Story("Asignar Personal")
    @Description("Verifica que asignar empleado no disponible falla")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("add_staff empleado no disponible retorna fallo")
    void testResolveAlertAddStaffEmployeeNotAvailable() {
        ParkAlert alert = createActiveAlert(AlertType.HIGH_QUEUE, testAttraction.getId());
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(testOperator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.ON_BREAK)
                .build());

        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_ADD_STAFF, testOperator.getId());
        assertFalse(result.isSuccess());
        assertEquals(CODE_EMPLOYEE_NOT_AVAILABLE, result.getCode());
    }

    @Test
    @Story("Asignar Personal")
    @Description("Verifica que asignar empleado disponible tiene éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("add_staff empleado disponible tiene éxito")
    void testResolveAlertAddStaffSuccess() {
        ParkAlert alert = createActiveAlert(AlertType.HIGH_QUEUE, testAttraction.getId());
        createWorkingAssignment(testOperator);

        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_ADD_STAFF, testOperator.getId());
        assertTrue(result.isSuccess());
        assertEquals(ACTION_STAFF_ASSIGNED, result.getActionTaken());
    }

    @Test
    @Story("Llamar Ambulancia")
    @Description("Verifica que llamar ambulancia tiene éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("call_ambulance retorna éxito con Emergency Services")
    void testResolveAlertCallAmbulance() {
        ParkAlert alert = createActiveAlert(AlertType.MEDICAL_EMERGENCY, null);
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_CALL_AMBULANCE, null);

        assertTrue(result.isSuccess());
        assertEquals("AMBULANCE_CALLED", result.getActionTaken());
        assertEquals("Emergency Services", result.getResourcesUsed().get("service"));
    }

    @Test
    @Story("Compensación")
    @Description("Verifica que ofrecer compensación genera Voucher")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("offer_compensation genera Voucher")
    void testResolveAlertOfferCompensation() {
        ParkAlert alert = createActiveAlert(AlertType.GUEST_COMPLAINT, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_OFFER_COMPENSATION, null);

        assertTrue(result.isSuccess());
        assertEquals(ACTION_COMPENSATION, result.getActionTaken());
        assertEquals("Voucher", result.getResourcesUsed().get("compensationType"));
    }

    @Test
    @Story("Compensación")
    @Description("Verifica que ofrecer FastPass genera FastPass")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("offer_fastpass genera FastPass")
    void testResolveAlertOfferFastpass() {
        ParkAlert alert = createActiveAlert(AlertType.GUEST_COMPLAINT, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_OFFER_FASTPASS, null);

        assertTrue(result.isSuccess());
        assertEquals(ACTION_COMPENSATION, result.getActionTaken());
        assertEquals("FastPass", result.getResourcesUsed().get("compensationType"));
    }

    @Test
    @Story("Mantenimiento Programado")
    @Description("Verifica que programar mantenimiento actualiza estado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("schedule_maintenance actualiza estado a NEEDS_MAINTENANCE")
    void testResolveAlertScheduleMaintenance() {
        ParkAlert alert = createActiveAlert(AlertType.MAINTENANCE_REQUIRED, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_SCHEDULE_MAINTENANCE, null);

        assertTrue(result.isSuccess());
        assertEquals("MAINTENANCE_SCHEDULED", result.getActionTaken());
        assertEquals(MaintenanceStatus.NEEDS_MAINTENANCE,
                attractionRepository.findById(testAttraction.getId()).get().getMaintenanceStatus());
    }

    @Test
    @Story("Mantenimiento Programado")
    @Description("Verifica que programar mantenimiento sin atracción falla")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("schedule_maintenance sin atracción falla")
    void testResolveAlertScheduleMaintenanceNoAttraction() {
        ParkAlert alert = createActiveAlert(AlertType.MAINTENANCE_REQUIRED, null);
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_SCHEDULE_MAINTENANCE, null);

        assertFalse(result.isSuccess());
        assertEquals(CODE_NO_ATTRACTION, result.getCode());
    }

    @Test
    @Story("Mantenimiento Inmediato")
    @Description("Verifica que mantenimiento inmediato cierra atracción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("immediate_maintenance sin empleado cierra atracción")
    void testResolveAlertImmediateMaintenanceWithoutEmployee() {
        ParkAlert alert = createActiveAlert(AlertType.TECHNICAL_ISSUE, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_IMMEDIATE_MAINTENANCE, null);

        assertTrue(result.isSuccess());
        assertEquals("IMMEDIATE_MAINTENANCE", result.getActionTaken());
        assertFalse(attractionRepository.findById(testAttraction.getId()).get().getIsActive());
    }

    @Test
    @Story("Opción Desconocida")
    @Description("Verifica que opción desconocida retorna fallo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("opción desconocida retorna fallo")
    void testResolveAlertUnknownOption() {
        ParkAlert alert = createActiveAlert(AlertType.HIGH_QUEUE, testAttraction.getId());
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), "nonexistent_option", null);

        assertFalse(result.isSuccess());
        assertEquals(CODE_UNKNOWN_OPTION, result.getCode());
    }

    @Test
    @Story("Búsqueda Activada")
    @Description("Verifica que activar búsqueda con seguridad asigna personal")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("activate_search con seguridad disponible tiene éxito")
    void testResolveAlertActivateSearch() {
        Employee securityEmployee = employeeRepository.save(Employee.builder()
                .firstName("Guard").lastName("Security").email("guard@magicworld.com")
                .phone("999").role(EmployeeRole.SECURITY)
                .status(EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2024, 1, 1))
                .build());
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(securityEmployee).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.WORKING)
                .build());

        ParkAlert alert = createActiveAlert(AlertType.LOST_CHILD, null);
        ResolutionResult result = alertResolutionService.resolveAlert(alert.getId(), OPTION_ACTIVATE_SEARCH, securityEmployee.getId());

        assertTrue(result.isSuccess());
        assertEquals("SEARCH_ACTIVATED", result.getActionTaken());
    }

    @Test
    @Story("Resolver Alerta")
    @Description("Verifica que resolver alerta libera empleados asignados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("markAlertResolved libera empleados asignados")
    void testMarkAlertResolvedReleasesEmployees() {
        ParkAlert alert = createActiveAlert(AlertType.HIGH_QUEUE, testAttraction.getId());
        DailyAssignment assignment = dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(testOperator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.ASSIGNED_TO_ALERT).assignedAlert(alert)
                .build());

        alertResolutionService.resolveAlert(alert.getId(), OPTION_ACKNOWLEDGE, null);

        DailyAssignment updated = dailyAssignmentRepository.findById(assignment.getId()).get();
        assertEquals(DailyStatus.WORKING, updated.getCurrentStatus());
        assertNull(updated.getAssignedAlert());
    }
}

