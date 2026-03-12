package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateScheduleRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.CoverageValidationResult;
import com.magicworld.tfg_angular_springboot.employee.dto.WeeklyScheduleDTO;
import com.magicworld.tfg_angular_springboot.employee.service.ScheduleService;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Horarios")
@Feature("Servicio de Horarios Semanales")
public class ScheduleServiceTests {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WeeklyScheduleRepository scheduleRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ParkZoneRepository zoneRepository;

    private Employee operator;
    private Employee security;
    private Attraction attraction;
    private ParkZone zone;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        employeeRepository.deleteAll();
        attractionRepository.deleteAll();

        operator = employeeRepository.save(Employee.builder()
                .firstName("John").lastName("Doe").email("op@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        security = employeeRepository.save(Employee.builder()
                .firstName("Jane").lastName("Smith").email("sec@test.com")
                .role(EmployeeRole.SECURITY).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        zone = zoneRepository.findAll().stream().findFirst().orElse(null);

        attraction = attractionRepository.save(Attraction.builder()
                .name("Test Coaster").description("A test ride").photoUrl("http://example.com/photo.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(1.0).mapPositionY(1.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        // Monday of next week
        LocalDate today = LocalDate.now();
        monday = today.plusDays(8 - today.getDayOfWeek().getValue());
    }

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
    }

    @Test
    @Story("Obtener Horario Semanal")
    @Description("Verifica que obtener horario de semana vacía retorna lista vacía")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener horario de semana vacía retorna lista vacía")
    void testGetWeekScheduleEmpty() {
        List<WeeklyScheduleDTO> result = scheduleService.getWeekSchedule(monday);
        assertTrue(result.isEmpty());
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que crear entrada de horario para operador con atracción funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear entrada de horario para operador")
    void testCreateScheduleEntryForOperator() {
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build();

        WeeklyScheduleDTO result = scheduleService.createScheduleEntry(request);
        assertNotNull(result.getId());
        assertEquals(operator.getId(), result.getEmployeeId());
        assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que crear entrada duplicada lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear entrada duplicada lanza excepción")
    void testCreateDuplicateEntryThrows() {
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build();
        scheduleService.createScheduleEntry(request);
        assertThrows(IllegalArgumentException.class, () -> scheduleService.createScheduleEntry(request));
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que operador sin atracción lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Operador sin atracción lanza excepción")
    void testOperatorWithoutAttractionThrows() {
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .shift(WorkShift.FULL_DAY)
                .breakGroup(BreakGroup.A)
                .build();
        assertThrows(IllegalArgumentException.class, () -> scheduleService.createScheduleEntry(request));
    }

    @Test
    @Story("Crear Entrada de Horario")
    @Description("Verifica que seguridad sin zona lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Seguridad sin zona lanza excepción")
    void testSecurityWithoutZoneThrows() {
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .employeeId(security.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .breakGroup(BreakGroup.B)
                .build();
        assertThrows(IllegalArgumentException.class, () -> scheduleService.createScheduleEntry(request));
    }

    @Test
    @Story("Obtener Horario de Empleado")
    @Description("Verifica que obtener horario de empleado retorna entradas correctas")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener horario de empleado")
    void testGetEmployeeSchedule() {
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.C)
                .build();
        scheduleService.createScheduleEntry(request);

        List<WeeklyScheduleDTO> result = scheduleService.getEmployeeSchedule(operator.getId(), monday);
        assertEquals(1, result.size());
        assertEquals(DayOfWeek.WEDNESDAY, result.get(0).getDayOfWeek());
    }

    @Test
    @Story("Eliminar Entrada")
    @Description("Verifica que eliminar entrada de horario funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar entrada de horario")
    void testDeleteScheduleEntry() {
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.THURSDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.D)
                .build();
        WeeklyScheduleDTO created = scheduleService.createScheduleEntry(request);
        scheduleService.deleteScheduleEntry(created.getId());

        List<WeeklyScheduleDTO> result = scheduleService.getEmployeeSchedule(operator.getId(), monday);
        assertTrue(result.isEmpty());
    }

    @Test
    @Story("Validar Cobertura")
    @Description("Verifica que validar cobertura detecta issues cuando no hay horarios")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Validar cobertura detecta issues")
    void testValidateCoverageDetectsIssues() {
        CoverageValidationResult result = scheduleService.validateWeekCoverage(monday);
        assertFalse(result.isValid());
        assertFalse(result.getIssues().isEmpty());
    }

    @Test
    @Story("Auto-asignar Semana")
    @Description("Verifica que auto-asignar semana crea horarios para empleados activos")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Auto-asignar semana crea horarios")
    void testAutoAssignWeek() {
        scheduleService.autoAssignWeek(monday);
        List<WeeklyScheduleDTO> result = scheduleService.getWeekSchedule(monday);
        assertFalse(result.isEmpty());
    }
}
