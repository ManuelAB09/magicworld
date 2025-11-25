package com.magicworld.tfg_angular_springboot.attraction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    @Query("""
        SELECT a FROM Attraction a
        WHERE (:minHeight IS NULL OR a.minimumHeight <= :minHeight)
          AND (:minWeight IS NULL OR a.minimumWeight <= :minWeight)
          AND (:minAge IS NULL OR a.minimumAge <= :minAge)
        """)
    List<Attraction> findByOptionalFilters(@Param("minHeight") Integer minHeight,
                                           @Param("minWeight") Integer minWeight,
                                           @Param("minAge") Integer minAge);
}
