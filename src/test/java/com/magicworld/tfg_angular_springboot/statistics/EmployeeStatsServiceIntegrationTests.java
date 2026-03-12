package com.magicworld.tfg_angular_springboot.statistics;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateScheduleRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryRequest;
import com.magicworld.tfg_angular_springboot.employee.service.ScheduleService;
import com.magicworld.tfg_angular_springboot.employee.service.WorkLogService;
import com.magicworld.tfg_angular_springboot.statistics.dto.*;
import com.magicworld.tfg_angular_springboot.statistics.service.EmployeeStatsService;
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
@Epic("Estadísticas")
@Feature("Servicio de Estadísticas de Empleados - Integración")
public class EmployeeStatsServiceIntegrationTests {

    private static final String EMAIL_PREFIX = "statsint";
    private static final BigDecimal FULL_DAY_HOURS = new BigDecimal("8.00");
    private static final BigDecimal OVERTIME_AMOUNT = new BigDecimal("4.00");

    @Autowired private EmployeeStatsService employeeStatsService;
    @Autowired private ScheduleService scheduleService;
    @Autowired private WorkLogService workLogService;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private WeeklyScheduleRepository scheduleRepository;
    @Autowired private WorkLogRepository workLogRepository;
    @Autowired private AttractionRepository attractionRepository;

    private Employee operator;
    private Attraction attraction;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        workLogRepository.deleteAll();
        scheduleRepository.deleteAll();
        employeeRepository.deleteAll();

        operator = employeeRepository.save(Employee.builder()
                .firstName("StatsInt").lastName("Worker").email(EMAIL_PREFIX + "op@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name("StatsInt Coaster").description("Test").photoUrl("http://example.com/si.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.MEDIUM)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(10).minimumHeight(130).minimumWeight(0)
                .mapPositionX(4.0).mapPositionY(4.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        LocalDate today = LocalDate.now();
        monday = today.plusDays(8 - today.getDayOfWeek().getValue());

        // Create schedule entries for Monday to Friday
        for (int i = 0; i < 5; i++) {
            DayOfWeek day = DayOfWeek.of(i + 1);
            scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                    .employeeId(operator.getId())
                    .weekStartDate(monday)
                    .dayOfWeek(day)
                    .shift(WorkShift.FULL_DAY)
                    .assignedAttractionId(attraction.getId())
                    .breakGroup(BreakGroup.values()[i % 4])
                    .build());
        }
    }

    @Test
    @Story("Ranking de Horas")
    @Description("Verifica que getHoursRanking con horas extra las incluye")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getHoursRankingConHorasExtraLasIncluye")
    void getHoursRankingConHorasExtraLasIncluye() {
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday)
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(OVERTIME_AMOUNT)
                .isOvertime(true)
                .reason("Extra coverage")
                .build(), "admin");

        List<EmployeeHoursRankingDTO> result = employeeStatsService.getHoursRanking(
                monday, monday.plusDays(6));

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getTotalHours().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Story("Ranking de Horas")
    @Description("Verifica que getHoursRanking con ausencia reduce horas normales")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getHoursRankingConAusenciaReduceHorasNormales")
    void getHoursRankingConAusenciaReduceHorasNormales() {
        List<EmployeeHoursRankingDTO> rankingBefore = employeeStatsService.getHoursRanking(
                monday, monday.plusDays(6));
        BigDecimal totalBefore = rankingBefore.get(0).getTotalHours();

        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Sick")
                .build(), "admin");

        List<EmployeeHoursRankingDTO> rankingAfter = employeeStatsService.getHoursRanking(
                monday, monday.plusDays(6));
        BigDecimal totalAfter = rankingAfter.get(0).getTotalHours();

