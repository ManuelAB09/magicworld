package com.magicworld.tfg_angular_springboot.employee.service;

import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.employee.dto.EmployeeDTO;
import com.magicworld.tfg_angular_springboot.employee.dto.CreateEmployeeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final WeeklyScheduleRepository scheduleRepository;
    private final DailyAssignmentRepository dailyAssignmentRepository;
    private final ReinforcementCallRepository reinforcementCallRepository;
    private final WorkLogRepository workLogRepository;

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getActiveEmployees() {
        return employeeRepository.findAllActive().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByRole(EmployeeRole role) {
        return employeeRepository.findByRoleAndStatus(role, EmployeeStatus.ACTIVE).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployee(Long id) {
        return employeeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));
    }

    @Transactional
    public EmployeeDTO createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("error.employee.email.already.exists");
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(EmployeeStatus.ACTIVE)
                .hireDate(LocalDate.now())
                .build();

        return toDTO(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, CreateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setRole(request.getRole());

        return toDTO(employeeRepository.save(employee));
    }

    @Transactional
    public void terminateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));

        // Delete all FK-referenced records first to avoid constraint violations
        workLogRepository.deleteByEmployeeId(id);
        reinforcementCallRepository.deleteByEmployeeId(id);
        dailyAssignmentRepository.deleteByEmployeeId(id);
        scheduleRepository.deleteByEmployeeId(id);

        // Hard-delete the employee from the database
        employeeRepository.delete(employee);
    }

    private EmployeeDTO toDTO(Employee e) {
        return EmployeeDTO.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .role(e.getRole())
                .status(e.getStatus())
                .hireDate(e.getHireDate())
                .build();
    }
}
