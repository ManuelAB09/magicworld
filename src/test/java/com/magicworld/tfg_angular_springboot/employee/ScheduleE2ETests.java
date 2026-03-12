package com.magicworld.tfg_angular_springboot.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateScheduleRequest;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = "ADMIN")
@Epic("Gestión de Horarios")
@Feature("API REST de Horarios E2E")
public class ScheduleE2ETests {

    private static final String API_SCHEDULES = "/api/v1/schedules";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private WeeklyScheduleRepository scheduleRepository;
    @Autowired private AttractionRepository attractionRepository;
    @Autowired private ParkZoneRepository zoneRepository;

    private Employee operator;
    private Employee security;
    private Attraction attraction;
    private ParkZone zone;
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        employeeRepository.deleteAll();

        operator = employeeRepository.save(Employee.builder()
                .firstName("E2EOp").lastName("Sched").email("e2eop@sched.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        security = employeeRepository.save(Employee.builder()
                .firstName("E2ESec").lastName("Sched").email("e2esec@sched.com")
                .role(EmployeeRole.SECURITY).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name("E2E Schedule Ride").description("Test").photoUrl("http://example.com/e2es.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.HIGH)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(12).minimumHeight(140).minimumWeight(0)
                .mapPositionX(8.0).mapPositionY(8.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        zone = zoneRepository.findAll().stream().findFirst().orElse(null);

        LocalDate today = LocalDate.now();
        monday = today.plusDays(8 - today.getDayOfWeek().getValue());
    }

    @Test
    @Story("Obtener Horario Semanal")
    @Description("Verifica que GET /week retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getWeekScheduleEndpointRetorna200")
    void getWeekScheduleEndpointRetorna200() throws Exception {
        mockMvc.perform(get(API_SCHEDULES + "/week").param("weekStart", monday.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Story("Crear Entrada")
    @Description("Verifica que POST / crea entrada y retorna 201")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("createScheduleEntryEndpointRetorna201")
    void createScheduleEntryEndpointRetorna201() throws Exception {
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.MONDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.A)
                .build();

        mockMvc.perform(post(API_SCHEDULES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(operator.getId()));
    }

    @Test
    @Story("Eliminar Entrada")
    @Description("Verifica que DELETE /{id} retorna 204")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("deleteScheduleEntryEndpointRetorna204")
    void deleteScheduleEntryEndpointRetorna204() throws Exception {
        CreateScheduleRequest request = CreateScheduleRequest.builder()
                .employeeId(operator.getId())
                .weekStartDate(monday)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .shift(WorkShift.FULL_DAY)
                .assignedAttractionId(attraction.getId())
                .breakGroup(BreakGroup.B)
                .build();

        String result = mockMvc.perform(post(API_SCHEDULES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(result).get("id").asLong();

        mockMvc.perform(delete(API_SCHEDULES + "/" + id).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Story("Copiar Semana")
    @Description("Verifica que POST /copy-week retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("copyPreviousWeekEndpointRetorna200")
    void copyPreviousWeekEndpointRetorna200() throws Exception {
        mockMvc.perform(post(API_SCHEDULES + "/copy-week").param("targetWeekStart", monday.toString()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Validar Cobertura")
    @Description("Verifica que GET /validate retorna 200 con resultado de validación")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("validateWeekCoverageEndpointRetorna200")
    void validateWeekCoverageEndpointRetorna200() throws Exception {
        mockMvc.perform(get(API_SCHEDULES + "/validate").param("weekStart", monday.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").exists())
                .andExpect(jsonPath("$.issues").isArray());
    }

    @Test
    @Story("Auto-asignar")
    @Description("Verifica que POST /auto-assign retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("autoAssignWeekEndpointRetorna200")
    void autoAssignWeekEndpointRetorna200() throws Exception {
        mockMvc.perform(post(API_SCHEDULES + "/auto-assign").param("weekStart", monday.toString()).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Obtener Horario de Empleado")
    @Description("Verifica que GET /employee/{id} retorna 200")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getEmployeeScheduleEndpointRetorna200")
    void getEmployeeScheduleEndpointRetorna200() throws Exception {
        mockMvc.perform(get(API_SCHEDULES + "/employee/" + operator.getId())
                        .param("weekStart", monday.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

