package com.magicworld.tfg_angular_springboot.purchase_line;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseLineRepository extends JpaRepository<PurchaseLine, Long> {

    List<PurchaseLine> findByPurchaseId(Long purchaseId);

    @Query("SELECT COALESCE(SUM(pl.quantity), 0) FROM PurchaseLine pl WHERE pl.ticketTypeName = :typeName AND pl.validDate = :date")
    Integer sumQuantityByTicketTypeNameAndValidDate(@Param("typeName") String typeName, @Param("date") LocalDate date);
}

