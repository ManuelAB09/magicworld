package com.magicworld.tfg_angular_springboot.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByStatus(EmployeeStatus status);

    List<Employee> findByRole(EmployeeRole role);

    List<Employee> findByRoleAndStatus(EmployeeRole role, EmployeeStatus status);

    boolean existsByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE e.status = 'ACTIVE' ORDER BY e.lastName, e.firstName")
    List<Employee> findAllActive();
}

