package com.magicworld.tfg_angular_springboot.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateScheduleRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryRequest;
import com.magicworld.tfg_angular_springboot.employee.service.ScheduleService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Horas de Trabajo")
@Feature("API REST de WorkLog E2E")
public class WorkLogE2ETests {

    private static final String API_WORKLOG = "/api/v1/worklog";
    private static final BigDecimal HOURS_8 = new BigDecimal("8.00");
    private static final BigDecimal HOURS_4 = new BigDecimal("4.00");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private WeeklyScheduleRepository scheduleRepository;
    @Autowired private WorkLogRepository workLogRepository;
    @Autowired private AttractionRepository attractionRepository;
    @Autowired private ScheduleService scheduleService;

    private Employee employee;
    private Attraction attraction;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        workLogRepository.deleteAll();
        scheduleRepository.deleteAll();
        employeeRepository.deleteAll();

        employee = employeeRepository.save(Employee.builder()
                .firstName("E2EWl").lastName("Test").email("e2ewl@test.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name("E2E WL Ride").description("Test").photoUrl("http://example.com/e2ewl.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(9.0).mapPositionY(9.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        LocalDate today = LocalDate.now();
        LocalDate monday = today.plusDays(8 - today.getDayOfWeek().getValue());
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
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Story("Obtener Resumen")
    @Description("Verifica que GET /summary/{id} retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getEmployeeSummaryEndpointRetorna200")
    void getEmployeeSummaryEndpointRetorna200() throws Exception {
        mockMvc.perform(get(API_WORKLOG + "/summary/" + employee.getId())
                        .param("from", futureDate.toString())
                        .param("to", futureDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(jsonPath("$.scheduledDays").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Story("Obtener Historial")
    @Description("Verifica que GET /history/{id} retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getWorkLogHistoryEndpointRetorna200")
    void getWorkLogHistoryEndpointRetorna200() throws Exception {
        mockMvc.perform(get(API_WORKLOG + "/history/" + employee.getId())
                        .param("from", futureDate.toString())
                        .param("to", futureDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Story("Añadir Entrada")
    @Description("Verifica que POST /entry con horas extra retorna 201")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("addOvertimeEntryEndpointRetorna201")
    void addOvertimeEntryEndpointRetorna201() throws Exception {
        WorkLogEntryRequest request = WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(HOURS_4)
                .isOvertime(true)
                .reason("Extra shift coverage")
                .build();

        mockMvc.perform(post(API_WORKLOG + "/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("ADD_OVERTIME_HOURS"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Story("Registrar Ausencia")
    @Description("Verifica que POST /entry con ausencia retorna 201")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("addAbsenceEntryEndpointRetorna201")
    void addAbsenceEntryEndpointRetorna201() throws Exception {
        WorkLogEntryRequest request = WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(futureDate)
                .action(WorkLogAction.ADD_ABSENCE)
                .hoursAffected(HOURS_8)
                .reason("Sick leave")
                .build();

        mockMvc.perform(post(API_WORKLOG + "/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.action").value("ADD_ABSENCE"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Story("Validación")
    @Description("Verifica que POST /entry con fecha pasada retorna 400")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("addEntryConFechaPasadaRetorna400")
    void addEntryConFechaPasadaRetorna400() throws Exception {
        WorkLogEntryRequest request = WorkLogEntryRequest.builder()
                .employeeId(employee.getId())
                .targetDate(LocalDate.now().minusDays(1))
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(HOURS_4)
                .reason("Test")
                .build();

        mockMvc.perform(post(API_WORKLOG + "/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}

