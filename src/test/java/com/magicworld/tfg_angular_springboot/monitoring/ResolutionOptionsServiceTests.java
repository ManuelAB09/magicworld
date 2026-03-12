package com.magicworld.tfg_angular_springboot.monitoring;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.monitoring.alert.*;
import com.magicworld.tfg_angular_springboot.monitoring.dto.ResolutionOption;
import com.magicworld.tfg_angular_springboot.monitoring.service.MonitoringWebSocketService;
import com.magicworld.tfg_angular_springboot.monitoring.service.ResolutionOptionsService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Monitorización del Parque")
@Feature("Servicio de Opciones de Resolución")
public class ResolutionOptionsServiceTests {

    private static final String OPTION_ADD_STAFF = "add_staff";
    private static final String OPTION_OFFER_FASTPASS = "offer_fastpass";
    private static final String OPTION_ANNOUNCE_PA = "announce_pa";
    private static final String OPTION_SEND_MEDICAL = "send_medical";
    private static final String OPTION_CALL_AMBULANCE = "call_ambulance";
    private static final String OPTION_TEMPORARY_CLOSE = "temporary_close";
    private static final String OPTION_IMMEDIATE_MAINTENANCE = "immediate_maintenance";
    private static final String OPTION_SCHEDULE_MAINTENANCE = "schedule_maintenance";
    private static final String OPTION_ACTIVATE_SEARCH = "activate_search";
    private static final String OPTION_ACKNOWLEDGE = "acknowledge";

    @Autowired
    private ResolutionOptionsService resolutionOptionsService;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DailyAssignmentRepository dailyAssignmentRepository;

    @Autowired
    private ParkAlertRepository alertRepository;

    @MockitoBean
    private MonitoringWebSocketService webSocketService;

    private Attraction testAttraction;

    @BeforeEach
    void setUp() {
        dailyAssignmentRepository.deleteAll();
        alertRepository.deleteAll();

        testAttraction = attractionRepository.save(Attraction.builder()
                .name("Options Test Ride").description("Test").photoUrl("http://test.com/img.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.MEDIUM)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(8).minimumHeight(120).minimumWeight(0)
                .mapPositionX(10.0).mapPositionY(20.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(21, 0))
                .build());
    }

    private ParkAlert buildAlert(AlertType type, Long attractionId) {
        return ParkAlert.builder()
                .alertType(type).severity(AlertSeverity.WARNING)
                .message("test").suggestion("test")
                .attractionId(attractionId)
                .timestamp(LocalDateTime.now()).isActive(true)
                .build();
    }

    @Test
    @Story("Opciones de Cola Alta")
    @Description("Verifica que HIGH_QUEUE retorna opciones de cola")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("HIGH_QUEUE retorna add_staff, offer_fastpass, announce_pa")
    void testGetResolutionOptionsHighQueue() {
        ParkAlert alert = buildAlert(AlertType.HIGH_QUEUE, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);

        assertEquals(3, options.size());
        assertTrue(options.stream().anyMatch(o -> OPTION_ADD_STAFF.equals(o.getId())));
        assertTrue(options.stream().anyMatch(o -> OPTION_OFFER_FASTPASS.equals(o.getId())));
        assertTrue(options.stream().anyMatch(o -> OPTION_ANNOUNCE_PA.equals(o.getId())));
    }

    @Test
    @Story("Opciones de Emergencia Médica")
    @Description("Verifica que MEDICAL_EMERGENCY retorna opciones médicas")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("MEDICAL_EMERGENCY retorna send_medical y call_ambulance")
    void testGetResolutionOptionsMedicalEmergency() {
        ParkAlert alert = buildAlert(AlertType.MEDICAL_EMERGENCY, null);
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);

        assertEquals(2, options.size());
        assertTrue(options.stream().anyMatch(o -> OPTION_SEND_MEDICAL.equals(o.getId())));
        assertTrue(options.stream().anyMatch(o -> OPTION_CALL_AMBULANCE.equals(o.getId())));
    }

    @Test
    @Story("Opciones de Emergencia Médica")
    @Description("Verifica que send_medical está habilitado cuando hay médicos disponibles")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("send_medical habilitado con médicos disponibles")
    void testMedicalOptionsWithAvailableMedical() {
        Employee medic = employeeRepository.save(Employee.builder()
                .firstName("Doc").lastName("Medical").email("doc@magicworld.com")
                .role(EmployeeRole.MEDICAL).status(EmployeeStatus.ACTIVE).hireDate(LocalDate.of(2024, 1, 1))
                .build());
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(medic).assignmentDate(LocalDate.now()).currentStatus(DailyStatus.WORKING).build());

