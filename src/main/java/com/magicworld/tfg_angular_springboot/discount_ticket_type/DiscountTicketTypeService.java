package com.magicworld.tfg_angular_springboot.discount_ticket_type;

import com.magicworld.tfg_angular_springboot.discount.Discount;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountTicketTypeService {

    private final DiscountTicketTypeRepository discountTicketTypeRepository;

    @Transactional(readOnly = true)
    public List<TicketType> findTicketsTypesByDiscountId(Long discountId) {
        return discountTicketTypeRepository.findByDiscountId(discountId);
    }

    @Transactional
    public void replaceAssociations(Discount discount, List<TicketType> ticketTypes) {
        discountTicketTypeRepository.deleteByDiscountId(discount.getId());
        List<DiscountTicketType> toSave = ticketTypes.stream()
                .map(tt -> DiscountTicketType.builder()
                        .discount(discount)
                        .ticketType(tt)
                        .build())
                .collect(Collectors.toList());
        if (!toSave.isEmpty()) {
            discountTicketTypeRepository.saveAll(toSave);
        }
    }

    @Transactional
    public void deleteByDiscountId(Long discountId) {
        discountTicketTypeRepository.deleteByDiscountId(discountId);
    }
}
