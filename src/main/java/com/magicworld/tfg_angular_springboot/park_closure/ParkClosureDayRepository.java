package com.magicworld.tfg_angular_springboot.park_closure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkClosureDayRepository extends JpaRepository<ParkClosureDay, Long> {

    List<ParkClosureDay> findByClosureDateBetween(LocalDate from, LocalDate to);

    Optional<ParkClosureDay> findByClosureDate(LocalDate date);

    boolean existsByClosureDate(LocalDate date);
}

