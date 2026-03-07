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
        if (action == WorkLogAction.REMOVE_SCHEDULED_DAY
                || action == WorkLogAction.ADD_ABSENCE
                || action == WorkLogAction.PARTIAL_ABSENCE) {
            if (!isEmployeeScheduledOnDate(employee.getId(), targetDate)) {
                throw new IllegalArgumentException("error.worklog.not.scheduled");
            }
        }

        // REMOVE_ABSENCE: only if there are active absences for that employee on that date
        if (action == WorkLogAction.REMOVE_ABSENCE) {
            if (!hasActiveAbsence(employee.getId(), targetDate)) {
                throw new IllegalArgumentException("error.worklog.no.absence");
            }
        }

        // Build WorkLog
        WorkLog.WorkLogBuilder builder = WorkLog.builder()
                .employee(employee)
                .targetDate(targetDate)
                .action(action)
                .hoursAffected(request.getHoursAffected())
                .isOvertime(Boolean.TRUE.equals(request.getIsOvertime()))
                .reason(request.getReason())
                .performedBy(adminUsername)
                .createdAt(LocalDateTime.now());

        // On ADD_ABSENCE: save schedule snapshot before removing
        if (action == WorkLogAction.ADD_ABSENCE) {
            WeeklySchedule ws = findScheduleEntry(employee.getId(), targetDate);
            if (ws != null) {
                builder.snapshotAttraction(ws.getAssignedAttraction())
                       .snapshotZone(ws.getAssignedZone())
                       .snapshotShift(ws.getShift())
                       .snapshotBreakGroup(ws.getBreakGroup());
            }
        }

        WorkLog saved = workLogRepository.save(builder.build());

        // Sync schedule
        if (action == WorkLogAction.ADD_ABSENCE || action == WorkLogAction.REMOVE_SCHEDULED_DAY) {
            removeFromSchedule(employee.getId(), targetDate);
        } else if (action == WorkLogAction.REMOVE_ABSENCE) {
            restoreScheduleFromAbsence(employee.getId(), targetDate);
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

    private boolean isEmployeeScheduledOnDate(Long employeeId, LocalDate date) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        java.time.DayOfWeek dow = date.getDayOfWeek();

        return weeklyScheduleRepository.findByEmployeeIdAndWeekStartDateBetween(employeeId, weekStart, weekStart)
                .stream()
                .anyMatch(ws -> ws.getDayOfWeek() == dow);
    }

    private boolean hasActiveAbsence(Long employeeId, LocalDate date) {
        List<WorkLog> logs = workLogRepository
                .findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(employeeId, date, date);

        int absenceCount = 0;
        for (WorkLog log : logs) {
            if (log.getAction() == WorkLogAction.ADD_ABSENCE) absenceCount++;
            if (log.getAction() == WorkLogAction.REMOVE_ABSENCE) absenceCount--;
        }
        return absenceCount > 0;
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
    //   scheduledDays / scheduledHours → from WeeklySchedule only
    //   absences → from WorkLog only (ADD_ABSENCE minus REMOVE_ABSENCE)
    //   workedDays = scheduledDays - absences
    //   normalHoursWorked = scheduledNormalHours - absenceHours - partialAbsenceHours
    //   overtimeHours = scheduledOvertimeHours + worklog ADD_OVERTIME_HOURS
    //   totalHours = normalHoursWorked + overtimeHours

    @Transactional(readOnly = true)
    public EmployeeHoursSummaryDTO getEmployeeSummary(Long employeeId, LocalDate from, LocalDate to) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));

        ScheduleBaseline baseline = calculateBaseline(employeeId, from, to);
        WorkLogAdjustments adj = calculateWorkLogAdjustments(employeeId, from, to);

        int absences = Math.max(0, adj.absences);
        int workedDays = Math.max(0, baseline.scheduledDays - absences);

        BigDecimal normalHours = baseline.normalHours
                .subtract(adj.absenceHours)
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

        for (WeeklySchedule ws : schedules) {
            LocalDate actualDate = ws.getActualDate();
            if (actualDate.isBefore(from) || actualDate.isAfter(to)) {
                continue;
            }

            scheduledDays++;
            BigDecimal shiftHours = calculateEffectiveHours(ws);

            if (Boolean.TRUE.equals(ws.getIsOvertime())) {
                overtimeHours = overtimeHours.add(shiftHours);
            } else {
                normalHours = normalHours.add(shiftHours);
            }
        }

        return new ScheduleBaseline(normalHours, overtimeHours, normalHours.add(overtimeHours), scheduledDays);
    }

    // ── Private: WorkLog Adjustments (only from WorkLog entries) ──

    private WorkLogAdjustments calculateWorkLogAdjustments(Long employeeId, LocalDate from, LocalDate to) {
        List<WorkLog> logs = workLogRepository
                .findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(employeeId, from, to);

        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal absenceHours = BigDecimal.ZERO;
        BigDecimal partialAbsenceHours = BigDecimal.ZERO;
        int absences = 0;

        for (WorkLog log : logs) {
            switch (log.getAction()) {
                case ADD_OVERTIME_HOURS -> overtimeHours = overtimeHours.add(log.getHoursAffected());
                case ADD_ABSENCE -> {
                    absences++;
                    absenceHours = absenceHours.add(log.getHoursAffected());
                }
                case REMOVE_ABSENCE -> {
                    absences--;
                    absenceHours = absenceHours.subtract(log.getHoursAffected());
                }
                case PARTIAL_ABSENCE -> partialAbsenceHours = partialAbsenceHours.add(log.getHoursAffected());
                case REMOVE_SCHEDULED_DAY -> absenceHours = absenceHours.add(log.getHoursAffected());
            }
        }

        return new WorkLogAdjustments(overtimeHours, absenceHours.max(BigDecimal.ZERO),
                partialAbsenceHours, Math.max(0, absences));
    }

    // ── Utility ──

    private void removeFromSchedule(Long employeeId, LocalDate date) {
        WeeklySchedule ws = findScheduleEntry(employeeId, date);
        if (ws != null) {
            weeklyScheduleRepository.deleteById(ws.getId());
        }
    }

    /**
     * Restore the schedule entry that was removed when the absence was registered.
     * Also remove any overtime replacement that was covering this position.
     */
    private void restoreScheduleFromAbsence(Long employeeId, LocalDate date) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        DayOfWeek dow = date.getDayOfWeek();

        // Find the original ADD_ABSENCE WorkLog with the snapshot
        WorkLog absenceLog = workLogRepository
                .findByEmployeeIdAndTargetDateBetweenOrderByCreatedAtDesc(employeeId, date, date)
                .stream()
                .filter(wl -> wl.getAction() == WorkLogAction.ADD_ABSENCE && wl.getSnapshotShift() != null)
                .findFirst()
                .orElse(null);

        if (absenceLog == null) {
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
     * operating hours (capped at 8h max). Otherwise use the shift hours (8h).
     */
    public static BigDecimal calculateEffectiveHours(WeeklySchedule ws) {
        if (ws.getAssignedAttraction() != null) {
            Attraction attr = ws.getAssignedAttraction();
            LocalTime open = attr.getOpeningTime();
            LocalTime close = attr.getClosingTime();
            if (open != null && close != null && close.isAfter(open)) {
                long attractionMinutes = Duration.between(open, close).toMinutes();
                long effectiveMinutes = Math.min(attractionMinutes, 480); // 8h max
                return BigDecimal.valueOf(effectiveMinutes)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            }
        }
        return calculateShiftHours(ws.getShift());
    }

    static BigDecimal calculateShiftHours(WorkShift shift) {
        long minutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
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
                                     BigDecimal totalScheduled, int scheduledDays) {}

    private record WorkLogAdjustments(BigDecimal overtimeHours, BigDecimal absenceHours,
                                       BigDecimal partialAbsenceHours, int absences) {}
}

