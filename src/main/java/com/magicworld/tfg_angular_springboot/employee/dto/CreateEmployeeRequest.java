package com.magicworld.tfg_angular_springboot.employee.dto;

import com.magicworld.tfg_angular_springboot.employee.EmployeeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {

    @NotNull(message = "validation.employee.firstName.required")
    @Size(min = 2, max = 50, message = "validation.employee.firstName.size")
    private String firstName;

    @NotNull(message = "validation.employee.lastName.required")
    @Size(min = 2, max = 50, message = "validation.employee.lastName.size")
    private String lastName;

    @NotNull(message = "validation.employee.email.required")
    @Email(message = "validation.employee.email.invalid")
    private String email;

    @Size(max = 20, message = "validation.employee.phone.size")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "validation.employee.phone.invalid")
    private String phone;

    @NotNull(message = "validation.employee.role.required")
    private EmployeeRole role;
}
