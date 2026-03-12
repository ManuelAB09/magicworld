package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.employee.dto.CreateScheduleRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.EmployeeHoursSummaryDTO;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryDTO;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryRequest;
import com.magicworld.tfg_angular_springboot.employee.service.ScheduleService;
import com.magicworld.tfg_angular_springboot.employee.service.WorkLogService;
import com.magicworld.tfg_angular_springboot.attraction.*;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Horas de Trabajo")
@Feature("Servicio de Registro de Horas")
public class WorkLogServiceTests {

    @Autowired
    private WorkLogService workLogService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WeeklyScheduleRepository scheduleRepository;

    @Autowired
    private WorkLogRepository workLogRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    private Employee employee;
    private LocalDate futureDate;
    private Attraction attraction;

    @BeforeEach
    void setUp() {
        workLogRepository.deleteAll();
        scheduleRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = employeeRepository.save(Employee.builder()
                .firstName("John").lastName("Doe").email("wl@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name("Log Coaster").description("A test ride").photoUrl("http://example.com/p.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(1.0).mapPositionY(1.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        // Find the next Monday
        LocalDate today = LocalDate.now();
        LocalDate monday = today.plusDays(8 - today.getDayOfWeek().getValue());
        futureDate = monday; // Monday of next week

        // Create a schedule entry for the employee on futureDate
        DayOfWeek dow = futureDate.getDayOfWeek();
        LocalDate weekStart = futureDate.minusDays(futureDate.getDayOfWeek().getValue() - 1);
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(employee.getId())
                .weekStartDate(weekStart)
                .dayOfWeek(dow)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());
    }

    @AfterEach
    void tearDown() {
        workLogRepository.deleteAll();
        scheduleRepository.deleteAll();
    }

    @Test
    @Story("Añadir Horas Extra")
    @Description("Verifica que añadir horas extra crea entrada en el log")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Añadir horas extra crea entrada")
    void testAddOvertimeEntry() {
        WorkLogEntryRequest request = WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(new BigDecimal("4.00"))
                .isOvertime(true)
                .reason("Extra coverage needed")
                .build();

        WorkLogEntryDTO result = workLogService.addWorkLogEntry(request, "admin");
        assertNotNull(result.getId());
        assertEquals(WorkLogAction.ADD_OVERTIME_HOURS, result.getAction());
    }

    @Test
    @Story("Validación de Fecha")
    @Description("Verifica que añadir entrada con fecha pasada lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Fecha pasada lanza excepción")
    void testPastDateThrows() {
        WorkLogEntryRequest request = WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(LocalDate.now().minusDays(1))
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(new BigDecimal("2.00"))
                .reason("Test")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> workLogService.addWorkLogEntry(request, "admin"));
    }

    @Test
    @Story("Registrar Ausencia")
    @Description("Verifica que registrar ausencia de empleado programado funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Registrar ausencia funciona")
    void testAddAbsence() {
        WorkLogEntryRequest request = WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(new BigDecimal("8.00"))
                .reason("Sick leave")
                .build();

        WorkLogEntryDTO result = workLogService.addWorkLogEntry(request, "admin");
        assertNotNull(result.getId());
        assertEquals(WorkLogAction.ADD_ABSENCE, result.getAction());
    }

    @Test
    @Story("Historial")
    @Description("Verifica que obtener historial retorna entradas del empleado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener historial retorna entradas")
    void testGetWorkLogHistory() {
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(new BigDecimal("3.00"))
                .reason("Coverage")
                .build(), "admin");

        List<WorkLogEntryDTO> history = workLogService.getWorkLogHistory(
                employee.getId(), futureDate, futureDate);
        assertEquals(1, history.size());
    }

    @Test
    @Story("Resumen de Empleado")
    @Description("Verifica que obtener resumen de horas del empleado funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener resumen de horas")
    void testGetEmployeeSummary() {
        EmployeeHoursSummaryDTO summary = workLogService.getEmployeeSummary(
                employee.getId(), futureDate, futureDate);

        assertNotNull(summary);
        assertEquals(employee.getId(), summary.getEmployeeId());
        assertTrue(summary.getScheduledDays() >= 0);
    }

    @Test
    @Story("Validación de Ausencia")
    @Description("Verifica que quitar ausencia sin ausencia activa lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Quitar ausencia sin ausencia lanza excepción")
    void testRemoveAbsenceWithoutAbsenceThrows() {
        WorkLogEntryRequest request = WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.REMOVE_ABSENCE)
                .hoursAffected(new BigDecimal("8.00"))
                .reason("Undo absence")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> workLogService.addWorkLogEntry(request, "admin"));
    }
}
