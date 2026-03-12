package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.employee.WeeklySchedule;
import com.magicworld.tfg_angular_springboot.employee.WeeklyScheduleRepository;
import com.magicworld.tfg_angular_springboot.employee.service.WorkLogService;
import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Attraction saveAttraction(Attraction attraction) {
        return attractionRepository.save(attraction);
    }

    @Transactional(readOnly = true)
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Attraction> getAllAttractions(Integer minHeight, Integer minWeight, Integer minAge) {
        if (minHeight != null && minHeight < 0)
            throw new BadRequestException("minHeight");
        if (minWeight != null && minWeight < 0)
            throw new BadRequestException("minWeight");
        if (minAge != null && minAge < 0)
            throw new BadRequestException("minAge");
        return attractionRepository.findByOptionalFilters(minHeight, minWeight, minAge);
    }

    @Transactional(readOnly = true)
    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.attraction.notfound"));
    }

    @Transactional
    public void deleteAttraction(Long id) {
        Attraction attraction = getAttractionById(id);

        // Handle weekly_schedule entries referencing this attraction:
        // - Future days (after today): delete the schedule entry entirely (unassign employee)
        // - Past/today: just nullify the attraction reference to keep the historical record
        LocalDate today = LocalDate.now();
        List<WeeklySchedule> schedules = weeklyScheduleRepository.findByAssignedAttractionId(id);
        for (WeeklySchedule ws : schedules) {
            LocalDate actualDate = ws.getActualDate();
            if (actualDate.isAfter(today)) {
                weeklyScheduleRepository.delete(ws);
            } else {
                ws.setSnapshotAttractionName(attraction.getName());
                ws.setSnapshotEffectiveHours(WorkLogService.calculateEffectiveHours(ws));
                ws.setAssignedAttraction(null);
                weeklyScheduleRepository.save(ws);
            }
        }

        // Nullify references in other tables to avoid foreign key constraints
        jdbcTemplate.update("UPDATE work_log SET snapshot_attraction_id = NULL WHERE snapshot_attraction_id = ?", id);
        jdbcTemplate.update("UPDATE daily_assignment SET current_attraction_id = NULL WHERE current_attraction_id = ?",
                id);

        // Delete related monitoring and alert records
        jdbcTemplate.update("DELETE FROM park_event WHERE attraction_id = ?", id);
        jdbcTemplate.update("DELETE FROM park_alert WHERE attraction_id = ?", id);

        attractionRepository.delete(attraction);
    }

    @Transactional
    public Attraction updateAttraction(Long id, Attraction updatedAttraction) {
        Attraction existingAttraction = getAttractionById(id);

        existingAttraction.setName(updatedAttraction.getName());
        existingAttraction.setIntensity(updatedAttraction.getIntensity());
        existingAttraction.setCategory(updatedAttraction.getCategory());
        existingAttraction.setMinimumHeight(updatedAttraction.getMinimumHeight());
        existingAttraction.setMinimumAge(updatedAttraction.getMinimumAge());
        existingAttraction.setMinimumWeight(updatedAttraction.getMinimumWeight());
        existingAttraction.setDescription(updatedAttraction.getDescription());
        if (updatedAttraction.getPhotoUrl() != null) {
            existingAttraction.setPhotoUrl(updatedAttraction.getPhotoUrl());
        }
        existingAttraction.setIsActive(updatedAttraction.getIsActive());
        existingAttraction.setMapPositionX(updatedAttraction.getMapPositionX());
        existingAttraction.setMapPositionY(updatedAttraction.getMapPositionY());
        if (updatedAttraction.getOpeningTime() != null) {
            existingAttraction.setOpeningTime(updatedAttraction.getOpeningTime());
        }
        if (updatedAttraction.getClosingTime() != null) {
            existingAttraction.setClosingTime(updatedAttraction.getClosingTime());
        }
        return attractionRepository.save(existingAttraction);
    }
}
