package com.magicworld.tfg_angular_springboot.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {

    List<WorkLog> findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(
            Long employeeId, LocalDate from, LocalDate to);

    @Query("SELECT wl FROM WorkLog wl JOIN FETCH wl.employee WHERE wl.targetDate BETWEEN :from AND :to AND wl.action = :action")
    List<WorkLog> findByTargetDateBetweenAndAction(
            @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("action") WorkLogAction action);

    @Query("SELECT wl FROM WorkLog wl JOIN FETCH wl.employee WHERE wl.targetDate BETWEEN :from AND :to")
    List<WorkLog> findAllByTargetDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    void deleteByEmployeeId(Long employeeId);
}

