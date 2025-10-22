package com.magicworld.tfg_angular_springboot.discount_ticket_type;

import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountTicketTypeRepository extends JpaRepository<DiscountTicketType, Long> {

    @Query("SELECT dtt.ticketType FROM DiscountTicketType dtt WHERE dtt.discount.id = :discountId")
    List<TicketType> findByDiscountId(Long discountId);

    @Modifying(clearAutomatically = true)
    @Query("delete from DiscountTicketType d where d.discount.id = :discountId")
    void deleteByDiscountId(Long discountId);

    Boolean existsByTicketTypeId(Long ticketTypeId);
}
