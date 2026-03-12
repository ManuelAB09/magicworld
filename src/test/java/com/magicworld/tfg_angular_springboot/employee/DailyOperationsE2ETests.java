package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.monitoring.alert.*;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
@Epic("Gestión de Operaciones Diarias")
@Feature("API REST de Operaciones Diarias E2E")
public class DailyOperationsE2ETests {

    private static final String API_BASE = "/api/v1/daily-operations";

    @Autowired private MockMvc mockMvc;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private DailyAssignmentRepository dailyAssignmentRepository;
    @Autowired private AttractionRepository attractionRepository;
    @Autowired private ParkAlertRepository alertRepository;
    @Autowired private ReinforcementCallRepository reinforcementCallRepository;

    private Employee operator;
    private Employee reinforcementEmp;
    private Attraction attraction;
    private ParkAlert alert;

    @BeforeEach
    void setUp() {
        dailyAssignmentRepository.deleteAll();
        reinforcementCallRepository.deleteAll();

        operator = employeeRepository.save(Employee.builder()
                .firstName("E2EOp").lastName("Test").email("e2eop@dops.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now()).build());

        reinforcementEmp = employeeRepository.save(Employee.builder()
                .firstName("E2EReinf").lastName("Test").email("e2ereinf@dops.com")
                .role(EmployeeRole.OPERATOR).status(EmployeeStatus.ACTIVE)
                .phone("555-1234").hireDate(LocalDate.now()).build());

        attraction = attractionRepository.save(Attraction.builder()
                .name("E2E DailyOps Ride").description("Test").photoUrl("http://example.com/e2e.jpg")
                .category(AttractionCategory.ROLLER_COASTER).intensity(Intensity.MEDIUM)
                .maintenanceStatus(MaintenanceStatus.OPERATIONAL)
                .isActive(true).minimumAge(10).minimumHeight(130).minimumWeight(0)
                .mapPositionX(7.0).mapPositionY(7.0)
                .openingTime(LocalTime.of(9, 0)).closingTime(LocalTime.of(17, 0))
                .build());

        alert = alertRepository.save(ParkAlert.builder()
                .alertType(AlertType.TECHNICAL_ISSUE)
                .severity(AlertSeverity.WARNING)
                .message("E2E test alert")
                .suggestion("Fix it")
                .timestamp(LocalDateTime.now())
                .isActive(true).build());
    }

    @Test
    @Story("Inicializar Día")
    @Description("Verifica que POST /initialize retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("initializeDayEndpointRetorna200")
    void initializeDayEndpointRetorna200() throws Exception {
        String futureDate = LocalDate.now().plusDays(30).toString();
        mockMvc.perform(post(API_BASE + "/initialize").param("date", futureDate).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Obtener Asignaciones")
    @Description("Verifica que GET /today retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getTodayAssignmentsEndpointRetorna200")
    void getTodayAssignmentsEndpointRetorna200() throws Exception {
        mockMvc.perform(get(API_BASE + "/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Story("Obtener Asignaciones")
    @Description("Verifica que GET /date retorna 200 con fecha válida")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("getAssignmentsForDateEndpointRetorna200")
    void getAssignmentsForDateEndpointRetorna200() throws Exception {
        String date = LocalDate.now().toString();
        mockMvc.perform(get(API_BASE + "/date").param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Story("Empleados Disponibles")
    @Description("Verifica que GET /available retorna 200 con rol válido")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAvailableEmployeesEndpointRetorna200")
    void getAvailableEmployeesEndpointRetorna200() throws Exception {
        mockMvc.perform(get(API_BASE + "/available").param("role", "OPERATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasReinforcements").exists());
    }

    @Test
    @Story("Asignar a Alerta")
    @Description("Verifica que POST /assign-to-alert retorna 200 con empleado trabajando")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("assignToAlertEndpointRetorna200")
    void assignToAlertEndpointRetorna200() throws Exception {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(operator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.WORKING)
                .currentAttraction(attraction)
                .breakGroup(BreakGroup.A)
                .breakStartTime(BreakGroup.A.getStartTime())
                .breakEndTime(BreakGroup.A.getEndTime())
                .build());

        mockMvc.perform(post(API_BASE + "/assign-to-alert")
                        .param("employeeId", operator.getId().toString())
                        .param("alertId", alert.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("ASSIGNED_TO_ALERT"));
    }

    @Test
    @Story("Liberar de Alerta")
    @Description("Verifica que POST /release-from-alert retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("releaseFromAlertEndpointRetorna200")
    void releaseFromAlertEndpointRetorna200() throws Exception {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(operator).assignmentDate(LocalDate.now())
                .currentStatus(DailyStatus.ASSIGNED_TO_ALERT)
                .assignedAlert(alert)
                .breakGroup(BreakGroup.A)
                .breakStartTime(BreakGroup.A.getStartTime())
                .breakEndTime(BreakGroup.A.getEndTime())
                .build());

        mockMvc.perform(post(API_BASE + "/release-from-alert")
                        .param("employeeId", operator.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("WORKING"));
    }

    @Test
    @Story("Llamar Refuerzo")
    @Description("Verifica que POST /call-reinforcement retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("callReinforcementEndpointRetorna200")
    void callReinforcementEndpointRetorna200() throws Exception {
        mockMvc.perform(post(API_BASE + "/call-reinforcement")
                        .param("employeeId", reinforcementEmp.getId().toString())
                        .param("alertId", alert.getId().toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @Story("Actualizar Estado Refuerzo")
    @Description("Verifica que POST /reinforcement/{id}/status retorna 200")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateReinforcementStatusEndpointRetorna200")
    void updateReinforcementStatusEndpointRetorna200() throws Exception {
        ReinforcementCall call = reinforcementCallRepository.save(ReinforcementCall.builder()
                .employee(reinforcementEmp)
                .callTime(LocalDateTime.now())
                .status(ReinforcementStatus.PENDING)
                .isOvertime(true).build());

        mockMvc.perform(post(API_BASE + "/reinforcement/" + call.getId() + "/status")
                        .param("status", "ACCEPTED")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}

