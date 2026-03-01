package com.magicworld.tfg_angular_springboot.attraction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkZoneRepository extends JpaRepository<ParkZone, Long> {
    Optional<ParkZone> findByZoneName(ParkZoneName zoneName);
}

