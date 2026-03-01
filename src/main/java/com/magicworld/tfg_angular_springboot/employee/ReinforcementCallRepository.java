package com.magicworld.tfg_angular_springboot.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReinforcementCallRepository extends JpaRepository<ReinforcementCall, Long> {

    List<ReinforcementCall> findByStatus(ReinforcementStatus status);

    List<ReinforcementCall> findByOriginAlertId(Long alertId);

    List<ReinforcementCall> findByEmployeeIdAndCallTimeBetween(
            Long employeeId,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end);

    default List<ReinforcementCall> findByEmployeeAndDate(Long employeeId, LocalDate date) {
        return findByEmployeeIdAndCallTimeBetween(
                employeeId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay());
    }

    void deleteByEmployeeId(Long employeeId);
}
