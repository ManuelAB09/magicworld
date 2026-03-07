package com.magicworld.tfg_angular_springboot.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyAssignmentRepository extends JpaRepository<DailyAssignment, Long> {

    List<DailyAssignment> findByAssignmentDate(LocalDate date);

    Optional<DailyAssignment> findByEmployeeIdAndAssignmentDate(Long employeeId, LocalDate date);

    List<DailyAssignment> findByAssignmentDateAndCurrentStatus(LocalDate date, DailyStatus status);

    @Query("SELECT da FROM DailyAssignment da JOIN FETCH da.employee WHERE da.assignmentDate = :date")
    List<DailyAssignment> findByDateWithEmployee(@Param("date") LocalDate date);

    @Query("SELECT da FROM DailyAssignment da JOIN FETCH da.employee e " +
            "WHERE da.assignmentDate = :date AND da.currentStatus = :status AND e.role = :role")
    List<DailyAssignment> findAvailableByRoleAndDate(
            @Param("date") LocalDate date,
            @Param("status") DailyStatus status,
            @Param("role") EmployeeRole role);

    @Query("SELECT da FROM DailyAssignment da WHERE da.assignmentDate = :date AND da.assignedAlert.id = :alertId")
    List<DailyAssignment> findByDateAndAlert(@Param("date") LocalDate date, @Param("alertId") Long alertId);

    @Query("SELECT da FROM DailyAssignment da JOIN FETCH da.employee e " +
            "WHERE da.assignmentDate = :date AND da.currentAttraction.id = :attractionId")
    List<DailyAssignment> findByDateAndAttraction(
            @Param("date") LocalDate date,
            @Param("attractionId") Long attractionId);

    void deleteByEmployeeId(Long employeeId);

    List<DailyAssignment> findByEmployeeIdAndAssignmentDateBetween(
            Long employeeId, LocalDate from, LocalDate to);

    @Query("SELECT da FROM DailyAssignment da JOIN FETCH da.employee " +
            "WHERE da.assignmentDate BETWEEN :from AND :to")
    List<DailyAssignment> findAllInDateRangeWithEmployee(
            @Param("from") LocalDate from, @Param("to") LocalDate to);
}
