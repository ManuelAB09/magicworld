package com.magicworld.tfg_angular_springboot.monitoring.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParkEventRepository extends JpaRepository<ParkEvent, Long> {

    List<ParkEvent> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    List<ParkEvent> findByEventTypeAndTimestampBetween(
            ParkEventType eventType, LocalDateTime start, LocalDateTime end);

    List<ParkEvent> findByAttractionIdAndTimestampBetween(
            Long attractionId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(e) FROM ParkEvent e WHERE e.eventType = :type AND e.timestamp >= :since")
    long countByEventTypeSince(@Param("type") ParkEventType type, @Param("since") LocalDateTime since);

    @Query("SELECT e FROM ParkEvent e WHERE e.attractionId = :attractionId " +
           "AND e.eventType IN :types AND e.timestamp >= :since ORDER BY e.timestamp DESC")
    List<ParkEvent> findAttractionEventsSince(
            @Param("attractionId") Long attractionId,
            @Param("types") List<ParkEventType> types,
            @Param("since") LocalDateTime since);

    @Query("SELECT e.attractionId, COUNT(e), " +
           "COALESCE(MAX(e.queueSize), 0), " +
           "COALESCE(AVG(e.queueSize), 0) " +
           "FROM ParkEvent e " +
           "WHERE e.eventType = 'ATTRACTION_QUEUE_JOIN' " +
           "AND e.attractionId IS NOT NULL " +
           "AND e.timestamp BETWEEN :from AND :to " +
           "GROUP BY e.attractionId " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> findAttractionPerformanceStats(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
