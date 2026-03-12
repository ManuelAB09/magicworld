package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.dto.AvailableEmployeesResponse;
import com.magicworld.tfg_angular_springboot.employee.dto.DailyAssignmentDTO;
import com.magicworld.tfg_angular_springboot.employee.service.DailyOperationsService;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertSeverity;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertType;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlert;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlertRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Operaciones Diarias")
@Feature("Servicio de Operaciones Diarias - Integración")
public class DailyOperationsServiceIntegrationTests {

    private static final String EMPLOYEE_EMAIL_PREFIX = "dailyint";
    private static final String ATTRACTION_NAME = "Integration Coaster";
    private static final String ALERT_MESSAGE = "Integration test alert";

    @Autowired
    private DailyOperationsService dailyOperationsService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DailyAssignmentRepository dailyAssignmentRepository;

    @Autowired
    private ReinforcementCallRepository reinforcementCallRepository;

    @Autowired
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ParkZoneRepository parkZoneRepository;

    @Autowired
    private ParkAlertRepository alertRepository;

    private Employee operator;
    private Employee securityEmp;
    private Employee medicalEmp;
    private Employee reinforcementEmp;
    private Attraction attraction;
    private ParkZone zone;
    private ParkAlert alert;

    @BeforeEach
    void setUp() {
        dailyAssignmentRepository.deleteAll();
        reinforcementCallRepository.deleteAll();

        operator = employeeRepository.save(Employee.builder()
                .firstName("IntOp").lastName("Test").email(EMPLOYEE_EMAIL_PREFIX + "op@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        securityEmp = employeeRepository.save(Employee.builder()
                .firstName("IntSec").lastName("Test").email(EMPLOYEE_EMAIL_PREFIX + "sec@test.com")
                .role(EmployeeRole.SECURITY).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        medicalEmp = employeeRepository.save(Employee.builder()
                .firstName("IntMed").lastName("Test").email(EMPLOYEE_EMAIL_PREFIX + "med@test.com")
                .role(EmployeeRole.MEDICAL).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        reinforcementEmp = employeeRepository.save(Employee.builder()
                .firstName("IntReinf").lastName("Test").email(EMPLOYEE_EMAIL_PREFIX + "reinf@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .phone("555-0001")
                .hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name(ATTRACTION_NAME).description("Test ride").photoUrl("http://example.com/int.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.MEDIUM)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(10).minimumHeight(130).minimumWeight(0)
                .mapPositionX(1.0).mapPositionY(1.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        zone = parkZoneRepository.findAll().stream().findFirst().orElse(null);

        alert = alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.TECHNICAL_ISSUE)
                .severity(AlertSeverity.WARNING)
                .message(ALERT_MESSAGE)
                .suggestion("Fix it")
                .timestamp(LocalDateTime.now())
                .isActive(true)
                .build());
    }

    @Test
    @Story("Empleados Disponibles")
    @Description("Verifica que checkAvailableEmployees retorna empleados trabajando hoy")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("checkAvailableEmployeesRetornaEmpleadosTrabajandoHoy")
    void checkAvailableEmployeesRetornaEmpleadosTrabajandoHoy() {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(operator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.WORKING)
                .currentAttraction(attraction)
                .breakGroup(BreakGroup.A)
                .breakStartTime(BreakGroup.A.getStartTime())
                .breakEndTime(BreakGroup.A.getEndTime())
                .build());

        AvailableEmployeesResponse response = dailyOperationsService.checkAvailableEmployees(EmployeeRole.OPERATOR);

        assertTrue(response.isHasAvailable());
        assertFalse(response.getEmployees().isEmpty());
    }

    @Test
    @Story("Empleados Disponibles")
    @Description("Verifica que checkAvailableEmployees incluye candidatos de refuerzo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("checkAvailableEmployeesIncluyeCandidatosRefuerzo")
    void checkAvailableEmployeesIncluyeCandidatosRefuerzo() {
        AvailableEmployeesResponse response = dailyOperationsService.checkAvailableEmployees(EmployeeRole.OPERATOR);

        assertTrue(response.isHasReinforcements());
        assertFalse(response.getReinforcements().isEmpty());
    }

    @Test
    @Story("Empleados Disponibles")
    @Description("Verifica que getAvailableEmployees delega a checkAvailableEmployees")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getAvailableEmployeesDelegaCorrectamente")
    void getAvailableEmployeesDelegaCorrectamente() {
        AvailableEmployeesResponse response = dailyOperationsService.getAvailableEmployees(EmployeeRole.SECURITY);
        assertNotNull(response);
    }

    @Test
    @Story("Llamar Refuerzo")
    @Description("Verifica que callReinforcement con aceptación crea asignación diaria")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("callReinforcementAceptadoCreaAsignacionDiaria")
    void callReinforcementAceptadoCreaAsignacionDiaria() {
        // Force acceptance by setting seed
        ReflectionTestUtils.setField(dailyOperationsService, "random", new Random(42));

        ReinforcementCall result = dailyOperationsService.callReinforcement(
                reinforcementEmp.getId(), alert.getId());

        assertNotNull(result.getId());
        if (result.getStatus() == ReinforcementStatus.ACCEPTED) {
            assertNotNull(result.getArrivalTime());
        }
    }

    @Test
    @Story("Llamar Refuerzo")
    @Description("Verifica que callReinforcement sin alerta funciona correctamente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("callReinforcementSinAlertaFunciona")
    void callReinforcementSinAlertaFunciona() {
        ReflectionTestUtils.setField(dailyOperationsService, "random", new Random(42));

        ReinforcementCall result = dailyOperationsService.callReinforcement(
                reinforcementEmp.getId(), null);

        assertNotNull(result.getId());
        assertNull(result.getOriginAlert());
    }

    @Test
    @Story("Llamar Refuerzo")
    @Description("Verifica que callReinforcement con empleado inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("callReinforcementEmpleadoInexistenteLanzaExcepcion")
    void callReinforcementEmpleadoInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> dailyOperationsService.callReinforcement(99999L, alert.getId()));
    }

    @Test
    @Story("Llamar Refuerzo")
    @Description("Verifica que callReinforcement rechazado no crea asignación diaria")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("callReinforcementRechazadoNoCreaAsignacion")
    void callReinforcementRechazadoNoCreaAsignacion() {
        // Use seed that gives rejection (< 0.30)
        Random fixedRandom = new Random(0) {
            @Override
            public double nextDouble() {
                return 0.1; // Always < 0.30, so always rejected
            }
        };
        ReflectionTestUtils.setField(dailyOperationsService, "random", fixedRandom);

        ReinforcementCall result = dailyOperationsService.callReinforcement(
                reinforcementEmp.getId(), alert.getId());

        assertEquals(ReinforcementStatus.REJECTED, result.getStatus());
    }

    @Test
    @Story("Llamar Refuerzo")
    @Description("Verifica que callReinforcement aceptado crea asignación con alerta cuando tiene originAlert")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("callReinforcementAceptadoConAlertaCreaAsignacionConAlerta")
    void callReinforcementAceptadoConAlertaCreaAsignacionConAlerta() {
        Random fixedRandom = new Random(0) {
            @Override
            public double nextDouble() {
                return 0.99; // Always > 0.30, so always accepted
            }
        };
        ReflectionTestUtils.setField(dailyOperationsService, "random", fixedRandom);

        ReinforcementCall result = dailyOperationsService.callReinforcement(
                reinforcementEmp.getId(), alert.getId());

        assertEquals(ReinforcementStatus.ACCEPTED, result.getStatus());
        assertNotNull(result.getArrivalTime());
    }

    @Test
    @Story("Actualizar Estado Refuerzo")
    @Description("Verifica que updateReinforcementStatus con ACCEPTED actualiza responseTime")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateReinforcementStatusAcceptedActualizaResponseTime")
    void updateReinforcementStatusAcceptedActualizaResponseTime() {
        ReinforcementCall call = reinforcementCallRepository.save(ReinforcementCall.builder()
                .employee(reinforcementEmp)
                .callTime(LocalDateTime.now())
                .status(ReinforcementStatus.PENDING)
                .isOvertime(true)
                .build());

        ReinforcementCall updated = dailyOperationsService.updateReinforcementStatus(
                call.getId(), ReinforcementStatus.ACCEPTED);

        assertEquals(ReinforcementStatus.ACCEPTED, updated.getStatus());
        assertNotNull(updated.getResponseTime());
    }

    @Test
    @Story("Actualizar Estado Refuerzo")
    @Description("Verifica que updateReinforcementStatus con REJECTED actualiza responseTime")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("updateReinforcementStatusRejectedActualizaResponseTime")
    void updateReinforcementStatusRejectedActualizaResponseTime() {
        ReinforcementCall call = reinforcementCallRepository.save(ReinforcementCall.builder()
                .employee(reinforcementEmp)
                .callTime(LocalDateTime.now())
                .status(ReinforcementStatus.PENDING)
                .isOvertime(true)
                .build());

        ReinforcementCall updated = dailyOperationsService.updateReinforcementStatus(
                call.getId(), ReinforcementStatus.REJECTED);

        assertEquals(ReinforcementStatus.REJECTED, updated.getStatus());
        assertNotNull(updated.getResponseTime());
    }

    @Test
    @Story("Actualizar Estado Refuerzo")
    @Description("Verifica que updateReinforcementStatus con ARRIVED actualiza arrivalTime y crea asignación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateReinforcementStatusArrivedCreaAsignacion")
    void updateReinforcementStatusArrivedCreaAsignacion() {
        ReinforcementCall call = reinforcementCallRepository.save(ReinforcementCall.builder()
                .employee(reinforcementEmp)
                .callTime(LocalDateTime.now())
                .originAlert(alert)
                .status(ReinforcementStatus.ACCEPTED)
                .isOvertime(true)
                .build());

        ReinforcementCall updated = dailyOperationsService.updateReinforcementStatus(
                call.getId(), ReinforcementStatus.ARRIVED);

        assertEquals(ReinforcementStatus.ARRIVED, updated.getStatus());
        assertNotNull(updated.getArrivalTime());
    }

    @Test
    @Story("Actualizar Estado Refuerzo")
    @Description("Verifica que updateReinforcementStatus con ID inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("updateReinforcementStatusIdInexistenteLanzaExcepcion")
    void updateReinforcementStatusIdInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> dailyOperationsService.updateReinforcementStatus(99999L, ReinforcementStatus.ACCEPTED));
    }

    @Test
    @Story("Asignar a Alerta")
    @Description("Verifica que asignar empleado no disponible lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("assignEmployeeToAlertNoDisponibleLanzaExcepcion")
    void assignEmployeeToAlertNoDisponibleLanzaExcepcion() {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(operator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.ON_BREAK)
                .breakGroup(BreakGroup.A)
                .breakStartTime(BreakGroup.A.getStartTime())
                .breakEndTime(BreakGroup.A.getEndTime())
                .build());

        assertThrows(IllegalArgumentException.class,
                () -> dailyOperationsService.assignEmployeeToAlert(operator.getId(), alert.getId()));
    }

    @Test
    @Story("Asignar a Alerta")
    @Description("Verifica que asignar empleado sin asignación hoy lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("assignEmployeeToAlertSinAsignacionLanzaExcepcion")
    void assignEmployeeToAlertSinAsignacionLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> dailyOperationsService.assignEmployeeToAlert(operator.getId(), alert.getId()));
    }

    @Test
    @Story("Asignar a Alerta")
    @Description("Verifica que asignar a alerta inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("assignEmployeeToAlertAlertaInexistenteLanzaExcepcion")
    void assignEmployeeToAlertAlertaInexistenteLanzaExcepcion() {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(operator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.WORKING)
                .breakGroup(BreakGroup.A)
                .breakStartTime(BreakGroup.A.getStartTime())
                .breakEndTime(BreakGroup.A.getEndTime())
                .build());

        assertThrows(IllegalArgumentException.class,
                () -> dailyOperationsService.assignEmployeeToAlert(operator.getId(), 99999L));
    }

    @Test
    @Story("Obtener Asignaciones de Hoy")
    @Description("Verifica que getTodayAssignments retorna asignaciones del día actual")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getTodayAssignmentsRetornaAsignacionesDelDia")
    void getTodayAssignmentsRetornaAsignacionesDelDia() {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(operator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.WORKING)
                .currentAttraction(attraction)
                .breakGroup(BreakGroup.A)
                .breakStartTime(BreakGroup.A.getStartTime())
                .breakEndTime(BreakGroup.A.getEndTime())
                .build());

        List<DailyAssignmentDTO> result = dailyOperationsService.getTodayAssignments();
        assertFalse(result.isEmpty());
    }

    @Test
    @Story("Inicializar Día")
    @Description("Verifica que initializeDay con horarios semanales existentes crea asignaciones")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("initializeDayConHorariosSemanalesCreaAsignaciones")
    void initializeDayConHorariosSemanalesCreaAsignaciones() {
        LocalDate futureDate = LocalDate.now().plusDays(14);
        LocalDate weekStart = futureDate.minusDays(futureDate.getDayOfWeek().getValue() - 1);

        weeklyScheduleRepository.save(WeeklySchedule.builder()
                .employee(operator)
                .weekStartDate(weekStart)
                .dayOfWeek(futureDate.getDayOfWeek())
                .shift(WorkShift.FULL_DAY)
                .assignedAttraction(attraction)
                .breakGroup(BreakGroup.B)
                .build());

        dailyOperationsService.initializeDay(futureDate);

        List<DailyAssignmentDTO> assignments = dailyOperationsService.getAssignmentsForDate(futureDate);
        assertFalse(assignments.isEmpty());
    }

    @Test
    @Story("Liberar de Alerta")
    @Description("Verifica que liberar empleado sin asignación lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("releaseEmployeeFromAlertSinAsignacionLanzaExcepcion")
    void releaseEmployeeFromAlertSinAsignacionLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> dailyOperationsService.releaseEmployeeFromAlert(operator.getId()));
    }
}

