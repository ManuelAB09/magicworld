package com.magicworld.tfg_angular_springboot.employee.dto;

import com.magicworld.tfg_angular_springboot.employee.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableEmployeesResponse {
    private List<AvailableEmployee> employees;
    private List<ReinforcementCandidate> reinforcements;
    private boolean hasAvailable;
    private boolean hasReinforcements;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableEmployee {
        private Long id;
        private String name;
        private EmployeeRole role;
        private String currentLocation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReinforcementCandidate {
        private Long id;
        private String name;
        private EmployeeRole role;
        private String phone;
    }
}

