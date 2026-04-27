package com.magicworld.tfg_angular_springboot.employee.service;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.employee.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkLogService {

    private static final BigDecimal MAX_NORMAL_WEEKLY_HOURS = new BigDecimal("40");
    private static final BigDecimal DEFAULT_FULL_DAY_HOURS = new BigDecimal("8.00");
    private static final String AUTO_ABSENCE_OVERTIME_DEDUCTION_REASON = "__AUTO_ABSENCE_OVERTIME_DEDUCTION__";
    private static final String AUTO_ABSENCE_OVERTIME_RESTORE_REASON = "__AUTO_ABSENCE_OVERTIME_RESTORE__";

    private final WorkLogRepository workLogRepository;
    private final EmployeeRepository employeeRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;

    @Transactional
    public WorkLogEntryDTO addWorkLogEntry(WorkLogEntryRequest request, String adminUsername) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));

        LocalDate targetDate = request.getTargetDate();
        WorkLogAction action = request.getAction();

        // No past dates allowed
        if (targetDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("error.worklog.past.date");
        }

        // For REMOVE_SCHEDULED_DAY, ADD_ABSENCE, PARTIAL_ABSENCE: employee must be scheduled that day
        WeeklySchedule scheduleEntry = null;
        if (action == WorkLogAction.REMOVE_SCHEDULED_DAY
                || action == WorkLogAction.ADD_ABSENCE
                || action == WorkLogAction.PARTIAL_ABSENCE) {
            scheduleEntry = findScheduleEntry(employee.getId(), targetDate);
            if (scheduleEntry == null) {
                throw new IllegalArgumentException("error.worklog.not.scheduled");
            }
        }

        // REMOVE_ABSENCE: only if there are active absences for that employee on that date
        WorkLog activeAbsenceLog = null;
        if (action == WorkLogAction.REMOVE_ABSENCE) {
            activeAbsenceLog = findActiveAbsenceLog(employee.getId(), targetDate);
            if (activeAbsenceLog == null) {
                throw new IllegalArgumentException("error.worklog.no.absence");
            }
        }

        BigDecimal hoursAffected = resolveHoursAffected(request, employee, action, scheduleEntry, activeAbsenceLog);

        // Build WorkLog
        WorkLog.WorkLogBuilder builder = WorkLog.builder()
                .employee(employee)
                .targetDate(targetDate)
                .action(action)
                .hoursAffected(hoursAffected)
                .isOvertime(action == WorkLogAction.ADD_OVERTIME_HOURS && Boolean.TRUE.equals(request.getIsOvertime()))
                .reason(request.getReason())
                .performedBy(adminUsername)
                .createdAt(LocalDateTime.now());

        // On ADD_ABSENCE: save schedule snapshot before removing
        if (action == WorkLogAction.ADD_ABSENCE) {
            if (scheduleEntry != null) {
                builder.snapshotAttraction(scheduleEntry.getAssignedAttraction())
                       .snapshotZone(scheduleEntry.getAssignedZone())
                       .snapshotShift(scheduleEntry.getShift())
                       .snapshotBreakGroup(scheduleEntry.getBreakGroup());
            }
        }

        WorkLog saved = workLogRepository.save(builder.build());

        // Sync schedule
        if (action == WorkLogAction.ADD_ABSENCE || action == WorkLogAction.REMOVE_SCHEDULED_DAY) {
            removeFromSchedule(scheduleEntry);
            if (action == WorkLogAction.ADD_ABSENCE) {
                applyAbsenceOvertimeCompensation(employee, targetDate, adminUsername);
            }
        } else if (action == WorkLogAction.REMOVE_ABSENCE) {
            restoreScheduleFromAbsence(employee.getId(), targetDate, activeAbsenceLog);
            restoreAbsenceOvertimeCompensation(employee, targetDate, adminUsername);
        }

        return toDTO(saved);
    }

    // ── Schedule sync helpers ──

    private WeeklySchedule findScheduleEntry(Long employeeId, LocalDate date) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        DayOfWeek dow = date.getDayOfWeek();

        return weeklyScheduleRepository.findByEmployeeIdAndWeekStartDateBetween(employeeId, weekStart, weekStart)
                .stream()
                .filter(ws -> ws.getDayOfWeek() == dow)
                .findFirst()
                .orElse(null);
    }

    private WorkLog findActiveAbsenceLog(Long employeeId, LocalDate date) {
        List<WorkLog> logs = workLogRepository
                .findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(employeeId, date, date);

        int pendingRemovals = 0;
        for (WorkLog log : logs) {
            if (log.getAction() == WorkLogAction.REMOVE_ABSENCE) {
                pendingRemovals++;
                continue;
            }
            if (log.getAction() == WorkLogAction.ADD_ABSENCE) {
                if (pendingRemovals > 0) {
                    pendingRemovals--;
                    continue;
                }
                return log;
            }
        }
        return null;
    }

    // ── History ──

    @Transactional(readOnly = true)
    public List<WorkLogEntryDTO> getWorkLogHistory(Long employeeId, LocalDate from, LocalDate to) {
        return workLogRepository
                .findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(employeeId, from, to)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Employee Summary ──
    // Rules:
    //   scheduledDays / workedDays / scheduledHours → from WeeklySchedule
    //   absences → from WorkLog only (ADD_ABSENCE minus REMOVE_ABSENCE)
    //   workedDays = scheduledDays (full-day absences remove the schedule entry)
    //   normalHoursWorked = scheduledNormalHours - partialAbsenceHours
    //   overtimeHours = scheduledOvertimeHours + worklog ADD_OVERTIME_HOURS
    //   totalHours = normalHoursWorked + overtimeHours

    @Transactional(readOnly = true)
    public EmployeeHoursSummaryDTO getEmployeeSummary(Long employeeId, LocalDate from, LocalDate to) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));

        ScheduleBaseline baseline = calculateBaseline(employeeId, from, to);
        WorkLogAdjustments adj = calculateWorkLogAdjustments(employeeId, from, to);

        int absences = Math.max(0, adj.absences);
        int workedDays = baseline.scheduledDays;

        BigDecimal normalHours = baseline.normalHours
                .subtract(adj.partialAbsenceHours)
                .max(BigDecimal.ZERO);

        BigDecimal overtimeHours = baseline.overtimeHours
                .add(adj.overtimeHours)
                .max(BigDecimal.ZERO);

        // Rebalance: if total ≤ 40h, overtime becomes normal
        BigDecimal totalHours = normalHours.add(overtimeHours);
        if (totalHours.compareTo(MAX_NORMAL_WEEKLY_HOURS) <= 0) {
            normalHours = totalHours;
            overtimeHours = BigDecimal.ZERO;
        } else if (normalHours.compareTo(MAX_NORMAL_WEEKLY_HOURS) < 0) {
            // Normal fills up to 40h, the rest is overtime
            overtimeHours = totalHours.subtract(MAX_NORMAL_WEEKLY_HOURS);
            normalHours = MAX_NORMAL_WEEKLY_HOURS;
        }

        List<WorkLogEntryDTO> logEntries = getWorkLogHistory(employeeId, from, to);

        return EmployeeHoursSummaryDTO.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .role(employee.getRole().name())
                .scheduledHours(baseline.totalScheduled)
                .normalHoursWorked(normalHours)
                .overtimeHours(overtimeHours)
                .totalHoursWorked(totalHours)
                .absences(absences)
                .scheduledDays(baseline.scheduledDays)
                .workedDays(workedDays)
                .reinforcementDays(baseline.reinforcementDays)
                .adjustments(logEntries)
                .build();
    }

    // ── Private: Schedule Baseline (from WeeklySchedule) ──

    private ScheduleBaseline calculateBaseline(Long employeeId, LocalDate from, LocalDate to) {
        LocalDate weekFrom = normalizeToMonday(from);
        LocalDate weekTo = normalizeToMonday(to);

        List<WeeklySchedule> schedules = weeklyScheduleRepository
                .findByEmployeeIdAndWeekStartDateBetween(employeeId, weekFrom, weekTo);

        BigDecimal normalHours = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;
        int scheduledDays = 0;
        int reinforcementDays = 0;

        for (WeeklySchedule ws : schedules) {
            LocalDate actualDate = ws.getActualDate();
            if (actualDate.isBefore(from) || actualDate.isAfter(to)) {
                continue;
            }

            scheduledDays++;
            if (Boolean.TRUE.equals(ws.getIsReinforcement())) {
                reinforcementDays++;
            }
            BigDecimal shiftHours = calculateEffectiveHours(ws);

            if (Boolean.TRUE.equals(ws.getIsOvertime())) {
                overtimeHours = overtimeHours.add(shiftHours);
            } else {
                normalHours = normalHours.add(shiftHours);
            }
        }

        return new ScheduleBaseline(normalHours, overtimeHours, normalHours.add(overtimeHours), scheduledDays, reinforcementDays);
    }

    // ── Private: WorkLog Adjustments (only from WorkLog entries) ──

    private WorkLogAdjustments calculateWorkLogAdjustments(Long employeeId, LocalDate from, LocalDate to) {
        List<WorkLog> logs = workLogRepository
                .findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(employeeId, from, to);

        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal partialAbsenceHours = BigDecimal.ZERO;
        int absences = 0;

        for (WorkLog log : logs) {
            switch (log.getAction()) {
                case ADD_OVERTIME_HOURS -> overtimeHours = overtimeHours.add(log.getHoursAffected());
                case ADD_ABSENCE -> absences++;
                case REMOVE_ABSENCE -> absences--;
                case PARTIAL_ABSENCE -> partialAbsenceHours = partialAbsenceHours.add(log.getHoursAffected());
                case REMOVE_SCHEDULED_DAY -> {
                    // Full-day schedule removals are already reflected in WeeklySchedule baseline.
                }
            }
        }

        return new WorkLogAdjustments(overtimeHours, partialAbsenceHours, Math.max(0, absences));
    }

    private void applyAbsenceOvertimeCompensation(Employee employee, LocalDate targetDate, String adminUsername) {
        BigDecimal currentOvertime = getNetOvertimeHoursForDate(employee.getId(), targetDate);
        if (currentOvertime.compareTo(BigDecimal.ZERO) > 0) {
            persistOvertimeAdjustment(
                    employee,
                    targetDate,
                    currentOvertime.negate(),
                    adminUsername,
                    AUTO_ABSENCE_OVERTIME_DEDUCTION_REASON
            );
        }
    }

    private void restoreAbsenceOvertimeCompensation(Employee employee, LocalDate targetDate, String adminUsername) {
        BigDecimal pendingCompensation = findPendingAbsenceOvertimeCompensation(employee.getId(), targetDate);
        if (pendingCompensation.compareTo(BigDecimal.ZERO) > 0) {
            persistOvertimeAdjustment(
                    employee,
                    targetDate,
                    pendingCompensation,
                    adminUsername,
                    AUTO_ABSENCE_OVERTIME_RESTORE_REASON
            );
        }
    }

    private BigDecimal getNetOvertimeHoursForDate(Long employeeId, LocalDate date) {
        List<WorkLog> logs = workLogRepository
                .findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(employeeId, date, date);

        return logs.stream()
                .filter(log -> log.getAction() == WorkLogAction.ADD_OVERTIME_HOURS)
                .map(WorkLog::getHoursAffected)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal findPendingAbsenceOvertimeCompensation(Long employeeId, LocalDate date) {
        List<WorkLog> logs = workLogRepository
                .findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(employeeId, date, date);

        int pendingRestores = 0;
        for (WorkLog log : logs) {
            if (log.getAction() != WorkLogAction.ADD_OVERTIME_HOURS) {
                continue;
            }

            if (isAutoAbsenceOvertimeRestore(log)) {
                pendingRestores++;
                continue;
            }

            if (isAutoAbsenceOvertimeDeduction(log)) {
                if (pendingRestores > 0) {
                    pendingRestores--;
                    continue;
                }
                return log.getHoursAffected().abs();
            }
        }

        return BigDecimal.ZERO;
    }

    private boolean isAutoAbsenceOvertimeDeduction(WorkLog log) {
        return AUTO_ABSENCE_OVERTIME_DEDUCTION_REASON.equals(log.getReason());
    }

    private boolean isAutoAbsenceOvertimeRestore(WorkLog log) {
        return AUTO_ABSENCE_OVERTIME_RESTORE_REASON.equals(log.getReason());
    }

    private void persistOvertimeAdjustment(
            Employee employee,
            LocalDate targetDate,
            BigDecimal hours,
            String adminUsername,
            String reason) {
        workLogRepository.save(WorkLog.builder()
                .employee(employee)
                .targetDate(targetDate)
                .action(WorkLogAction.ADD_OVERTIME_HOURS)
                .hoursAffected(hours)
                .isOvertime(true)
                .reason(reason)
                .performedBy(adminUsername)
                .createdAt(LocalDateTime.now())
                .build());
    }

    // ── Utility ──

    private void removeFromSchedule(WeeklySchedule scheduleEntry) {
        if (scheduleEntry != null) {
            weeklyScheduleRepository.deleteById(scheduleEntry.getId());
        }
    }

    /**
     * Restore the schedule entry that was removed when the absence was registered.
     * Also remove any overtime replacement that was covering this position.
     */
    private void restoreScheduleFromAbsence(Long employeeId, LocalDate date, WorkLog absenceLog) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        DayOfWeek dow = date.getDayOfWeek();

        if (absenceLog == null || absenceLog.getSnapshotShift() == null) {
            return;
        }

        // Check if someone else is covering this position as overtime on the same day
        removeOvertimeReplacement(weekStart, dow, absenceLog);

        // Restore the original employee's schedule entry
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));

        WeeklySchedule restored = WeeklySchedule.builder()
                .employee(employee)
                .weekStartDate(weekStart)
                .dayOfWeek(dow)
                .shift(absenceLog.getSnapshotShift())
                .assignedAttraction(absenceLog.getSnapshotAttraction())
                .assignedZone(absenceLog.getSnapshotZone())
                .breakGroup(absenceLog.getSnapshotBreakGroup() != null
                        ? absenceLog.getSnapshotBreakGroup() : BreakGroup.A)
                .isOvertime(false)
                .build();

        weeklyScheduleRepository.save(restored);
    }

    /**
     * If another employee was assigned as overtime to cover the same attraction/zone
     * on the same day, remove that overtime assignment.
     */
    private void removeOvertimeReplacement(LocalDate weekStart, DayOfWeek dow, WorkLog absenceLog) {
        List<WeeklySchedule> sameDaySchedules = weeklyScheduleRepository
                .findByWeekStartDateAndDayOfWeek(weekStart, dow);

        for (WeeklySchedule ws : sameDaySchedules) {
            if (!Boolean.TRUE.equals(ws.getIsOvertime())) {
                continue;
            }
            // Match by attraction or zone
            boolean sameAttraction = absenceLog.getSnapshotAttraction() != null
                    && ws.getAssignedAttraction() != null
                    && ws.getAssignedAttraction().getId().equals(absenceLog.getSnapshotAttraction().getId());
            boolean sameZone = absenceLog.getSnapshotZone() != null
                    && ws.getAssignedZone() != null
                    && ws.getAssignedZone().getId().equals(absenceLog.getSnapshotZone().getId());

            if (sameAttraction || sameZone) {
                weeklyScheduleRepository.deleteById(ws.getId());
                break; // Only remove one replacement
            }
        }
    }

    /**
     * Calculate the effective working hours for a schedule entry.
     * If the employee is assigned to an attraction, use the attraction's
     * operating hours. Otherwise use shift hours.
     */
    public static BigDecimal calculateEffectiveHours(WeeklySchedule ws) {
        if (ws.getAssignedAttraction() != null) {
            Attraction attr = ws.getAssignedAttraction();
            LocalTime open = attr.getOpeningTime();
            LocalTime close = attr.getClosingTime();
            if (open != null && close != null && close.isAfter(open)) {
                long attractionMinutes = Duration.between(open, close).toMinutes();
                return BigDecimal.valueOf(attractionMinutes)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            }
        }
        // If the attraction was deleted, use the snapshot hours preserved at deletion time
        if (ws.getSnapshotEffectiveHours() != null) {
            return ws.getSnapshotEffectiveHours();
        }
        return calculateShiftHours(ws.getShift());
    }

    static BigDecimal calculateShiftHours(WorkShift shift) {
        long minutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveHoursAffected(
            WorkLogEntryRequest request,
            Employee employee,
            WorkLogAction action,
            WeeklySchedule scheduleEntry,
            WorkLog activeAbsenceLog) {
        return switch (action) {
            case ADD_ABSENCE, REMOVE_SCHEDULED_DAY -> calculateFullDayHours(employee, scheduleEntry);
            case REMOVE_ABSENCE -> activeAbsenceLog.getHoursAffected();
            case ADD_OVERTIME_HOURS, PARTIAL_ABSENCE -> request.getHoursAffected();
        };
    }

    private BigDecimal calculateFullDayHours(Employee employee, WeeklySchedule scheduleEntry) {
        if (employee.getRole() == EmployeeRole.OPERATOR
                && scheduleEntry != null
                && scheduleEntry.getAssignedAttraction() != null) {
            Attraction attraction = scheduleEntry.getAssignedAttraction();
            LocalTime open = attraction.getOpeningTime();
            LocalTime close = attraction.getClosingTime();
            if (open != null && close != null && close.isAfter(open)) {
                long minutes = Duration.between(open, close).toMinutes();
                return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            }
        }
        return DEFAULT_FULL_DAY_HOURS;
    }

    private LocalDate normalizeToMonday(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    private WorkLogEntryDTO toDTO(WorkLog wl) {
        return WorkLogEntryDTO.builder()
                .id(wl.getId())
                .employeeId(wl.getEmployee().getId())
                .employeeName(wl.getEmployee().getFullName())
                .targetDate(wl.getTargetDate())
                .action(wl.getAction())
                .hoursAffected(wl.getHoursAffected())
                .isOvertime(wl.getIsOvertime())
                .reason(wl.getReason())
                .performedBy(wl.getPerformedBy())
                .createdAt(wl.getCreatedAt())
                .build();
    }

    // ── Records ──

    private record ScheduleBaseline(BigDecimal normalHours, BigDecimal overtimeHours,
                                     BigDecimal totalScheduled, int scheduledDays, int reinforcementDays) {}

    private record WorkLogAdjustments(BigDecimal overtimeHours,
                                      BigDecimal partialAbsenceHours, int absences) {}
}

