package com.magicworld.tfg_angular_springboot.ticket_type;

import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    @Transactional(readOnly = true)
    public List<TicketType> findAll() {
        return ticketTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TicketType findById(Long id) {
        return ticketTypeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.ticket_type.notfound"));
    }

    @Transactional(readOnly = true)
    public TicketType findByTypeName(String typeName) {
        return ticketTypeRepository.findByTypeName(typeName).orElseThrow(() -> new ResourceNotFoundException("error.ticket_type.notfound"));
    }

    @Transactional
    public TicketType save(TicketType ticketType) {
        return ticketTypeRepository.save(ticketType);
    }

    @Transactional
    public TicketType update(Long id, TicketType updatedTicketType) {
        TicketType existingTicketType = findById(id);
        existingTicketType.setCost(updatedTicketType.getCost());
        existingTicketType.setCurrency(updatedTicketType.getCurrency());
        existingTicketType.setTypeName(updatedTicketType.getTypeName());
        existingTicketType.setDescription(updatedTicketType.getDescription());
        existingTicketType.setMaxPerDay(updatedTicketType.getMaxPerDay());
        return ticketTypeRepository.save(existingTicketType);
    }

    @Transactional
    public void delete(Long id) {
        TicketType ticketType = findById(id);
        ticketTypeRepository.delete(ticketType);
    }
}
