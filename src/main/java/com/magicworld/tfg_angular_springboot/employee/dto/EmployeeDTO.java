package com.magicworld.tfg_angular_springboot.employee.dto;

import com.magicworld.tfg_angular_springboot.employee.EmployeeRole;
import com.magicworld.tfg_angular_springboot.employee.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private EmployeeRole role;
    private EmployeeStatus status;
    private LocalDate hireDate;
}

