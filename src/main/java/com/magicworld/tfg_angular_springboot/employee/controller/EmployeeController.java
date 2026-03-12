package com.magicworld.tfg_angular_springboot.employee.controller;

import com.magicworld.tfg_angular_springboot.employee.EmployeeRole;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateEmployeeRequest;
import com.magicworld.tfg_angular_springboot.employee.dto.EmployeeDTO;
import com.magicworld.tfg_angular_springboot.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get all employees")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active employees")
    public ResponseEntity<List<EmployeeDTO>> getActiveEmployees() {
        return ResponseEntity.ok(employeeService.getActiveEmployees());
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get employees by role")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByRole(@PathVariable EmployeeRole role) {
        return ResponseEntity.ok(employeeService.getEmployeesByRole(role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployee(id));
    }

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @PostMapping("/{id}/terminate")
    @Operation(summary = "Delete an employee")
    public ResponseEntity<Void> terminateEmployee(@PathVariable Long id) {
        employeeService.terminateEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
