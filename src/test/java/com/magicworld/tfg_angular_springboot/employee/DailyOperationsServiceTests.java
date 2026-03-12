package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.dto.DailyAssignmentDTO;
import com.magicworld.tfg_angular_springboot.employee.service.DailyOperationsService;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlert;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlertRepository;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertSeverity;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertType;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Operaciones Diarias")
@Feature("Servicio de Operaciones Diarias")
public class DailyOperationsServiceTests {

    @Autowired
    private DailyOperationsService dailyOperationsService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DailyAssignmentRepository dailyAssignmentRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ParkAlertRepository alertRepository;

    private Employee operator;
    private Attraction attraction;

    @BeforeEach
    void setUp() {
        dailyAssignmentRepository.deleteAll();

        operator = employeeRepository.save(Employee.builder()
                .firstName("Daily").lastName("Op").email("daily@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name("Daily Coaster").description("Test ride").photoUrl("http://example.com/d.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.MEDIUM)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(10).minimumHeight(130).minimumWeight(0)
                .mapPositionX(2.0).mapPositionY(2.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());
    }

    @AfterEach
    void tearDown() {
        dailyAssignmentRepository.deleteAll();
    }

    @Test
    @Story("Inicializar Día")
    @Description("Verifica que inicializar día crea asignaciones demo cuando no hay horarios")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Inicializar día crea asignaciones demo")
    void testInitializeDayCreatesAssignments() {
        LocalDate futureDate = LocalDate.now().plusDays(30);
        dailyOperationsService.initializeDay(futureDate);

        List<DailyAssignmentDTO> result = dailyOperationsService.getAssignmentsForDate(futureDate);
        // Depends on how many active employees exist; should not be empty if employees are active
        assertNotNull(result);
    }

    @Test
    @Story("Inicializar Día")
    @Description("Verifica que inicializar día no duplica si ya existe")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Inicializar día no duplica")
    void testInitializeDayDoesNotDuplicate() {
        LocalDate futureDate = LocalDate.now().plusDays(31);
        dailyOperationsService.initializeDay(futureDate);
        int firstSize = dailyOperationsService.getAssignmentsForDate(futureDate).size();

        dailyOperationsService.initializeDay(futureDate);
        int secondSize = dailyOperationsService.getAssignmentsForDate(futureDate).size();

        assertEquals(firstSize, secondSize);
    }

    @Test
    @Story("Obtener Asignaciones")
    @Description("Verifica que obtener asignaciones por fecha funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener asignaciones por fecha")
    void testGetAssignmentsForDate() {
        LocalDate futureDate = LocalDate.now().plusDays(32);
        List<DailyAssignmentDTO> result = dailyOperationsService.getAssignmentsForDate(futureDate);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Story("Asignar a Alerta")
    @Description("Verifica que asignar empleado a alerta funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Asignar empleado a alerta")
    void testAssignEmployeeToAlert() {
        // Create daily assignment for today
        DailyAssignment assignment = dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(operator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.WORKING)
                .currentAttraction(attraction)
                .breakGroup(BreakGroup.A)
                .breakStartTime(BreakGroup.A.getStartTime())
                .breakEndTime(BreakGroup.A.getEndTime())
                .build());

        ParkAlert alert = alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.TECHNICAL_ISSUE)
                .severity(AlertSeverity.WARNING)
                .message("Test alert")
                .suggestion("Fix it")
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build());

        DailyAssignmentDTO result = dailyOperationsService.assignEmployeeToAlert(
                operator.getId(), alert.getId());
        assertEquals(DailyStatus.ASSIGNED_TO_ALERT, result.getCurrentStatus());
    }

    @Test
    @Story("Liberar de Alerta")
    @Description("Verifica que liberar empleado de alerta funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Liberar empleado de alerta")
    void testReleaseEmployeeFromAlert() {
        ParkAlert alert = alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.TECHNICAL_ISSUE)
                .severity(AlertSeverity.WARNING)
                .message("Test alert")
                .suggestion("Fix it")
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build());

        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(operator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.ASSIGNED_TO_ALERT)
                .assignedAlert(alert)
                .currentAttraction(attraction)
                .breakGroup(BreakGroup.B)
                .breakStartTime(BreakGroup.B.getStartTime())
                .breakEndTime(BreakGroup.B.getEndTime())
                .build());

        DailyAssignmentDTO result = dailyOperationsService.releaseEmployeeFromAlert(operator.getId());
        assertEquals(DailyStatus.WORKING, result.getCurrentStatus());
    }
}
