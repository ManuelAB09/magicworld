package com.magicworld.tfg_angular_springboot.seasonal_pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SeasonalPricingRepository extends JpaRepository<SeasonalPricing, Long> {

    @Query("SELECT sp FROM SeasonalPricing sp WHERE sp.startDate <= :date AND sp.endDate >= :date")
    List<SeasonalPricing> findActiveForDate(@Param("date") LocalDate date);
}

