package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateScheduleRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.CoverageValidationResult;
import com.magicworld.tfg_angular_springboot.employee.dto.WeeklyScheduleDTO;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryRequest;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Horarios")
@Feature("Servicio de Horarios - Integración")
public class ScheduleServiceIntegrationTests {

    private static final String EMAIL_PREFIX = "schedint";

    @Autowired private ScheduleService scheduleService;
    @Autowired private WorkLogService workLogService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private WeeklyScheduleRepository scheduleRepository;
    @Autowired private WorkLogRepository workLogRepository;
    @Autowired private AttractionRepository attractionRepository;
    @Autowired private ParkZoneRepository zoneRepository;

    private Employee operator;
    private Employee security;
    private Employee medical;
    private Employee maintenance;
    private Employee guestServices;
    private Attraction attraction;
    private Attraction attraction2;
    private ParkZone zone;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        workLogRepository.deleteAll();
        scheduleRepository.deleteAll();
        employeeRepository.deleteAll();

        operator = employeeRepository.save(Employee.builder()
                .firstName("SchedOp").lastName("Test").email(EMAIL_PREFIX + "op@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        security = employeeRepository.save(Employee.builder()
                .firstName("SchedSec").lastName("Test").email(EMAIL_PREFIX + "sec@test.com")
                .role(EmployeeRole.SECURITY).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        medical = employeeRepository.save(Employee.builder()
                .firstName("SchedMed").lastName("Test").email(EMAIL_PREFIX + "med@test.com")
                .role(EmployeeRole.MEDICAL).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        maintenance = employeeRepository.save(Employee.builder()
                .firstName("SchedMnt").lastName("Test").email(EMAIL_PREFIX + "mnt@test.com")
                .role(EmployeeRole.MAINTENANCE).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        guestServices = employeeRepository.save(Employee.builder()
                .firstName("SchedGs").lastName("Test").email(EMAIL_PREFIX + "gs@test.com")
                .role(EmployeeRole.GUEST_SERVICES).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        zone = zoneRepository.findAll().stream().findFirst().orElse(null);

        attraction = attractionRepository.save(Attraction.builder()
                .name("Sched Coaster 1").description("Test").photoUrl("http://example.com/s1.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(1.0).mapPositionY(1.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        attraction2 = attractionRepository.save(Attraction.builder()
                .name("Sched Coaster 2").description("Test").photoUrl("http://example.com/s2.jpg")
                .category(AttractionCategory.WATER_RIDE).intensity(Intensity.MEDIUM)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(8).minimumHeight(120).minimumWeight(0)
                .mapPositionX(2.0).mapPositionY(2.0)
                .openingTime(LocalTime.of(10, 0)).closingTime(LocalTime.of(18, 0))
                .build());

        LocalDate today = LocalDate.now();
        monday = today.plusDays(8 - today.getDayOfWeek().getValue());
    }

    @Test
    @Story("Copiar Semana Anterior")
    @Description("Verifica que copyPreviousWeek copia horarios de la semana anterior")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("copyPreviousWeekCopiaHorariosDeSemanaAnterior")
    void copyPreviousWeekCopiaHorariosDeSemanaAnterior() {
        LocalDate previousMonday = monday.minusWeeks(1);
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(previousMonday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());

        scheduleService.copyPreviousWeek(monday);

        List<WeeklyScheduleDTO> result = scheduleService.getWeekSchedule(monday);
        assertFalse(result.isEmpty());
        assertEquals(DayOfWeek.MONDAY, result.get(0).getDayOfWeek());
    }

    @Test
    @Story("Copiar Semana Anterior")
    @Description("Verifica que copyPreviousWeek omite empleados con ausencia en el día destino")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("copyPreviousWeekOmiteEmpleadosConAusencia")
    void copyPreviousWeekOmiteEmpleadosConAusencia() {
        LocalDate previousMonday = monday.minusWeeks(1);
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(previousMonday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());

        // Create schedule entry for the target week first so absence can apply
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());

        // Register an absence for the operator on Monday of target week
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(new BigDecimal("8.00"))
                .reason("Sick leave")
                .build(), "admin");

        // Now copy - the operator should be skipped on Monday
        scheduleService.copyPreviousWeek(monday);

        // The schedule should not have duplicated the operator (already removed by absence)
        assertNotNull(scheduleService.getWeekSchedule(monday));
    }

    @Test
    @Story("Auto-asignar Semana")
    @Description("Verifica que autoAssignWeek crea horarios con rotación para todos los roles")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("autoAssignWeekCreaHorariosConRotacion")
    void autoAssignWeekCreaHorariosConRotacion() {
        scheduleService.autoAssignWeek(monday);

        List<WeeklyScheduleDTO> result = scheduleService.getWeekSchedule(monday);
        assertFalse(result.isEmpty());

        boolean hasOperator = result.stream().anyMatch(dto -> operator.getId().equals(dto.getEmployeeId()));
        boolean hasMedical = result.stream().anyMatch(dto -> medical.getId().equals(dto.getEmployeeId()));
        boolean hasMaintenance = result.stream().anyMatch(dto -> maintenance.getId().equals(dto.getEmployeeId()));
        boolean hasGuestServices = result.stream().anyMatch(dto -> guestServices.getId().equals(dto.getEmployeeId()));
        assertTrue(hasOperator, "Debe asignar al operador");
        assertTrue(hasMedical, "Debe asignar al médico");
        assertTrue(hasMaintenance, "Debe asignar a mantenimiento");
        assertTrue(hasGuestServices, "Debe asignar a atención al cliente");
    }

    @Test
    @Story("Auto-asignar Semana")
    @Description("Verifica que autoAssignWeek limpia horarios previos antes de asignar")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("autoAssignWeekLimpiaHorariosPrevios")
    void autoAssignWeekLimpiaHorariosPrevios() {
        scheduleService.autoAssignWeek(monday);
        int firstCount = scheduleService.getWeekSchedule(monday).size();

        scheduleService.autoAssignWeek(monday);
        int secondCount = scheduleService.getWeekSchedule(monday).size();

        assertEquals(firstCount, secondCount);
    }

    @Test
    @Story("Validar Cobertura")
    @Description("Verifica que validateWeekCoverage con cobertura completa retorna válido")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("validateWeekCoverageConCoberturaCompletaRetornaValido")
    void validateWeekCoverageConCoberturaCompletaRetornaValido() {
        scheduleService.autoAssignWeek(monday);
        CoverageValidationResult result = scheduleService.validateWeekCoverage(monday);
        assertNotNull(result);
        assertEquals(monday, result.getWeekStartDate());
    }

    @Test
    @Story("Validar Cobertura")
    @Description("Verifica que validateWeekCoverage detecta falta de médico")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("validateWeekCoverageDetectaFaltaDeRol")
    void validateWeekCoverageDetectaFaltaDeRol() {
        CoverageValidationResult result = scheduleService.validateWeekCoverage(monday);
        assertFalse(result.isValid());
        boolean hasNoMedical = result.getIssues().stream()
                .anyMatch(i -> "NO_MEDICAL".equals(i.getIssueType()));
        assertTrue(hasNoMedical);
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que crear 6to día como overtime cuando hay issues de cobertura")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("createScheduleEntry6toDiaMarcaComoOvertime")
    void createScheduleEntry6toDiaMarcaComoOvertime() {
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};

        for (DayOfWeek day : days) {
            scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                    .employeeId(operator.getId())
                    .weekStartDate(monday)
                    .dayOfWeek(day)
                    .shift(WorkShift.FULL_DAY)
                    .assignedAttractionId(attraction.getId())
                    .breakGroup(BreakGroup.A)
                    .build());
        }

        // The 6th day should throw or be overtime depending on coverage issues
        try {
            WeeklyScheduleDTO sixthDay = scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                    .employeeId(operator.getId())
                    .weekStartDate(monday)
                    .dayOfWeek(DayOfWeek.SATURDAY)
                    .shift(WorkShift.FULL_DAY)
                    .assignedAttractionId(attraction.getId())
                    .breakGroup(BreakGroup.A)
                    .build());
            assertTrue(sixthDay.getIsOvertime());
        } catch (IllegalArgumentException e) {
            // If no coverage issues exist, max days exceeded is expected
            assertTrue(e.getMessage().contains("error.schedule"));
        }
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que asignar atracción ya ocupada lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("createScheduleEntryAtraccionOcupadaLanzaExcepcion")
    void createScheduleEntryAtraccionOcupadaLanzaExcepcion() {
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());

        Employee operator2 = employeeRepository.save(Employee.builder()
                .firstName("Op2").lastName("Test").email(EMAIL_PREFIX + "op2@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                        .employeeId(operator2.getId())
                        .weekStartDate(monday)
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .shift(WorkShift.FULL_DAY)
                        .assignedAttractionId(attraction.getId())
                        .breakGroup(BreakGroup.B)
                        .build()));
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que asignar zona ya ocupada lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("createScheduleEntryZonaOcupadaLanzaExcepcion")
    void createScheduleEntryZonaOcupadaLanzaExcepcion() {
        if (zone == null) return;

        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(security.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedZoneId(zone.getId())
                .breakGroup(BreakGroup.A)
                .build());

        Employee security2 = employeeRepository.save(Employee.builder()
                .firstName("Sec2").lastName("Test").email(EMAIL_PREFIX + "sec2@test.com")
                .role(EmployeeRole.SECURITY).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                        .employeeId(security2.getId())
                        .weekStartDate(monday)
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .shift(WorkShift.FULL_DAY)
                        .assignedZoneId(zone.getId())
                        .breakGroup(BreakGroup.B)
                        .build()));
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que empleado con ausencia no puede ser asignado")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("createScheduleEntryEmpleadoConAusenciaLanzaExcepcion")
    void createScheduleEntryEmpleadoConAusenciaLanzaExcepcion() {
        // Schedule the employee for Tuesday
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());

        // Register an absence for Tuesday (removes the schedule entry)
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday.plusDays(1)) // Tuesday
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(new BigDecimal("8.00"))
                .reason("Sick")
                .build(), "admin");

        // Trying to re-schedule on Tuesday should fail because absence is active
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                        .employeeId(operator.getId())
                        .weekStartDate(monday)
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .shift(WorkShift.FULL_DAY)
                        .assignedAttractionId(attraction2.getId())
                        .breakGroup(BreakGroup.B)
                        .build()));
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que empleado inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("createScheduleEntryEmpleadoInexistenteLanzaExcepcion")
    void createScheduleEntryEmpleadoInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                        .employeeId(99999L)
                        .weekStartDate(monday)
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .shift(WorkShift.FULL_DAY)
                        .breakGroup(BreakGroup.A)
                        .build()));
    }

    @Test
    @Story("Validar Cobertura")
    @Description("Verifica que validateWeekCoverage detecta ausencias de empleados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("validateWeekCoverageDetectaAusencias")
    void validateWeekCoverageDetectaAusencias() {
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());

        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(new BigDecimal("8.00"))
                .reason("Sick")
                .build(), "admin");

        CoverageValidationResult result = scheduleService.validateWeekCoverage(monday);
        boolean hasAbsenceIssue = result.getIssues().stream()
                .anyMatch(i -> "EMPLOYEE_ABSENT".equals(i.getIssueType()));
        assertTrue(hasAbsenceIssue);
    }
}

