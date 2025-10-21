package com.magicworld.tfg_angular_springboot.ticket_type;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    Optional<TicketType> findByTypeName(String typeName);
    @Query("SELECT tt.typeName FROM TicketType tt")
    List<String> findAllTypeNames();
}
