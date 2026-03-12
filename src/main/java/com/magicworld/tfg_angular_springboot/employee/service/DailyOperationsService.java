package com.magicworld.tfg_angular_springboot.employee.service;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.attraction.ParkZone;
import com.magicworld.tfg_angular_springboot.attraction.ParkZoneRepository;
import com.magicworld.tfg_angular_springboot.employee.*;
import com.magicworld.tfg_angular_springboot.employee.dto.*;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlert;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyOperationsService {

    private final DailyAssignmentRepository dailyAssignmentRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ReinforcementCallRepository reinforcementCallRepository;
    private final ParkAlertRepository alertRepository;
    private final AttractionRepository attractionRepository;
    private final ParkZoneRepository parkZoneRepository;

    private static final double REJECTION_PROBABILITY = 0.30;
    private final Random random = new Random();

    @Transactional
    public void initializeDay(LocalDate date) {
        List<DailyAssignment> existing = dailyAssignmentRepository.findByAssignmentDate(date);
        if (!existing.isEmpty())
            return;

        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        List<WeeklySchedule> schedules = weeklyScheduleRepository
                .findByWeekStartDateAndDayOfWeek(weekStart, dayOfWeek);

        if (schedules.isEmpty()) {
            createDemoAssignments(date);
        } else {
            for (WeeklySchedule schedule : schedules) {
                createDailyAssignment(schedule, date);
            }
        }
    }

    private void createDemoAssignments(LocalDate date) {
        List<Attraction> attractions = attractionRepository.findByIsActiveTrue();
        List<ParkZone> zones = parkZoneRepository.findAll();

        List<Employee> operators = employeeRepository.findByRoleAndStatus(EmployeeRole.OPERATOR, EmployeeStatus.ACTIVE);
        List<Employee> security = employeeRepository.findByRoleAndStatus(EmployeeRole.SECURITY, EmployeeStatus.ACTIVE);
        List<Employee> medical = employeeRepository.findByRoleAndStatus(EmployeeRole.MEDICAL, EmployeeStatus.ACTIVE);
        List<Employee> maintenance = employeeRepository.findByRoleAndStatus(EmployeeRole.MAINTENANCE,
                EmployeeStatus.ACTIVE);
        List<Employee> guestServices = employeeRepository.findByRoleAndStatus(EmployeeRole.GUEST_SERVICES,
                EmployeeStatus.ACTIVE);

        int breakGroupIndex = 0;
        BreakGroup[] groups = BreakGroup.values();

        for (int i = 0; i < Math.min(operators.size(), attractions.size()); i++) {
            createAssignmentWithAttraction(operators.get(i), date, attractions.get(i), groups[breakGroupIndex++ % 4]);
        }

        for (int i = 0; i < Math.min(security.size(), zones.size()); i++) {
            createAssignmentWithZone(security.get(i), date, zones.get(i), groups[breakGroupIndex++ % 4]);
        }

        for (Employee emp : medical.stream().limit(2).toList()) {
            createBasicAssignment(emp, date, groups[breakGroupIndex++ % 4]);
        }

        for (Employee emp : maintenance.stream().limit(2).toList()) {
            createBasicAssignment(emp, date, groups[breakGroupIndex++ % 4]);
        }

        for (Employee emp : guestServices.stream().limit(2).toList()) {
            createBasicAssignment(emp, date, groups[breakGroupIndex++ % 4]);
        }
    }

    private void createAssignmentWithAttraction(Employee emp, LocalDate date, Attraction attr, BreakGroup bg) {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(emp).assignmentDate(date).currentStatus(DailyStatus.WORKING)
                .currentAttraction(attr).breakGroup(bg)
                .breakStartTime(bg.getStartTime()).breakEndTime(bg.getEndTime()).build());
    }

    private void createAssignmentWithZone(Employee emp, LocalDate date, ParkZone zone, BreakGroup bg) {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(emp).assignmentDate(date).currentStatus(DailyStatus.WORKING)
                .currentZone(zone).breakGroup(bg)
                .breakStartTime(bg.getStartTime()).breakEndTime(bg.getEndTime()).build());
    }

    private void createBasicAssignment(Employee emp, LocalDate date, BreakGroup bg) {
        dailyAssignmentRepository.save(DailyAssignment.builder()
                .employee(emp).assignmentDate(date).currentStatus(DailyStatus.WORKING)
                .breakGroup(bg).breakStartTime(bg.getStartTime()).breakEndTime(bg.getEndTime()).build());
    }

    private void createDailyAssignment(WeeklySchedule schedule, LocalDate date) {
        DailyAssignment assignment = DailyAssignment.builder()
                .employee(schedule.getEmployee())
                .assignmentDate(date)
                .currentStatus(DailyStatus.WORKING)
                .currentZone(schedule.getAssignedZone())
                .currentAttraction(schedule.getAssignedAttraction())
                .breakGroup(schedule.getBreakGroup())
                .breakStartTime(schedule.getBreakGroup().getStartTime())
                .breakEndTime(schedule.getBreakGroup().getEndTime())
                .build();

        dailyAssignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<DailyAssignmentDTO> getTodayAssignments() {
        return getAssignmentsForDate(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<DailyAssignmentDTO> getAssignmentsForDate(LocalDate date) {
        return dailyAssignmentRepository.findByDateWithEmployee(date).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvailableEmployeesResponse checkAvailableEmployees(EmployeeRole role) {
        LocalDate today = LocalDate.now();

        List<DailyAssignment> available = dailyAssignmentRepository
                .findAvailableByRoleAndDate(today, DailyStatus.WORKING, role);

        List<AvailableEmployeesResponse.AvailableEmployee> availableList = available.stream()
                .map(this::toAvailableEmployee)
                .toList();

        List<AvailableEmployeesResponse.ReinforcementCandidate> reinforcements = getReinforcementCandidates(role,
                today);

        return AvailableEmployeesResponse.builder()
                .employees(availableList)
                .reinforcements(reinforcements)
                .hasAvailable(!availableList.isEmpty())
                .hasReinforcements(!reinforcements.isEmpty())
                .build();
    }

    @Transactional(readOnly = true)
    public AvailableEmployeesResponse getAvailableEmployees(EmployeeRole role) {
        return checkAvailableEmployees(role);
    }

    private List<AvailableEmployeesResponse.ReinforcementCandidate> getReinforcementCandidates(
            EmployeeRole role, LocalDate date) {

        List<Employee> allOfRole = employeeRepository.findByRoleAndStatus(role, EmployeeStatus.ACTIVE);

        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        List<Long> scheduledTodayIds = weeklyScheduleRepository
                .findByWeekStartDateAndDayOfWeek(weekStart, dayOfWeek).stream()
                .map(ws -> ws.getEmployee().getId())
                .toList();

        return allOfRole.stream()
                .filter(e -> !scheduledTodayIds.contains(e.getId()))
                .map(e -> AvailableEmployeesResponse.ReinforcementCandidate.builder()
                        .id(e.getId())
                        .name(e.getFullName())
                        .role(e.getRole())
                        .phone(e.getPhone())
                        .build())
                .toList();
    }

    @Transactional
    public DailyAssignmentDTO assignEmployeeToAlert(Long employeeId, Long alertId) {
        LocalDate today = LocalDate.now();
        DailyAssignment assignment = dailyAssignmentRepository
                .findByEmployeeIdAndAssignmentDate(employeeId, today)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.not.working.today"));

        if (assignment.getCurrentStatus() != DailyStatus.WORKING) {
            throw new IllegalArgumentException("error.employee.not.available");
        }

        ParkAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("error.alert.notfound"));

        assignment.setCurrentStatus(DailyStatus.ASSIGNED_TO_ALERT);
        assignment.setAssignedAlert(alert);

        return toDTO(dailyAssignmentRepository.save(assignment));
    }

    @Transactional
    public DailyAssignmentDTO releaseEmployeeFromAlert(Long employeeId) {
        LocalDate today = LocalDate.now();
        DailyAssignment assignment = dailyAssignmentRepository
                .findByEmployeeIdAndAssignmentDate(employeeId, today)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.not.working.today"));

        assignment.setCurrentStatus(DailyStatus.WORKING);
        assignment.setAssignedAlert(null);

        return toDTO(dailyAssignmentRepository.save(assignment));
    }

    @Transactional
    public ReinforcementCall callReinforcement(Long employeeId, Long alertId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("error.employee.notfound"));

        ParkAlert alert = alertId != null ? alertRepository.findById(alertId).orElse(null) : null;

        // Simulate 30% rejection probability
        boolean rejected = random.nextDouble() < REJECTION_PROBABILITY;

        ReinforcementCall call = ReinforcementCall.builder()
                .employee(employee)
                .callTime(LocalDateTime.now())
                .originAlert(alert)
                .isOvertime(true)
                .status(rejected ? ReinforcementStatus.REJECTED : ReinforcementStatus.ACCEPTED)
                .responseTime(LocalDateTime.now())
                .build();

        ReinforcementCall saved = reinforcementCallRepository.save(call);

        if (!rejected) {
            saved.setArrivalTime(LocalDateTime.now());
            createAssignmentForReinforcement(saved);
            reinforcementCallRepository.save(saved);
            log.info("Reinforcement accepted: {} for alert {}", employee.getFullName(), alertId);
        } else {
            log.info("Reinforcement REJECTED by: {} for alert {}", employee.getFullName(), alertId);
        }

        return saved;
    }

    @Transactional
    public ReinforcementCall updateReinforcementStatus(Long callId, ReinforcementStatus newStatus) {
        ReinforcementCall call = reinforcementCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("error.reinforcement.notfound"));

        call.setStatus(newStatus);

        if (newStatus == ReinforcementStatus.ACCEPTED || newStatus == ReinforcementStatus.REJECTED) {
            call.setResponseTime(LocalDateTime.now());
        }
        if (newStatus == ReinforcementStatus.ARRIVED) {
            call.setArrivalTime(LocalDateTime.now());
            createAssignmentForReinforcement(call);
        }

        return reinforcementCallRepository.save(call);
    }

    private void createAssignmentForReinforcement(ReinforcementCall call) {
        LocalDate today = LocalDate.now();
        if (dailyAssignmentRepository.findByEmployeeIdAndAssignmentDate(
                call.getEmployee().getId(), today).isPresent()) {
            return;
        }

        DailyAssignment assignment = DailyAssignment.builder()
                .employee(call.getEmployee())
                .assignmentDate(today)
                .currentStatus(DailyStatus.WORKING)
                .breakGroup(BreakGroup.D)
                .isOvertime(true)
                .build();

        if (call.getOriginAlert() != null) {
            assignment.setCurrentStatus(DailyStatus.ASSIGNED_TO_ALERT);
            assignment.setAssignedAlert(call.getOriginAlert());
        }

        dailyAssignmentRepository.save(assignment);

        addReinforcementToWeeklySchedule(call.getEmployee(), today);
    }

    private void addReinforcementToWeeklySchedule(Employee employee, LocalDate date) {
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() - 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        boolean alreadyScheduled = weeklyScheduleRepository
                .findByEmployeeIdAndWeekStartDate(employee.getId(), weekStart).stream()
                .anyMatch(ws -> ws.getDayOfWeek() == dayOfWeek);

        if (alreadyScheduled)
            return;

        WeeklySchedule schedule = WeeklySchedule.builder()
                .employee(employee)
                .weekStartDate(weekStart)
                .dayOfWeek(dayOfWeek)
                .shift(WorkShift.FULL_DAY)
                .breakGroup(BreakGroup.D)
                .isOvertime(true)
                .isReinforcement(true)
                .build();

        weeklyScheduleRepository.save(schedule);
    }

    private AvailableEmployeesResponse.AvailableEmployee toAvailableEmployee(DailyAssignment da) {
        String location = da.getCurrentAttraction() != null
                ? da.getCurrentAttraction().getName()
                : da.getCurrentZone() != null
                        ? da.getCurrentZone().getZoneName().name()
                        : "General";

        return AvailableEmployeesResponse.AvailableEmployee.builder()
                .id(da.getEmployee().getId())
                .name(da.getEmployee().getFullName())
                .role(da.getEmployee().getRole())
                .currentLocation(location)
                .build();
    }

    private DailyAssignmentDTO toDTO(DailyAssignment da) {
        return DailyAssignmentDTO.builder()
                .id(da.getId())
                .employeeId(da.getEmployee().getId())
                .employeeName(da.getEmployee().getFullName())
                .employeeRole(da.getEmployee().getRole())
                .assignmentDate(da.getAssignmentDate())
                .currentStatus(da.getCurrentStatus())
                .currentZoneId(da.getCurrentZone() != null ? da.getCurrentZone().getId() : null)
                .currentZoneName(da.getCurrentZone() != null ? da.getCurrentZone().getZoneName().name() : null)
                .currentAttractionId(da.getCurrentAttraction() != null ? da.getCurrentAttraction().getId() : null)
                .currentAttractionName(da.getCurrentAttraction() != null ? da.getCurrentAttraction().getName() : null)
                .assignedAlertId(da.getAssignedAlert() != null ? da.getAssignedAlert().getId() : null)
                .breakStartTime(da.getBreakStartTime())
                .breakEndTime(da.getBreakEndTime())
                .build();
    }
}
