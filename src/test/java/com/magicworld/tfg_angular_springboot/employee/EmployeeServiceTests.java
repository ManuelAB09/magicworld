package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.employee.dto.CreateEmployeeRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.EmployeeDTO;
import com.magicworld.tfg_angular_springboot.employee.service.EmployeeService;
import io.qameta.allure.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Empleados")
@Feature("Servicio de Empleados")
public class EmployeeServiceTests {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }

    private CreateEmployeeRequest sampleRequest(String email) {
        return CreateEmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .phone("+123456789")
                .role(EmployeeRole.OPERATOR)
                .build();
    }

    @Test
    @Story("Listar Empleados")
    @Description("Verifica que obtener todos los empleados retorna lista vacía inicialmente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos retorna lista vacía")
    void testGetAllEmployeesEmpty() {
        List<EmployeeDTO> result = employeeService.getAllEmployees();
        assertTrue(result.isEmpty());
    }

    @Test
    @Story("Listar Empleados")
    @Description("Verifica que obtener todos retorna empleados creados")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos retorna empleados")
    void testGetAllEmployeesWithData() {
        employeeService.createEmployee(sampleRequest("john@example.com"));
        List<EmployeeDTO> result = employeeService.getAllEmployees();
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }

    @Test
    @Story("Listar Empleados Activos")
    @Description("Verifica que obtener empleados activos funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener empleados activos")
    void testGetActiveEmployees() {
        employeeService.createEmployee(sampleRequest("active@example.com"));
        List<EmployeeDTO> result = employeeService.getActiveEmployees();
        assertEquals(1, result.size());
        assertEquals(EmployeeStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    @Story("Filtrar por Rol")
    @Description("Verifica que obtener empleados por rol funciona")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener empleados por rol")
    void testGetEmployeesByRole() {
        employeeService.createEmployee(sampleRequest("operator@example.com"));
        List<EmployeeDTO> result = employeeService.getEmployeesByRole(EmployeeRole.OPERATOR);
        assertEquals(1, result.size());
        assertEquals(EmployeeRole.OPERATOR, result.get(0).getRole());
    }

    @Test
    @Story("Obtener Empleado por ID")
    @Description("Verifica que obtener empleado por ID existente funciona")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener empleado por ID existente")
    void testGetEmployeeById() {
        EmployeeDTO created = employeeService.createEmployee(sampleRequest("emp@example.com"));
        EmployeeDTO found = employeeService.getEmployee(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("John", found.getFirstName());
    }

    @Test
    @Story("Obtener Empleado por ID")
    @Description("Verifica que obtener empleado por ID inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener empleado por ID inexistente lanza excepción")
    void testGetEmployeeByIdNotFound() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployee(999L));
    }

    @Test
    @Story("Crear Empleado")
    @Description("Verifica que crear empleado asigna ID y estado ACTIVE")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear empleado asigna ID y estado ACTIVE")
    void testCreateEmployee() {
        EmployeeDTO result = employeeService.createEmployee(sampleRequest("new@example.com"));
        assertNotNull(result.getId());
        assertEquals(EmployeeStatus.ACTIVE, result.getStatus());
        assertEquals("John Doe", result.getFullName());
    }

    @Test
    @Story("Crear Empleado")
    @Description("Verifica que crear empleado con email duplicado lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear empleado con email duplicado lanza excepción")
    void testCreateEmployeeDuplicateEmail() {
        employeeService.createEmployee(sampleRequest("dup@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> employeeService.createEmployee(sampleRequest("dup@example.com")));
    }

    @Test
    @Story("Actualizar Empleado")
    @Description("Verifica que actualizar empleado cambia los datos")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar empleado cambia datos")
    void testUpdateEmployee() {
        EmployeeDTO created = employeeService.createEmployee(sampleRequest("upd@example.com"));
        CreateEmployeeRequest updateReq = CreateEmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("upd@example.com")
                .phone("+987654321")
                .role(EmployeeRole.SECURITY)
                .build();
        EmployeeDTO updated = employeeService.updateEmployee(created.getId(), updateReq);
        assertEquals("Jane", updated.getFirstName());
        assertEquals(EmployeeRole.SECURITY, updated.getRole());
    }

    @Test
    @Story("Eliminar Empleado")
    @Description("Verifica que eliminar empleado lo borra de la base de datos")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar empleado lo borra")
    void testTerminateEmployee() {
        EmployeeDTO created = employeeService.createEmployee(sampleRequest("term@example.com"));
        employeeService.terminateEmployee(created.getId());
        assertThrows(IllegalArgumentException.class, () -> employeeService.getEmployee(created.getId()));
    }
}
