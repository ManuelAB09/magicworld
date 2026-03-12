package com.magicworld.tfg_angular_springboot.statistics;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.employee.service.ScheduleService;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateScheduleRequest;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Estadísticas")
@Feature("Servicio de Estadísticas de Empleados")
public class EmployeeStatsServiceTests {

    @Autowired
    private EmployeeStatsService employeeStatsService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WeeklyScheduleRepository scheduleRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ScheduleService scheduleService;

    private Employee operator;
    private Attraction attraction;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        employeeRepository.deleteAll();

        operator = employeeRepository.save(Employee.builder()
                .firstName("Stats").lastName("Worker").email("stats@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name("Stats Coaster").description("Ride").photoUrl("http://example.com/s.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.MEDIUM)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(10).minimumHeight(130).minimumWeight(0)
                .mapPositionX(3.0).mapPositionY(3.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        LocalDate today = LocalDate.now();
        monday = today.plusDays(8 - today.getDayOfWeek().getValue());

        // Create a schedule entry
        scheduleService.createScheduleEntry(CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build());
    }

    @Test
    @Story("Ranking de Horas")
    @Description("Verifica que obtener ranking de horas retorna resultados")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener ranking de horas")
    void testGetHoursRanking() {
        List<EmployeeHoursRankingDTO> result = employeeStatsService.getHoursRanking(monday, monday.plusDays(6));
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(operator.getId(), result.get(0).getEmployeeId());
    }

    @Test
    @Story("Ranking de Horas")
    @Description("Verifica que ranking vacío retorna lista vacía")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ranking vacío retorna lista vacía")
    void testGetHoursRankingEmpty() {
        LocalDate farFuture = monday.plusYears(1);
        List<EmployeeHoursRankingDTO> result = employeeStatsService.getHoursRanking(farFuture, farFuture.plusDays(6));
        assertTrue(result.isEmpty());
    }

    @Test
    @Story("Ranking de Ausencias")
    @Description("Verifica que obtener ranking de ausencias retorna resultados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener ranking de ausencias")
    void testGetAbsenceRanking() {
        List<EmployeeAbsenceRankingDTO> result = employeeStatsService.getAbsenceRanking(monday, monday.plusDays(6));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Story("Frecuencia de Posición")
    @Description("Verifica que obtener frecuencia de posición retorna resultados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener frecuencia de posición")
    void testGetPositionFrequency() {
        List<PositionFrequencyDTO> result = employeeStatsService.getPositionFrequency(
                operator.getId(), monday, monday.plusDays(6));
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("ATTRACTION", result.get(0).getPositionType());
    }

    @Test
    @Story("Informe de Salarios")
    @Description("Verifica que obtener informe de salarios en EUR funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Informe de salarios EUR")
    void testGetSalaryReportEUR() {
        List<SalaryReportDTO> result = employeeStatsService.getSalaryReport(monday, monday.plusDays(6), "es");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("EUR", result.get(0).getCurrency());
    }

    @Test
    @Story("Informe de Salarios")
    @Description("Verifica que obtener informe de salarios en USD funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Informe de salarios USD")
    void testGetSalaryReportUSD() {
        List<SalaryReportDTO> result = employeeStatsService.getSalaryReport(monday, monday.plusDays(6), "en");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("USD", result.get(0).getCurrency());
    }
}
