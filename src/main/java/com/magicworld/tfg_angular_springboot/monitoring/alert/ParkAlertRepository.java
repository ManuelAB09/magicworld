package com.magicworld.tfg_angular_springboot.monitoring.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParkAlertRepository extends JpaRepository<ParkAlert, Long> {

    List<ParkAlert> findByIsActiveTrueOrderByTimestampDesc();

    List<ParkAlert> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    List<ParkAlert> findByAttractionIdAndIsActiveTrue(Long attractionId);

    List<ParkAlert> findBySeverityAndIsActiveTrue(AlertSeverity severity);
}
