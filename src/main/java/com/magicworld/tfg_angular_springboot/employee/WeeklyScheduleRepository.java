package com.magicworld.tfg_angular_springboot.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {

        List<WeeklySchedule> findByWeekStartDate(LocalDate weekStartDate);

        List<WeeklySchedule> findByEmployeeIdAndWeekStartDate(Long employeeId, LocalDate weekStartDate);

        List<WeeklySchedule> findByWeekStartDateAndDayOfWeek(LocalDate weekStartDate, DayOfWeek dayOfWeek);

        @Query("SELECT ws FROM WeeklySchedule ws WHERE ws.weekStartDate = :weekStart AND ws.dayOfWeek = :day " +
                        "AND ws.assignedAttraction.id = :attractionId")
        List<WeeklySchedule> findByDateAndAttraction(
                        @Param("weekStart") LocalDate weekStart,
                        @Param("day") DayOfWeek day,
                        @Param("attractionId") Long attractionId);

        @Query("SELECT ws FROM WeeklySchedule ws WHERE ws.weekStartDate = :weekStart AND ws.dayOfWeek = :day " +
                        "AND ws.assignedZone.id = :zoneId")
        List<WeeklySchedule> findByDateAndZone(
                        @Param("weekStart") LocalDate weekStart,
                        @Param("day") DayOfWeek day,
                        @Param("zoneId") Long zoneId);

        @Query("SELECT ws FROM WeeklySchedule ws JOIN FETCH ws.employee WHERE ws.weekStartDate = :weekStart")
        List<WeeklySchedule> findByWeekWithEmployee(@Param("weekStart") LocalDate weekStart);

        void deleteByWeekStartDateAndEmployeeId(LocalDate weekStartDate, Long employeeId);

        void deleteByWeekStartDate(LocalDate weekStartDate);

        void deleteByEmployeeIdAndWeekStartDateGreaterThanEqual(Long employeeId, LocalDate fromDate);

        void deleteByEmployeeId(Long employeeId);

        List<WeeklySchedule> findByEmployeeIdAndWeekStartDateBetween(
                        Long employeeId, LocalDate from, LocalDate to);

        @Query("SELECT ws FROM WeeklySchedule ws JOIN FETCH ws.employee " +
                        "WHERE ws.weekStartDate BETWEEN :from AND :to")
        List<WeeklySchedule> findAllInDateRangeWithEmployee(
                        @Param("from") LocalDate from, @Param("to") LocalDate to);

        @Query("SELECT ws FROM WeeklySchedule ws WHERE ws.assignedAttraction.id = :attractionId")
        List<WeeklySchedule> findByAssignedAttractionId(@Param("attractionId") Long attractionId);
}
