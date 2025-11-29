package com.magicworld.tfg_angular_springboot.discount;

import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.exceptions.AtLeastOneTicketTypeMustBeProvidedException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final TicketTypeService ticketTypeService;
    private final DiscountTicketTypeService discountTicketTypeService;

    @Transactional(readOnly = true)
    public List<Discount> findAll() {
        return discountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Discount findById(Long id) {
        return discountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.discount.not_found"));
    }

    @Transactional
    public Discount save(Discount discount, List<String> applicableTicketTypesNames) {
        List<TicketType> applicableTicketTypes = applicableTicketTypesNames.stream()
                .map(ticketTypeService::findByTypeName)
                .toList();
        if (applicableTicketTypes.isEmpty()) {
            throw new AtLeastOneTicketTypeMustBeProvidedException();
        }
        Discount savedDiscount = discountRepository.save(discount);
        discountTicketTypeService.replaceAssociations(savedDiscount, applicableTicketTypes);
        return savedDiscount;
    }

    @Transactional
    public Discount update(Discount updatedDiscount, List<String> applicableTicketTypesNames) {
        Discount existingDiscount = findById(updatedDiscount.getId());
        existingDiscount.setDiscountCode(updatedDiscount.getDiscountCode());
        existingDiscount.setDiscountPercentage(updatedDiscount.getDiscountPercentage());
        existingDiscount.setExpiryDate(updatedDiscount.getExpiryDate());
        List<TicketType> applicableTicketTypes = applicableTicketTypesNames.stream()
                .map(ticketTypeService::findByTypeName)
                .toList();
        if (applicableTicketTypes.isEmpty()) {
            throw new AtLeastOneTicketTypeMustBeProvidedException();
        }
        discountTicketTypeService.replaceAssociations(existingDiscount, applicableTicketTypes);
        discountRepository.save(existingDiscount);
        return existingDiscount;
    }

    @Transactional
    public void deleteById(Long id) {
        Discount discount = findById(id);
        discountTicketTypeService.deleteByDiscountId(discount.getId());
        discountRepository.delete(discount);
    }
}
