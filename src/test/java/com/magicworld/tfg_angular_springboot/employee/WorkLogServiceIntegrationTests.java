package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.dto.*;
import com.magicworld.tfg_angular_springboot.employee.service.ScheduleService;
import com.magicworld.tfg_angular_springboot.employee.service.WorkLogService;
import io.qameta.allure.*;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Horas de Trabajo")
@Feature("Servicio de Registro de Horas - Integración")
public class WorkLogServiceIntegrationTests {

    private static final String EMAIL_PREFIX = "wlint";
    private static final BigDecimal FULL_DAY_HOURS = new BigDecimal("8.00");
    private static final BigDecimal PARTIAL_HOURS = new BigDecimal("3.00");
    private static final BigDecimal OVERTIME_HOURS = new BigDecimal("4.00");
    private static final String ADMIN_USERNAME = "admin";

    @Autowired private WorkLogService workLogService;
    @Autowired private ScheduleService scheduleService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private WeeklyScheduleRepository scheduleRepository;
    @Autowired private WorkLogRepository workLogRepository;
    @Autowired private AttractionRepository attractionRepository;
    @Autowired private ParkZoneRepository zoneRepository;

    private Employee employee;
    private Employee securityEmp;
    private Attraction attraction;
    private ParkZone zone;
    private LocalDate futureDate;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        workLogRepository.deleteAll();
        scheduleRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = employeeRepository.save(Employee.builder()
                .firstName("WlInt").lastName("Test").email(EMAIL_PREFIX + "op@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        securityEmp = employeeRepository.save(Employee.builder()
                .firstName("WlSec").lastName("Test").email(EMAIL_PREFIX + "sec@test.com")
                .role(EmployeeRole.SECURITY).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name("WL Int Coaster").description("Test").photoUrl("http://example.com/wl.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(1.0).mapPositionY(1.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        zone = zoneRepository.findAll().stream().findFirst().orElse(null);

        LocalDate today = LocalDate.now();
        monday = today.plusDays(8 - today.getDayOfWeek().getValue());
        futureDate = monday;

        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(employee.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());
    }

    @Test
    @Story("Ausencia Parcial")
    @Description("Verifica que registrar ausencia parcial crea entrada correcta")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("addPartialAbsenceCreaEntradaCorrecta")
    void addPartialAbsenceCreaEntradaCorrecta() {
        WorkLogEntryDTO result = workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.PARTIAL_ABSENCE)
                .hoursAffected(PARTIAL_HOURS)
                .reason("Doctor appointment")
                .build(), ADMIN_USERNAME);

        assertEquals(WorkLogAction.PARTIAL_ABSENCE, result.getAction());
        assertEquals(0, PARTIAL_HOURS.compareTo(result.getHoursAffected()));
    }

