package com.magicworld.tfg_angular_springboot.purchase_line;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseLineRepository extends JpaRepository<PurchaseLine, Long> {

    List<PurchaseLine> findByPurchaseId(Long purchaseId);

    @Query("SELECT COALESCE(SUM(pl.quantity), 0) FROM PurchaseLine pl WHERE pl.ticketTypeName = :typeName AND pl.validDate = :date")
    Integer sumQuantityByTicketTypeNameAndValidDate(@Param("typeName") String typeName, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(pl.quantity), 0) FROM PurchaseLine pl WHERE pl.validDate = :date")
    Integer sumTotalQuantityByValidDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(pl.quantity), 0) FROM PurchaseLine pl " +
           "WHERE pl.validDate BETWEEN :from AND :to")
    Integer sumQuantityByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(pl.totalCost), 0) FROM PurchaseLine pl " +
           "WHERE pl.validDate BETWEEN :from AND :to")
    BigDecimal sumRevenueByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT MONTH(pl.validDate) AS month, " +
           "COALESCE(SUM(pl.quantity), 0) AS tickets, " +
           "COALESCE(SUM(pl.totalCost), 0) AS revenue " +
           "FROM PurchaseLine pl " +
           "WHERE YEAR(pl.validDate) = :year " +
           "GROUP BY MONTH(pl.validDate) " +
           "ORDER BY MONTH(pl.validDate)")
    List<Object[]> findMonthlySalesByYear(@Param("year") int year);

    List<PurchaseLine> findByValidDateBetween(LocalDate from, LocalDate to);
}