        ParkAlert alert = buildAlert(AlertType.MEDICAL_EMERGENCY, null);
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);

        ResolutionOption sendMedical = options.stream().filter(o -> OPTION_SEND_MEDICAL.equals(o.getId())).findFirst().orElseThrow();
        assertTrue(sendMedical.isEnabled());
    }

    @Test
    @Story("Opciones de Queja de Cliente")
    @Description("Verifica que GUEST_COMPLAINT retorna 3 opciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GUEST_COMPLAINT retorna 3 opciones")
    void testGetResolutionOptionsGuestComplaint() {
        ParkAlert alert = buildAlert(AlertType.GUEST_COMPLAINT, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);

        assertEquals(3, options.size());
    }

    @Test
    @Story("Opciones Técnicas")
    @Description("Verifica que TECHNICAL_ISSUE retorna 4 opciones incluyendo cierre")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("TECHNICAL_ISSUE retorna 4 opciones")
    void testGetResolutionOptionsTechnicalIssue() {
        ParkAlert alert = buildAlert(AlertType.TECHNICAL_ISSUE, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);

        assertEquals(4, options.size());
        assertTrue(options.stream().anyMatch(o -> OPTION_TEMPORARY_CLOSE.equals(o.getId())));
        assertTrue(options.stream().anyMatch(o -> OPTION_IMMEDIATE_MAINTENANCE.equals(o.getId())));
    }

    @Test
    @Story("Opciones de Seguridad")
    @Description("Verifica que SAFETY_CONCERN retorna mismas opciones que técnico")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("SAFETY_CONCERN retorna 4 opciones")
    void testGetResolutionOptionsSafetyConcern() {
        ParkAlert alert = buildAlert(AlertType.SAFETY_CONCERN, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);
        assertEquals(4, options.size());
    }

    @Test
    @Story("Opciones de Mantenimiento")
    @Description("Verifica que MAINTENANCE_REQUIRED retorna 2 opciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("MAINTENANCE_REQUIRED retorna 2 opciones")
    void testGetResolutionOptionsMaintenanceRequired() {
        ParkAlert alert = buildAlert(AlertType.MAINTENANCE_REQUIRED, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);

        assertEquals(2, options.size());
        assertTrue(options.stream().anyMatch(o -> OPTION_SCHEDULE_MAINTENANCE.equals(o.getId())));
        assertTrue(options.stream().anyMatch(o -> OPTION_IMMEDIATE_MAINTENANCE.equals(o.getId())));
    }

    @Test
    @Story("Opciones de Niño Perdido")
    @Description("Verifica que LOST_CHILD retorna 3 opciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("LOST_CHILD retorna 3 opciones")
    void testGetResolutionOptionsLostChild() {
        ParkAlert alert = buildAlert(AlertType.LOST_CHILD, null);
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);

        assertEquals(3, options.size());
        assertTrue(options.stream().anyMatch(o -> OPTION_ACTIVATE_SEARCH.equals(o.getId())));
    }

    @Test
    @Story("Opciones de Bajo Personal")
    @Description("Verifica que LOW_STAFF retorna 2 opciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("LOW_STAFF retorna 2 opciones")
    void testGetResolutionOptionsLowStaff() {
        ParkAlert alert = buildAlert(AlertType.LOW_STAFF, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);
        assertEquals(2, options.size());
    }

    @Test
    @Story("Opciones de Atracción Caída")
    @Description("Verifica que ATTRACTION_DOWN retorna 3 opciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("ATTRACTION_DOWN retorna 3 opciones")
    void testGetResolutionOptionsAttractionDown() {
        ParkAlert alert = buildAlert(AlertType.ATTRACTION_DOWN, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);
        assertEquals(3, options.size());
    }

    @Test
    @Story("Opciones por Defecto")
    @Description("Verifica que tipo por defecto retorna acknowledge")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("CAPACITY_WARNING retorna opción acknowledge")
    void testGetResolutionOptionsDefault() {
        ParkAlert alert = buildAlert(AlertType.CAPACITY_WARNING, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);

        assertEquals(1, options.size());
        assertEquals(OPTION_ACKNOWLEDGE, options.get(0).getId());
    }

    @Test
    @Story("Cierre de Atracción")
    @Description("Verifica que cierre no es posible con attractionId null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("canCloseAttraction con null retorna false en opciones")
    void testCanCloseAttractionNullId() {
        ParkAlert alert = buildAlert(AlertType.TECHNICAL_ISSUE, null);
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);
        ResolutionOption closeOption = options.stream()
                .filter(o -> OPTION_TEMPORARY_CLOSE.equals(o.getId())).findFirst().orElseThrow();
        assertFalse(closeOption.isEnabled());
    }

    @Test
    @Story("Cierre de Atracción")
    @Description("Verifica que cierre no es posible si atracción está inactiva")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("canCloseAttraction con atracción inactiva retorna false")
    void testCanCloseAttractionInactive() {
        testAttraction.setIsActive(false);
        attractionRepository.save(testAttraction);

        ParkAlert alert = buildAlert(AlertType.TECHNICAL_ISSUE, testAttraction.getId());
        List<ResolutionOption> options = resolutionOptionsService.getResolutionOptions(alert);
        ResolutionOption closeOption = options.stream()
                .filter(o -> OPTION_TEMPORARY_CLOSE.equals(o.getId())).findFirst().orElseThrow();
        assertFalse(closeOption.isEnabled());
    }
}