    @Test
    @Story("Eliminar Día Programado")
    @Description("Verifica que REMOVE_SCHEDULED_DAY elimina del horario")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("removeScheduledDayEliminaDelHorario")
    void removeScheduledDayEliminaDelHorario() {
        WorkLogEntryDTO result = workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.REMOVE_SCHEDULED_DAY)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Day off")
                .build(), ADMIN_USERNAME);

        assertEquals(WorkLogAction.REMOVE_SCHEDULED_DAY, result.getAction());
    }

    @Test
    @Story("Eliminar Día Programado")
    @Description("Verifica que REMOVE_SCHEDULED_DAY sin programación lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("removeScheduledDaySinProgramacionLanzaExcepcion")
    void removeScheduledDaySinProgramacionLanzaExcepcion() {
        LocalDate unscheduledDate = monday.plusDays(5); // Saturday, not scheduled

        assertThrows(IllegalArgumentException.class, () ->
                workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                        .employeeId(employee.getId())
                        .targetDate(unscheduledDate)
                        .action(WorkLogAction.REMOVE_SCHEDULED_DAY)
                        .hoursAffected(FULL_DAY_HOURS)
                        .reason("Not needed")
                        .build(), ADMIN_USERNAME));
    }

    @Test
    @Story("Ausencia Parcial")
    @Description("Verifica que PARTIAL_ABSENCE sin programación lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("partialAbsenceSinProgramacionLanzaExcepcion")
    void partialAbsenceSinProgramacionLanzaExcepcion() {
        LocalDate unscheduledDate = monday.plusDays(5);

        assertThrows(IllegalArgumentException.class, () ->
                workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                        .employeeId(employee.getId())
                        .targetDate(unscheduledDate)
                        .action(WorkLogAction.PARTIAL_ABSENCE)
                        .hoursAffected(PARTIAL_HOURS)
                        .reason("Not needed")
                        .build(), ADMIN_USERNAME));
    }

    @Test
    @Story("Quitar Ausencia")
    @Description("Verifica que REMOVE_ABSENCE restaura el horario desde snapshot")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("removeAbsenceRestauraHorarioDesdeSnapshot")
    void removeAbsenceRestauraHorarioDesdeSnapshot() {
        // Register absence first
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Sick leave")
                .build(), ADMIN_USERNAME);

        // Remove absence
        WorkLogEntryDTO result = workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.REMOVE_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Recovered")
                .build(), ADMIN_USERNAME);

        assertEquals(WorkLogAction.REMOVE_ABSENCE, result.getAction());
    }

    @Test
    @Story("Registrar Ausencia")
    @Description("Verifica que ADD_ABSENCE ignora horas manuales y usa duración real de atracción para operadores")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("addAbsenceOperadorUsaDuracionAtraccion")
    void addAbsenceOperadorUsaDuracionAtraccion() {
        Attraction longAttraction = attractionRepository.save(Attraction.builder()
                .name("WL Long Ride").description("Long duration").photoUrl("http://example.com/wl-long.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(2.0).mapPositionY(2.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(21, 0))
                .build());

        LocalDate tuesday = monday.plusDays(1);
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(employee.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(longAttraction.getId())
                .breakGroup(BreakGroup.B)
                .build());

        WorkLogEntryDTO result = workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(tuesday)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(new BigDecimal("1.00"))
                .reason("Sick leave")
                .build(), ADMIN_USERNAME);

        assertEquals(0, new BigDecimal("12.00").compareTo(result.getHoursAffected()));
    }

    @Test
    @Story("Registrar Ausencia")
    @Description("Verifica que ADD_ABSENCE para roles no operador usa 8h por defecto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("addAbsenceNoOperadorUsaOchoHoras")
    void addAbsenceNoOperadorUsaOchoHoras() {
        if (zone == null) {
            return;
        }

        LocalDate tuesday = monday.plusDays(1);
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(securityEmp.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedZoneId(zone.getId())
                .breakGroup(BreakGroup.A)
                .build());

        WorkLogEntryDTO result = workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(securityEmp.getId())
                .targetDate(tuesday)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(new BigDecimal("2.00"))
                .reason("Medical leave")
                .build(), ADMIN_USERNAME);

        assertEquals(0, FULL_DAY_HOURS.compareTo(result.getHoursAffected()));
    }

    @Test
    @Story("Resumen de Empleado")
    @Description("Verifica que getEmployeeSummary con ausencia reduce días trabajados")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getEmployeeSummaryConAusenciaReduceDiasTrabajados")
    void getEmployeeSummaryConAusenciaReduceDiasTrabajados() {
        EmployeeHoursSummaryDTO summaryBefore = workLogService.getEmployeeSummary(
                employee.getId(), futureDate, futureDate);
        int daysBefore = summaryBefore.getScheduledDays();

        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Sick")
                .build(), ADMIN_USERNAME);

        EmployeeHoursSummaryDTO summaryAfter = workLogService.getEmployeeSummary(
                employee.getId(), futureDate, futureDate);

        assertTrue(summaryAfter.getAbsences() > 0);
    }

    @Test
    @Story("Resumen de Empleado")
    @Description("Verifica que, tras una falta total, los días trabajados quedan alineados con los programados restantes")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getEmployeeSummaryConFaltaTotalMantieneDiasAlineados")
    void getEmployeeSummaryConFaltaTotalMantieneDiasAlineados() {
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(employee.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.B)
                .build());

        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(employee.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.C)
                .build());

        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(employee.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.THURSDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.D)
                .build());

        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(employee.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());

        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(monday)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Sick")
                .build(), ADMIN_USERNAME);

        EmployeeHoursSummaryDTO summary = workLogService.getEmployeeSummary(
                employee.getId(), monday, monday.plusDays(4));

        assertEquals(1, summary.getAbsences());
        assertEquals(4, summary.getScheduledDays());
        assertEquals(4, summary.getWorkedDays());
    }

    @Test
    @Story("Resumen de Empleado")
    @Description("Verifica que getEmployeeSummary con horas extra suma correctamente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getEmployeeSummaryConHorasExtraSumaCorrectamente")
    void getEmployeeSummaryConHorasExtraSumaCorrectamente() {
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(OVERTIME_HOURS)
                .isOvertime(true)
                .reason("Extra coverage")
                .build(), ADMIN_USERNAME);

        EmployeeHoursSummaryDTO summary = workLogService.getEmployeeSummary(
                employee.getId(), futureDate, futureDate);

        assertTrue(summary.getTotalHoursWorked().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Story("Resumen de Empleado")
    @Description("Verifica que una falta total elimina las horas extra del día y que REMOVE_ABSENCE las restaura")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("addAbsenceCompensaHorasExtraYRemoveAbsenceLasRestaura")
    void addAbsenceCompensaHorasExtraYRemoveAbsenceLasRestaura() {
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(OVERTIME_HOURS)
                .isOvertime(true)
                .reason("Extra coverage")
                .build(), ADMIN_USERNAME);

        EmployeeHoursSummaryDTO beforeAbsence = workLogService.getEmployeeSummary(
                employee.getId(), futureDate, futureDate);
        assertEquals(0, new BigDecimal("12.00").compareTo(beforeAbsence.getTotalHoursWorked()));

        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Sick")
                .build(), ADMIN_USERNAME);

        EmployeeHoursSummaryDTO duringAbsence = workLogService.getEmployeeSummary(
                employee.getId(), futureDate, futureDate);
        assertEquals(0, BigDecimal.ZERO.compareTo(duringAbsence.getTotalHoursWorked()));
        assertEquals(1, duringAbsence.getAbsences());

        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.REMOVE_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Recovered")
                .build(), ADMIN_USERNAME);

        EmployeeHoursSummaryDTO afterRemovingAbsence = workLogService.getEmployeeSummary(
                employee.getId(), futureDate, futureDate);
        assertEquals(0, new BigDecimal("12.00").compareTo(afterRemovingAbsence.getTotalHoursWorked()));
        assertEquals(0, afterRemovingAbsence.getAbsences());
    }

    @Test
    @Story("Resumen de Empleado")
    @Description("Verifica que getEmployeeSummary con ausencia parcial descuenta horas")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getEmployeeSummaryConAusenciaParcialDescuentaHoras")
    void getEmployeeSummaryConAusenciaParcialDescuentaHoras() {
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.PARTIAL_ABSENCE)
                .hoursAffected(PARTIAL_HOURS)
                .reason("Doctor visit")
                .build(), ADMIN_USERNAME);

        EmployeeHoursSummaryDTO summary = workLogService.getEmployeeSummary(
                employee.getId(), futureDate, futureDate);

        assertNotNull(summary);
        assertNotNull(summary.getAdjustments());
        assertFalse(summary.getAdjustments().isEmpty());
    }

    @Test
    @Story("Resumen de Empleado")
    @Description("Verifica que getEmployeeSummary con empleado inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getEmployeeSummaryEmpleadoInexistenteLanzaExcepcion")
    void getEmployeeSummaryEmpleadoInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                workLogService.getEmployeeSummary(99999L, futureDate, futureDate));
    }

    @Test
    @Story("Validación")
    @Description("Verifica que ADD_ABSENCE sin programación lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("addAbsenceSinProgramacionLanzaExcepcion")
    void addAbsenceSinProgramacionLanzaExcepcion() {
        LocalDate unscheduledDate = monday.plusDays(5);

        assertThrows(IllegalArgumentException.class, () ->
                workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                        .employeeId(employee.getId())
                        .targetDate(unscheduledDate)
                        .action(WorkLogAction.ADD_ABSENCE)
                        .hoursAffected(FULL_DAY_HOURS)
                        .reason("Sick")
                        .build(), ADMIN_USERNAME));
    }

    @Test
    @Story("Horas Efectivas")
    @Description("Verifica que calculateEffectiveHours usa horas de atracción si disponibles")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("calculateEffectiveHoursUsaHorasAtraccion")
    void calculateEffectiveHoursUsaHorasAtraccion() {
        WeeklySchedule ws = WeeklySchedule.builder()
                .employee(employee)
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttraction(attraction)
                .breakGroup(BreakGroup.A)
                .build();

        BigDecimal hours = WorkLogService.calculateEffectiveHours(ws);
        assertTrue(hours.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(hours.compareTo(new BigDecimal("8.00")) <= 0);
    }

    @Test
    @Story("Horas Efectivas")
    @Description("Verifica que calculateEffectiveHours no limita a 8h cuando la atracción dura más")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("calculateEffectiveHoursSinCapParaAtraccionesLargas")
    void calculateEffectiveHoursSinCapParaAtraccionesLargas() {
        Attraction longAttraction = attractionRepository.save(Attraction.builder()
                .name("WL Very Long Ride").description("Long duration").photoUrl("http://example.com/wl-very-long.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(5.0).mapPositionY(5.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(21, 0))
                .build());

        WeeklySchedule ws = WeeklySchedule.builder()
                .employee(employee)
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttraction(longAttraction)
                .breakGroup(BreakGroup.A)
                .build();

        BigDecimal hours = WorkLogService.calculateEffectiveHours(ws);
        assertEquals(0, new BigDecimal("12.00").compareTo(hours));
    }

    @Test
    @Story("Horas Efectivas")
    @Description("Verifica que calculateEffectiveHours sin atracción usa horas de turno")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("calculateEffectiveHoursSinAtraccionUsaHorasTurno")
    void calculateEffectiveHoursSinAtraccionUsaHorasTurno() {
        WeeklySchedule ws = WeeklySchedule.builder()
                .employee(employee)
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .breakGroup(BreakGroup.A)
                .build();

        BigDecimal hours = WorkLogService.calculateEffectiveHours(ws);
        assertEquals(0, new BigDecimal("8.00").compareTo(hours));
    }

    @Test
    @Story("Horas Efectivas")
    @Description("Verifica que calculateEffectiveHours con turno MORNING calcula correctamente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("calculateEffectiveHoursConTurnoMorningCalculaCorrectamente")
    void calculateEffectiveHoursConTurnoMorningCalculaCorrectamente() {
        WeeklySchedule ws = WeeklySchedule.builder()
                .employee(employee)
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.MORNING)
                .breakGroup(BreakGroup.A)
                .build();

        BigDecimal hours = WorkLogService.calculateEffectiveHours(ws);
        assertEquals(0, new BigDecimal("8.00").compareTo(hours));
    }
}