        assertTrue(totalAfter.compareTo(totalBefore) < 0);
    }

    @Test
    @Story("Ranking de Ausencias")
    @Description("Verifica que getAbsenceRanking con ausencia registrada la contabiliza")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAbsenceRankingConAusenciaLaContabiliza")
    void getAbsenceRankingConAusenciaLaContabiliza() {
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Sick")
                .build(), "admin");

        List<EmployeeAbsenceRankingDTO> result = employeeStatsService.getAbsenceRanking(
                monday, monday.plusDays(6));

        assertFalse(result.isEmpty());
        EmployeeAbsenceRankingDTO empAbsence = result.stream()
                .filter(dto -> dto.getEmployeeId().equals(operator.getId()))
                .findFirst().orElseThrow();
        assertTrue(empAbsence.getAbsenceCount() > 0);
    }

    @Test
    @Story("Ranking de Ausencias")
    @Description("Verifica que getAbsenceRanking con REMOVE_ABSENCE reduce el conteo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getAbsenceRankingConRemoveAbsenceReduceConteo")
    void getAbsenceRankingConRemoveAbsenceReduceConteo() {
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Sick")
                .build(), "admin");

        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday)
                .action(WorkLogAction.REMOVE_ABSENCE)
                .hoursAffected(FULL_DAY_HOURS)
                .reason("Recovered")
                .build(), "admin");

        List<EmployeeAbsenceRankingDTO> result = employeeStatsService.getAbsenceRanking(
                monday, monday.plusDays(6));

        EmployeeAbsenceRankingDTO empAbsence = result.stream()
                .filter(dto -> dto.getEmployeeId().equals(operator.getId()))
                .findFirst().orElseThrow();
        assertEquals(0, empAbsence.getAbsenceCount());
    }

    @Test
    @Story("Frecuencia de Posición")
    @Description("Verifica que getPositionFrequency con zona asignada la incluye")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getPositionFrequencySinAsignacionRetornaGeneral")
    void getPositionFrequencySinAsignacionRetornaGeneral() {
        Employee medic = employeeRepository.save(Employee.builder()
                .firstName("MedStats").lastName("Test").email(EMAIL_PREFIX + "med@test.com")
                .role(EmployeeRole.MEDICAL).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        scheduleRepository.save(WeeklySchedule.builder()
                .employee(medic).weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY).shift(WorkShift.FULL_DAY)
                .breakGroup(BreakGroup.A).build());

        List<PositionFrequencyDTO> result = employeeStatsService.getPositionFrequency(
                medic.getId(), monday, monday.plusDays(6));

        assertFalse(result.isEmpty());
        assertEquals("GENERAL", result.get(0).getPositionType());
    }

    @Test
    @Story("Informe de Salarios")
    @Description("Verifica que getSalaryReport calcula salario con horas extra")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getSalaryReportCalculaSalarioConHorasExtra")
    void getSalaryReportCalculaSalarioConHorasExtra() {
        workLogService.addWorkLogEntry(WorkLogEntryRequest.builder()
                .employeeId(operator.getId())
                .targetDate(monday)
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(OVERTIME_AMOUNT)
                .isOvertime(true)
                .reason("Extra work")
                .build(), "admin");

        List<SalaryReportDTO> result = employeeStatsService.getSalaryReport(
                monday, monday.plusDays(6), "es");

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getTotalSalary().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @Story("Ranking de Horas")
    @Description("Verifica rebalanceo: si total <= 40h, overtime se convierte a normal")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getHoursRankingRebalanceaOvertimeANormalSiTotalBajo")
    void getHoursRankingRebalanceaOvertimeANormalSiTotalBajo() {
        // With 5 days of 8h = 40h exactly, total should be all normal
        List<EmployeeHoursRankingDTO> result = employeeStatsService.getHoursRanking(
                monday, monday.plusDays(6));

        assertFalse(result.isEmpty());
        EmployeeHoursRankingDTO ranking = result.get(0);
        assertEquals(0, ranking.getOvertimeHours().compareTo(BigDecimal.ZERO));
        assertEquals(ranking.getTotalHours(), ranking.getNormalHours());
    }
}

