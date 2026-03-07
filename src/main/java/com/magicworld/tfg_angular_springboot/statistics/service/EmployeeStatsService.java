package com.magicworld.tfg_angular_springboot.statistics.service;

import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.employee.service.WorkLogService;
import com.magicworld.tfg_angular_springboot.statistics.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmployeeStatsService {

    private static final BigDecimal MAX_NORMAL_WEEKLY_HOURS = new BigDecimal("40");

    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final WorkLogRepository workLogRepository;

    @Transactional(readOnly = true)
    public List<EmployeeHoursRankingDTO> getHoursRanking(LocalDate from, LocalDate to) {
        LocalDate weekFrom = normalizeToMonday(from);
        LocalDate weekTo = normalizeToMonday(to);

        List<WeeklySchedule> allSchedules = weeklyScheduleRepository
                .findAllInDateRangeWithEmployee(weekFrom, weekTo);

        Map<Long, HoursAccumulator> accumulators = new LinkedHashMap<>();

        // 1) Baseline from schedule
        for (WeeklySchedule ws : allSchedules) {
            LocalDate actualDate = ws.getActualDate();
            if (actualDate.isBefore(from) || actualDate.isAfter(to)) {
                continue;
            }

            Employee emp = ws.getEmployee();
            HoursAccumulator acc = accumulators.computeIfAbsent(
                    emp.getId(), k -> new HoursAccumulator(emp));

            BigDecimal hours = WorkLogService.calculateEffectiveHours(ws);
            if (Boolean.TRUE.equals(ws.getIsOvertime())) {
                acc.overtimeHours = acc.overtimeHours.add(hours);
            } else {
                acc.normalHours = acc.normalHours.add(hours);
            }
        }

        // 2) Apply WorkLog adjustments
        List<WorkLog> allLogs = workLogRepository.findAllByTargetDateBetween(from, to);

        for (WorkLog log : allLogs) {
            Employee emp = log.getEmployee();
            HoursAccumulator acc = accumulators.computeIfAbsent(
                    emp.getId(), k -> new HoursAccumulator(emp));

            switch (log.getAction()) {
                case ADD_OVERTIME_HOURS -> acc.overtimeHours = acc.overtimeHours.add(log.getHoursAffected());
                case ADD_ABSENCE -> acc.normalHours = acc.normalHours.subtract(log.getHoursAffected());
                case REMOVE_ABSENCE -> acc.normalHours = acc.normalHours.add(log.getHoursAffected());
                case PARTIAL_ABSENCE -> acc.normalHours = acc.normalHours.subtract(log.getHoursAffected());
                case REMOVE_SCHEDULED_DAY -> acc.normalHours = acc.normalHours.subtract(log.getHoursAffected());
            }
        }

        return accumulators.values().stream()
                .map(acc -> {
                    BigDecimal normal = acc.normalHours.max(BigDecimal.ZERO);
                    BigDecimal overtime = acc.overtimeHours.max(BigDecimal.ZERO);
                    BigDecimal total = normal.add(overtime);

                    // Rebalance: if total ≤ 40h, overtime becomes normal
                    if (total.compareTo(MAX_NORMAL_WEEKLY_HOURS) <= 0) {
                        normal = total;
                        overtime = BigDecimal.ZERO;
                    } else if (normal.compareTo(MAX_NORMAL_WEEKLY_HOURS) < 0) {
                        overtime = total.subtract(MAX_NORMAL_WEEKLY_HOURS);
                        normal = MAX_NORMAL_WEEKLY_HOURS;
                    }

                    return EmployeeHoursRankingDTO.builder()
                            .employeeId(acc.employee.getId())
                            .fullName(acc.employee.getFullName())
                            .role(acc.employee.getRole().name())
                            .normalHours(normal)
                            .overtimeHours(overtime)
                            .totalHours(total)
                            .build();
                })
                .sorted(Comparator.comparing(EmployeeHoursRankingDTO::getTotalHours).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeAbsenceRankingDTO> getAbsenceRanking(LocalDate from, LocalDate to) {
        LocalDate weekFrom = normalizeToMonday(from);
        LocalDate weekTo = normalizeToMonday(to);

        // Get scheduled days per employee from schedule
        List<WeeklySchedule> allSchedules = weeklyScheduleRepository
                .findAllInDateRangeWithEmployee(weekFrom, weekTo);

        Map<Long, AbsenceAccumulator> accumulators = new LinkedHashMap<>();

        for (WeeklySchedule ws : allSchedules) {
            LocalDate actualDate = ws.getActualDate();
            if (actualDate.isBefore(from) || actualDate.isAfter(to)) {
                continue;
            }
            Employee emp = ws.getEmployee();
            accumulators.computeIfAbsent(emp.getId(), k -> new AbsenceAccumulator(emp)).scheduledDays++;
        }

        // Count absences ONLY from WorkLog
        List<WorkLog> allLogs = workLogRepository.findAllByTargetDateBetween(from, to);
        for (WorkLog log : allLogs) {
            Employee emp = log.getEmployee();
            AbsenceAccumulator acc = accumulators.computeIfAbsent(emp.getId(), k -> new AbsenceAccumulator(emp));

            if (log.getAction() == WorkLogAction.ADD_ABSENCE) {
                acc.absenceCount++;
            } else if (log.getAction() == WorkLogAction.REMOVE_ABSENCE) {
                acc.absenceCount = Math.max(0, acc.absenceCount - 1);
            }
        }

        return accumulators.values().stream()
                .map(acc -> EmployeeAbsenceRankingDTO.builder()
                        .employeeId(acc.employee.getId())
                        .fullName(acc.employee.getFullName())
                        .role(acc.employee.getRole().name())
                        .absenceCount(acc.absenceCount)
                        .scheduledDays(acc.scheduledDays)
                        .build())
                .sorted(Comparator.comparingInt(EmployeeAbsenceRankingDTO::getAbsenceCount).reversed())
                .toList();
    }

    // ──────────────────────────────────────────────────────────
    // POSITION FREQUENCY
    // ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PositionFrequencyDTO> getPositionFrequency(Long employeeId, LocalDate from, LocalDate to) {
        LocalDate weekFrom = normalizeToMonday(from);
        LocalDate weekTo = normalizeToMonday(to);

        List<WeeklySchedule> schedules = weeklyScheduleRepository
                .findByEmployeeIdAndWeekStartDateBetween(employeeId, weekFrom, weekTo);

        Map<String, Integer> frequencyMap = new LinkedHashMap<>();

        for (WeeklySchedule ws : schedules) {
            LocalDate actualDate = ws.getActualDate();
            if (actualDate.isBefore(from) || actualDate.isAfter(to)) {
                continue;
            }

            if (ws.getAssignedAttraction() != null) {
                String key = "ATTRACTION:" + ws.getAssignedAttraction().getName();
                frequencyMap.merge(key, 1, (a, b) -> a + b);
            } else if (ws.getAssignedZone() != null) {
                String key = "ZONE:" + ws.getAssignedZone().getZoneName().name();
                frequencyMap.merge(key, 1, (a, b) -> a + b);
            } else {
                frequencyMap.merge("GENERAL:General", 1, (a, b) -> a + b);
            }
        }

        return frequencyMap.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split(":", 2);
                    return PositionFrequencyDTO.builder()
                            .positionType(parts[0])
                            .positionName(parts[1])
                            .assignmentCount(entry.getValue())
                            .build();
                })
                .sorted(Comparator.comparingInt(PositionFrequencyDTO::getAssignmentCount).reversed())
                .toList();
    }

    // ──────────────────────────────────────────────────────────
    // SALARY — uses hours ranking (which already includes adjustments)
    // ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SalaryReportDTO> getSalaryReport(LocalDate from, LocalDate to, String locale) {
        List<EmployeeHoursRankingDTO> hoursRanking = getHoursRanking(from, to);
        String currency = CurrencyConverter.getCurrency(locale);

        return hoursRanking.stream()
                .map(hr -> {
                    EmployeeRole role = EmployeeRole.valueOf(hr.getRole());
                    BigDecimal hourlyRate = SalaryRateConfig.getHourlyRate(role);
                    BigDecimal overtimeRate = SalaryRateConfig.getOvertimeRate(role);

                    BigDecimal normalPay = hr.getNormalHours()
                            .multiply(hourlyRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal overtimePay = hr.getOvertimeHours()
                            .multiply(overtimeRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal totalSalary = normalPay.add(overtimePay);

                    return SalaryReportDTO.builder()
                            .employeeId(hr.getEmployeeId())
                            .fullName(hr.getFullName())
                            .role(hr.getRole())
                            .normalHours(hr.getNormalHours())
                            .overtimeHours(hr.getOvertimeHours())
                            .hourlyRate(CurrencyConverter.convert(hourlyRate, locale))
                            .overtimeRate(CurrencyConverter.convert(overtimeRate, locale))
                            .normalPay(CurrencyConverter.convert(normalPay, locale))
                            .overtimePay(CurrencyConverter.convert(overtimePay, locale))
                            .totalSalary(CurrencyConverter.convert(totalSalary, locale))
                            .currency(currency)
                            .build();
                })
                .sorted(Comparator.comparing(SalaryReportDTO::getTotalSalary).reversed())
                .toList();
    }

    // ──────────────────────────────────────────────────────────
    // UTILITY
    // ──────────────────────────────────────────────────────────

    private LocalDate normalizeToMonday(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    private static class HoursAccumulator {
        final Employee employee;
        BigDecimal normalHours = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;

        HoursAccumulator(Employee employee) {
            this.employee = employee;
        }
    }

    private static class AbsenceAccumulator {
        final Employee employee;
        int scheduledDays = 0;
        int absenceCount = 0;

        AbsenceAccumulator(Employee employee) {
            this.employee = employee;
        }
    }
}
