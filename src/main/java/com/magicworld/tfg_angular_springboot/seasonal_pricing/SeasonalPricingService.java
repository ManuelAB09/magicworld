package com.magicworld.tfg_angular_springboot.seasonal_pricing;

import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonalPricingService {

    private final SeasonalPricingRepository repository;

    @Transactional(readOnly = true)
    public List<SeasonalPricing> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public SeasonalPricing findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.seasonal_pricing.notfound"));
    }

    @Transactional
    public SeasonalPricing save(SeasonalPricing pricing) {
        validateDates(pricing);
        return repository.save(pricing);
    }

    @Transactional
    public SeasonalPricing update(Long id, SeasonalPricing updated) {
        SeasonalPricing existing = findById(id);
        validateDates(updated);
        existing.setName(updated.getName());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setMultiplier(updated.getMultiplier());
        existing.setApplyOnWeekdays(updated.getApplyOnWeekdays());
        existing.setApplyOnWeekends(updated.getApplyOnWeekends());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        SeasonalPricing pricing = findById(id);
        repository.delete(pricing);
    }

    /**
     * Calculates the accumulated multiplier for a given date.
     * All active seasonal pricings that match the date and day-of-week are multiplied together.
     */
    @Transactional(readOnly = true)
    public BigDecimal getMultiplier(LocalDate date) {
        List<SeasonalPricing> activePricings = repository.findActiveForDate(date);
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;

        BigDecimal multiplier = BigDecimal.ONE;
        for (SeasonalPricing sp : activePricings) {
            if (isWeekend && Boolean.TRUE.equals(sp.getApplyOnWeekends())) {
                multiplier = multiplier.multiply(sp.getMultiplier());
            } else if (!isWeekend && Boolean.TRUE.equals(sp.getApplyOnWeekdays())) {
                multiplier = multiplier.multiply(sp.getMultiplier());
            }
        }
        return multiplier;
    }

    private void validateDates(SeasonalPricing pricing) {
        if (pricing.getEndDate().isBefore(pricing.getStartDate())) {
            throw new BadRequestException("error.seasonal_pricing.end_before_start");
        }
    }
}

