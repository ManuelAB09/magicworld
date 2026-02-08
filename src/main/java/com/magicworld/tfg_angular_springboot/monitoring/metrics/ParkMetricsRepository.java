package com.magicworld.tfg_angular_springboot.monitoring.metrics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkMetricsRepository extends JpaRepository<ParkMetrics, Long> {

    List<ParkMetrics> findByTimestampBetweenOrderByTimestampAsc(LocalDateTime start, LocalDateTime end);

    List<ParkMetrics> findByAttractionIdAndTimestampBetweenOrderByTimestampAsc(
            Long attractionId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM ParkMetrics p WHERE p.attractionId IS NULL " +
           "AND p.timestamp BETWEEN :start AND :end ORDER BY p.timestamp ASC")
    List<ParkMetrics> findGlobalMetricsBetween(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p FROM ParkMetrics p WHERE p.attractionId = :attractionId " +
           "ORDER BY p.timestamp DESC LIMIT 1")
    Optional<ParkMetrics> findLatestByAttractionId(@Param("attractionId") Long attractionId);

    @Query("SELECT p FROM ParkMetrics p WHERE p.attractionId IS NULL " +
           "ORDER BY p.timestamp DESC LIMIT 1")
    Optional<ParkMetrics> findLatestGlobalMetrics();

    @Query("SELECT p FROM ParkMetrics p WHERE p.attractionId = :attractionId " +
           "AND p.timestamp >= :since ORDER BY p.timestamp ASC")
    List<ParkMetrics> findByAttractionIdSince(
            @Param("attractionId") Long attractionId, @Param("since") LocalDateTime since);
}
