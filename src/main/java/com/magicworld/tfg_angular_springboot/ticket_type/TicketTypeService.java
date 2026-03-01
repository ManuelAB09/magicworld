package com.magicworld.tfg_angular_springboot.ticket_type;

import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.NoDiscountsCanBeAssignedToTicketTypeException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final DiscountTicketTypeService discountTicketTypeService;

    @Value("${park.max-capacity:500}")
    private int parkMaxCapacity;

    @Transactional(readOnly = true)
    public List<TicketType> findAll() {
        return ticketTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TicketType findById(Long id) {
        return ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.ticket_type.notfound"));
    }

    @Transactional(readOnly = true)
    public TicketType findByTypeName(String typeName) {
        return ticketTypeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new ResourceNotFoundException("error.ticket_type.notfound"));
    }

    @Transactional
    public TicketType save(TicketType ticketType) {
        validateCapacityConstraint(null, ticketType.getMaxPerDay());
        return ticketTypeRepository.save(ticketType);
    }

    @Transactional
    public TicketType update(Long id, TicketType updatedTicketType) {
        TicketType existingTicketType = findById(id);
        validateCapacityConstraint(id, updatedTicketType.getMaxPerDay());
        existingTicketType.setCost(updatedTicketType.getCost());
        existingTicketType.setTypeName(updatedTicketType.getTypeName());
        existingTicketType.setDescription(updatedTicketType.getDescription());
        existingTicketType.setMaxPerDay(updatedTicketType.getMaxPerDay());
        if (updatedTicketType.getPhotoUrl() != null) {
            existingTicketType.setPhotoUrl(updatedTicketType.getPhotoUrl());
        }
        return ticketTypeRepository.save(existingTicketType);
    }

    @Transactional
    public void delete(Long id) {
        TicketType ticketType = findById(id);
        if (discountTicketTypeService.hasAssociations(id)) {
            throw new NoDiscountsCanBeAssignedToTicketTypeException();
        }
        ticketTypeRepository.delete(ticketType);
    }

    private void validateCapacityConstraint(Long excludeId, int newMaxPerDay) {
        int currentTotal = ticketTypeRepository.findAll().stream()
                .filter(tt -> excludeId == null || !tt.getId().equals(excludeId))
                .mapToInt(TicketType::getMaxPerDay)
                .sum();
        if (currentTotal + newMaxPerDay > parkMaxCapacity) {
            throw new BadRequestException("error.ticket_type.max_per_day.exceeds_capacity");
        }
    }
}
