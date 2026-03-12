package com.magicworld.tfg_angular_springboot.employee.service;

import com.magicworld.tfg_angular_springboot.attraction.*;
import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.employee.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final WeeklyScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final AttractionRepository attractionRepository;
    private final ParkZoneRepository zoneRepository;
    private final WorkLogRepository workLogRepository;

    @Transactional(readOnly = true)
    public List<WeeklyScheduleDTO> getWeekSchedule(LocalDate weekStart) {
        LocalDate normalizedWeekStart = normalizeToMonday(weekStart);
        return scheduleRepository.findByWeekWithEmployee(normalizedWeekStart).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WeeklyScheduleDTO> getEmployeeSchedule(Long employeeId, LocalDate weekStart) {
        LocalDate normalizedWeekStart = normalizeToMonday(weekStart);
        return scheduleRepository.findByEmployeeIdAndWeekStartDate(employeeId, normalizedWeekStart).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public WeeklyScheduleDTO createScheduleEntry(CreateScheduleRequest request) {
        LocalDate normalizedWeekStart = normalizeToMonday(request.getWeekStartDate());

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));

        validateAssignment(employee.getRole(), request);

        // Prevent assigning an employee who has an active absence on this day
        LocalDate actualDate = normalizedWeekStart.plusDays(request.getDayOfWeek().getValue() - 1);
        if (isEmployeeAbsentOnDate(employee.getId(), actualDate)) {
            throw new IllegalArgumentException("error.schedule.employee.has.absence");
        }

        // Prevent duplicate attraction/zone assignments (except reinforcements from monitoring)
        validateNoDuplicateLocation(normalizedWeekStart, request.getDayOfWeek(), request);

        boolean isOvertime = checkAndValidateDays(employee.getId(), normalizedWeekStart, request.getDayOfWeek());

        WeeklySchedule schedule = WeeklySchedule.builder()
                .employee(employee)
                .weekStartDate(normalizedWeekStart)
                .dayOfWeek(request.getDayOfWeek())
                .shift(request.getShift())
                .breakGroup(request.getBreakGroup())
                .isOvertime(isOvertime)
                .build();

        assignLocation(schedule, request);
        return toDTO(scheduleRepository.save(schedule));
    }

    /**
     * Returns true if this assignment should be marked as overtime.
     * Rules:
     * - Cannot assign the same day twice.
     * - Up to 5 days: normal assignment (returns false).
     * - 6th/7th day: only allowed if there are coverage issues on that day (returns
     * true = overtime).
     */
    private boolean checkAndValidateDays(Long employeeId, LocalDate weekStart, DayOfWeek newDay) {
        List<WeeklySchedule> existingSchedules = scheduleRepository
                .findByEmployeeIdAndWeekStartDate(employeeId, weekStart);

        boolean alreadyAssigned = existingSchedules.stream()
                .anyMatch(s -> s.getDayOfWeek() == newDay);
        if (alreadyAssigned) {
            throw new IllegalArgumentException("error.schedule.already.assigned");
        }

        if (existingSchedules.size() < 5) {
            return false; // Normal day, no overtime
        }

        // 5+ days already — check if there are coverage issues to justify overtime
        if (hasCoverageIssuesOnDay(weekStart, newDay)) {
            return true; // Allow as overtime
        }

        throw new IllegalArgumentException("error.schedule.max.days.exceeded");
    }

    private boolean hasCoverageIssuesOnDay(LocalDate weekStart, DayOfWeek day) {
        LocalDate date = weekStart.plusDays(day.getValue() - 1);
        List<Attraction> activeAttractions = attractionRepository.findByIsActiveTrue();
        List<ParkZone> zones = zoneRepository.findAll();

        Map<LocalDate, Set<Long>> absentByDate = buildAbsentMap(weekStart, weekStart.plusDays(6));
        Set<Long> absentIds = absentByDate.getOrDefault(date, Set.of());

        List<CoverageValidationResult.CoverageIssue> issues = validateDayCoverage(weekStart, day, date,
                activeAttractions, zones, absentIds);

        return !issues.isEmpty();
    }

    @Transactional
    public void deleteScheduleEntry(Long id) {

        scheduleRepository.deleteById(id);
    }

    @Transactional
    public void copyPreviousWeek(LocalDate targetWeekStart) {
        LocalDate normalizedTarget = normalizeToMonday(targetWeekStart);
        LocalDate previousWeek = normalizedTarget.minusWeeks(1);

        List<WeeklySchedule> previousSchedules = scheduleRepository.findByWeekStartDate(previousWeek);

        // Build absent map for the target week to skip employees with absences
        Map<LocalDate, Set<Long>> absentByDate = buildAbsentMap(normalizedTarget, normalizedTarget.plusDays(6));

        for (WeeklySchedule prev : previousSchedules) {
            if (prev.getEmployee().getStatus() != EmployeeStatus.ACTIVE)
                continue;

            // Skip if employee has an active absence on this day in the target week
            LocalDate actualDate = normalizedTarget.plusDays(prev.getDayOfWeek().getValue() - 1);
            Set<Long> absentIds = absentByDate.getOrDefault(actualDate, Set.of());
            if (absentIds.contains(prev.getEmployee().getId()))
                continue;

            WeeklySchedule newSchedule = WeeklySchedule.builder()
                    .employee(prev.getEmployee())
                    .weekStartDate(normalizedTarget)
                    .dayOfWeek(prev.getDayOfWeek())
                    .shift(prev.getShift())
                    .assignedZone(prev.getAssignedZone())
                    .assignedAttraction(prev.getAssignedAttraction())
                    .breakGroup(prev.getBreakGroup())
                    .build();

            scheduleRepository.save(newSchedule);
        }
    }

    @Transactional
    public void autoAssignWeek(LocalDate weekStart) {
        LocalDate normalizedWeekStart = normalizeToMonday(weekStart);
        scheduleRepository.deleteByWeekStartDate(normalizedWeekStart);
        scheduleRepository.flush();

        List<Employee> operators = employeeRepository.findByRoleAndStatus(EmployeeRole.OPERATOR, EmployeeStatus.ACTIVE);
        List<Employee> security = employeeRepository.findByRoleAndStatus(EmployeeRole.SECURITY, EmployeeStatus.ACTIVE);
        List<Employee> medical = employeeRepository.findByRoleAndStatus(EmployeeRole.MEDICAL, EmployeeStatus.ACTIVE);
        List<Employee> maintenance = employeeRepository.findByRoleAndStatus(EmployeeRole.MAINTENANCE,
                EmployeeStatus.ACTIVE);
        List<Employee> guestServices = employeeRepository.findByRoleAndStatus(EmployeeRole.GUEST_SERVICES,
                EmployeeStatus.ACTIVE);

        List<Attraction> attractions = attractionRepository.findByIsActiveTrue();
        List<ParkZone> zones = zoneRepository.findAll();

        // Build absent map for the whole week to skip absent employees
        Map<LocalDate, Set<Long>> absentByDate = buildAbsentMap(normalizedWeekStart, normalizedWeekStart.plusDays(6));

        assignOperatorsWithRotation(operators, attractions, normalizedWeekStart, absentByDate);
        assignSecurityWithRotation(security, zones, normalizedWeekStart, absentByDate);
        assignRoleWithRotation(medical, normalizedWeekStart, 2, absentByDate);
        assignRoleWithRotation(maintenance, normalizedWeekStart, 3, absentByDate);
        assignRoleWithRotation(guestServices, normalizedWeekStart, 4, absentByDate);
    }

    private void assignOperatorsWithRotation(List<Employee> operators, List<Attraction> attractions,
            LocalDate weekStart, Map<LocalDate, Set<Long>> absentByDate) {
        if (operators.isEmpty() || attractions.isEmpty())
            return;
        BreakGroup[] breakGroups = BreakGroup.values();
        DayOfWeek[] allDays = DayOfWeek.values();

        int[][] restDays = computeRestDays(operators.size(), 0);

        for (int d = 0; d < 7; d++) {
            LocalDate date = weekStart.plusDays(d);
            Set<Long> absentIds = absentByDate.getOrDefault(date, Set.of());

            List<Employee> availableToday = new ArrayList<>();
            List<Integer> availableIndices = new ArrayList<>();
            for (int i = 0; i < operators.size(); i++) {
                if (d != restDays[i][0] && d != restDays[i][1]
                        && !absentIds.contains(operators.get(i).getId())) {
                    availableToday.add(operators.get(i));
                    availableIndices.add(i);
                }
            }

            if (availableToday.isEmpty())
                continue;

            // Each available employee gets exactly one attraction, rotating by day offset
            for (int i = 0; i < availableToday.size(); i++) {
                Employee emp = availableToday.get(i);
                int originalIdx = availableIndices.get(i);
                int attrIdx = (i + d) % attractions.size();
                BreakGroup breakGroup = breakGroups[originalIdx % breakGroups.length];
                createScheduleEntryInternal(emp, weekStart, allDays[d], attractions.get(attrIdx), null, breakGroup);
            }
        }
    }

    private void assignSecurityWithRotation(List<Employee> security, List<ParkZone> zones, LocalDate weekStart,
            Map<LocalDate, Set<Long>> absentByDate) {
        if (security.isEmpty() || zones.isEmpty())
            return;
        BreakGroup[] breakGroups = BreakGroup.values();
        DayOfWeek[] allDays = DayOfWeek.values();

        int[][] restDays = computeRestDays(security.size(), 1);

        for (int d = 0; d < 7; d++) {
            LocalDate date = weekStart.plusDays(d);
            Set<Long> absentIds = absentByDate.getOrDefault(date, Set.of());

            List<Employee> availableToday = new ArrayList<>();
            List<Integer> availableIndices = new ArrayList<>();
            for (int i = 0; i < security.size(); i++) {
                if (d != restDays[i][0] && d != restDays[i][1]
                        && !absentIds.contains(security.get(i).getId())) {
                    availableToday.add(security.get(i));
                    availableIndices.add(i);
                }
            }

            if (availableToday.isEmpty())
                continue;

            // Each available employee gets exactly one zone, rotating by day offset
            for (int i = 0; i < availableToday.size(); i++) {
                Employee emp = availableToday.get(i);
                int originalIdx = availableIndices.get(i);
                int zoneIdx = (i + d) % zones.size();
                BreakGroup breakGroup = breakGroups[originalIdx % breakGroups.length];
                createScheduleEntryInternal(emp, weekStart, allDays[d], null, zones.get(zoneIdx), breakGroup);
            }
        }
    }

    private void assignRoleWithRotation(List<Employee> employees, LocalDate weekStart, int offset,
            Map<LocalDate, Set<Long>> absentByDate) {
        if (employees.isEmpty())
            return;
        BreakGroup[] breakGroups = BreakGroup.values();
        DayOfWeek[] allDays = DayOfWeek.values();

        int[][] restDays = computeRestDays(employees.size(), offset);

        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            BreakGroup breakGroup = breakGroups[i % breakGroups.length];

            for (int d = 0; d < 7; d++) {
                if (d == restDays[i][0] || d == restDays[i][1])
                    continue;

                LocalDate date = weekStart.plusDays(d);
                Set<Long> absentIds = absentByDate.getOrDefault(date, Set.of());
                if (absentIds.contains(emp.getId()))
                    continue;

                createScheduleEntryInternal(emp, weekStart, allDays[d], null, null, breakGroup);
            }
        }
    }

    private int[][] computeRestDays(int employeeCount, int offset) {
        int[][] rest = new int[employeeCount][2];
        for (int i = 0; i < employeeCount; i++) {
            rest[i][0] = (i + offset) % 7;
            rest[i][1] = (i + offset + 3) % 7;
            if (rest[i][0] == rest[i][1])
                rest[i][1] = (rest[i][1] + 1) % 7;
        }
        return rest;
    }

    private void createScheduleEntryInternal(Employee emp, LocalDate weekStart, DayOfWeek day,
            Attraction attr, ParkZone zone, BreakGroup breakGroup) {
        WeeklySchedule schedule = WeeklySchedule.builder()
                .employee(emp)
                .weekStartDate(weekStart)
                .dayOfWeek(day)
                .shift(WorkShift.FULL_DAY)
                .assignedAttraction(attr)
                .assignedZone(zone)
                .breakGroup(breakGroup)
                .build();
        scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public CoverageValidationResult validateWeekCoverage(LocalDate weekStart) {
        LocalDate normalizedWeekStart = normalizeToMonday(weekStart);
        LocalDate weekEnd = normalizedWeekStart.plusDays(6);
        List<CoverageValidationResult.CoverageIssue> issues = new ArrayList<>();

        List<Attraction> activeAttractions = attractionRepository.findByIsActiveTrue();
        List<ParkZone> zones = zoneRepository.findAll();

        // Build maps of date → set of absent employee IDs and employee names from WorkLog
        AbsentInfo absentInfo = buildAbsentInfo(normalizedWeekStart, weekEnd);

        for (int i = 0; i < 7; i++) {
            DayOfWeek day = DayOfWeek.of((i % 7) + 1);
            LocalDate date = normalizedWeekStart.plusDays(i);
            Set<Long> absentIds = absentInfo.absentByDate.getOrDefault(date, Set.of());

            // Add EMPLOYEE_ABSENT issues directly from WorkLog data (independent of schedule)
            for (Long absentEmpId : absentIds) {
                String empName = absentInfo.employeeNames.getOrDefault(absentEmpId, "");
                issues.add(CoverageValidationResult.CoverageIssue.builder()
                        .date(date)
                        .issueType("EMPLOYEE_ABSENT")
                        .description("schedule.issues.EMPLOYEE_ABSENT")
                        .employeeName(empName)
                        .build());
            }

            issues.addAll(validateDayCoverage(normalizedWeekStart, day, date, activeAttractions, zones, absentIds));
        }

        return CoverageValidationResult.builder()
                .valid(issues.isEmpty())
                .weekStartDate(normalizedWeekStart)
                .issues(issues)
                .build();
    }

    /**
     * Check if an employee has an active absence on a specific date.
     */
    private boolean isEmployeeAbsentOnDate(Long employeeId, LocalDate date) {
        Map<LocalDate, Set<Long>> absentMap = buildAbsentMap(date, date);
        Set<Long> absentIds = absentMap.getOrDefault(date, Set.of());
        return absentIds.contains(employeeId);
    }

    private record AbsentInfo(Map<LocalDate, Set<Long>> absentByDate, Map<Long, String> employeeNames) {}

    /**
     * Build absence info including employee names for validation issue descriptions.
     */
    private AbsentInfo buildAbsentInfo(LocalDate from, LocalDate to) {
        List<WorkLog> absenceLogs = workLogRepository.findAllByTargetDateBetween(from, to);

        Map<LocalDate, Map<Long, Integer>> countByDateEmployee = new HashMap<>();
        Map<Long, String> employeeNames = new HashMap<>();
        for (WorkLog log : absenceLogs) {
            if (log.getAction() == WorkLogAction.ADD_ABSENCE || log.getAction() == WorkLogAction.REMOVE_ABSENCE) {
                Long empId = log.getEmployee().getId();
                LocalDate date = log.getTargetDate();
                employeeNames.putIfAbsent(empId, log.getEmployee().getFullName());
                countByDateEmployee.computeIfAbsent(date, k -> new HashMap<>());
                int delta = log.getAction() == WorkLogAction.ADD_ABSENCE ? 1 : -1;
                countByDateEmployee.get(date).merge(empId, delta, (a, b) -> a + b);
            }
        }

        Map<LocalDate, Set<Long>> result = new HashMap<>();
        for (var dateEntry : countByDateEmployee.entrySet()) {
            Set<Long> absentIds = new HashSet<>();
            for (var empEntry : dateEntry.getValue().entrySet()) {
                if (empEntry.getValue() > 0) {
                    absentIds.add(empEntry.getKey());
                }
            }
            if (!absentIds.isEmpty()) {
                result.put(dateEntry.getKey(), absentIds);
            }
        }
        return new AbsentInfo(result, employeeNames);
    }

    private Map<LocalDate, Set<Long>> buildAbsentMap(LocalDate from, LocalDate to) {
        return buildAbsentInfo(from, to).absentByDate;
    }

    private List<CoverageValidationResult.CoverageIssue> validateDayCoverage(
            LocalDate weekStart, DayOfWeek day, LocalDate date,
            List<Attraction> attractions, List<ParkZone> zones, Set<Long> absentIds) {

        List<CoverageValidationResult.CoverageIssue> issues = new ArrayList<>();
        List<WeeklySchedule> allDaySchedules = scheduleRepository.findByWeekStartDateAndDayOfWeek(weekStart, day);


        // Only count non-absent employees for coverage
        List<WeeklySchedule> daySchedules = allDaySchedules.stream()
                .filter(s -> !absentIds.contains(s.getEmployee().getId()))
                .toList();

        Map<Long, List<WeeklySchedule>> byAttraction = daySchedules.stream()
                .filter(s -> s.getAssignedAttraction() != null)
                .collect(Collectors.groupingBy(s -> s.getAssignedAttraction().getId()));

        for (Attraction attr : attractions) {
            if (!byAttraction.containsKey(attr.getId()) || byAttraction.get(attr.getId()).isEmpty()) {
                issues.add(buildAttractionIssue(date, attr));
            }
        }

        Map<Long, List<WeeklySchedule>> byZone = daySchedules.stream()
                .filter(s -> s.getAssignedZone() != null)
                .collect(Collectors.groupingBy(s -> s.getAssignedZone().getId()));

        for (ParkZone zone : zones) {
            if (!byZone.containsKey(zone.getId()) || byZone.get(zone.getId()).isEmpty()) {
                issues.add(buildZoneIssue(date, zone));
            }
        }

        boolean hasMedical = daySchedules.stream()
                .anyMatch(s -> s.getEmployee().getRole() == EmployeeRole.MEDICAL);
        if (!hasMedical) {
            issues.add(buildRoleIssue(date, "NO_MEDICAL"));
        }

        boolean hasMaintenance = daySchedules.stream()
                .anyMatch(s -> s.getEmployee().getRole() == EmployeeRole.MAINTENANCE);
        if (!hasMaintenance) {
            issues.add(buildRoleIssue(date, "NO_MAINTENANCE"));
        }

        boolean hasGuestServices = daySchedules.stream()
                .anyMatch(s -> s.getEmployee().getRole() == EmployeeRole.GUEST_SERVICES);
        if (!hasGuestServices) {
            issues.add(buildRoleIssue(date, "NO_GUEST_SERVICES"));
        }

        return issues;
    }

    private CoverageValidationResult.CoverageIssue buildAttractionIssue(LocalDate date, Attraction attr) {
        return CoverageValidationResult.CoverageIssue.builder()
                .date(date)
                .issueType("NO_OPERATOR")
                .description("schedule.issues.NO_OPERATOR")
                .attractionId(attr.getId())
                .attractionName(attr.getName())
                .build();
    }

    private CoverageValidationResult.CoverageIssue buildZoneIssue(LocalDate date, ParkZone zone) {
        return CoverageValidationResult.CoverageIssue.builder()
                .date(date)
                .issueType("NO_SECURITY")
                .description("schedule.issues.NO_SECURITY")
                .zoneId(zone.getId())
                .zoneName(zone.getZoneName().name())
                .build();
    }

    private CoverageValidationResult.CoverageIssue buildRoleIssue(LocalDate date, String issueType) {
        return CoverageValidationResult.CoverageIssue.builder()
                .date(date)
                .issueType(issueType)
                .description("schedule.issues." + issueType)
                .build();
    }

    private void validateAssignment(EmployeeRole role, CreateScheduleRequest request) {
        if (role == EmployeeRole.OPERATOR && request.getAssignedAttractionId() == null) {
            throw new IllegalArgumentException("error.schedule.operator.needs.attraction");
        }
        if (role == EmployeeRole.SECURITY && request.getAssignedZoneId() == null) {
            throw new IllegalArgumentException("error.schedule.security.needs.zone");
        }
    }
    private void validateNoDuplicateLocation(LocalDate weekStart, DayOfWeek day, CreateScheduleRequest request) {
        if (request.getAssignedAttractionId() != null) {
            List<WeeklySchedule> existing = scheduleRepository.findByDateAndAttraction(weekStart, day, request.getAssignedAttractionId());
            boolean alreadyOccupied = existing.stream()
                    .anyMatch(ws -> !Boolean.TRUE.equals(ws.getIsReinforcement()));
            if (alreadyOccupied) {
                throw new IllegalArgumentException("error.schedule.attraction.already.assigned");
            }
        }
        if (request.getAssignedZoneId() != null) {
            List<WeeklySchedule> existing = scheduleRepository.findByDateAndZone(weekStart, day, request.getAssignedZoneId());
            boolean alreadyOccupied = existing.stream()
                    .anyMatch(ws -> !Boolean.TRUE.equals(ws.getIsReinforcement()));
            if (alreadyOccupied) {
                throw new IllegalArgumentException("error.schedule.zone.already.assigned");
            }
        }
    }

    private void assignLocation(WeeklySchedule schedule, CreateScheduleRequest request) {
        if (request.getAssignedAttractionId() != null) {
            schedule.setAssignedAttraction(attractionRepository.findById(request.getAssignedAttractionId())
                    .orElseThrow(() -> new IllegalArgumentException("error.attraction.notfound")));
        }
        if (request.getAssignedZoneId() != null) {
            schedule.setAssignedZone(zoneRepository.findById(request.getAssignedZoneId())
                    .orElseThrow(() -> new IllegalArgumentException("error.zone.notfound")));
        }
    }

    private LocalDate normalizeToMonday(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    private WeeklyScheduleDTO toDTO(WeeklySchedule s) {
        String attractionName = s.getAssignedAttraction() != null
                ? s.getAssignedAttraction().getName()
                : s.getSnapshotAttractionName();

        return WeeklyScheduleDTO.builder()
                .id(s.getId())
                .employeeId(s.getEmployee().getId())
                .employeeName(s.getEmployee().getFullName())
                .weekStartDate(s.getWeekStartDate())
                .dayOfWeek(s.getDayOfWeek())
                .shift(s.getShift())
                .assignedZoneId(s.getAssignedZone() != null ? s.getAssignedZone().getId() : null)
                .assignedZoneName(s.getAssignedZone() != null ? s.getAssignedZone().getZoneName().name() : null)
                .assignedAttractionId(s.getAssignedAttraction() != null ? s.getAssignedAttraction().getId() : null)
                .assignedAttractionName(attractionName)
                .breakGroup(s.getBreakGroup())
                .isOvertime(s.getIsOvertime())
                .isReinforcement(s.getIsReinforcement())
                .build();
    }
}
