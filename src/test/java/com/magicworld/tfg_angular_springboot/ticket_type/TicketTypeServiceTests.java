package com.magicworld.tfg_angular_springboot.ticket_type;

import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketTypeServiceTests {

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    private TicketType sample;

    @BeforeEach
    void setUp() {
        ticketTypeRepository.deleteAll();
        sample = TicketType.builder()
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .typeName("STANDARD")
                .description("Standard ticket")
                .maxPerDay(10)
                .build();
    }

    @AfterEach
    void tearDown() {
        ticketTypeRepository.deleteAll();
    }

    @Test
    void testFindAllInitiallyEmpty() {
        List<TicketType> all = ticketTypeService.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testSaveTicketType() {
        TicketType saved = ticketTypeService.save(sample);
        assertNotNull(saved.getId());
        assertEquals("STANDARD", saved.getTypeName());
        assertEquals(new BigDecimal("50.00"), saved.getCost());
    }

    @Test
    void testFindByIdExists() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType found = ticketTypeService.findById(saved.getId());
        assertEquals(saved.getId(), found.getId());
        assertEquals("STANDARD", found.getTypeName());
    }

    @Test
    void testFindByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> ticketTypeService.findById(99999L));
    }

    @Test
    void testFindByTypeNameExists() {
        ticketTypeService.save(sample);
        TicketType found = ticketTypeService.findByTypeName("STANDARD");
        assertEquals("STANDARD", found.getTypeName());
    }

    @Test
    void testFindByTypeNameNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> ticketTypeService.findByTypeName("DOES_NOT_EXIST"));
    }

    @Test
    void testUpdateTicketType() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .build();

        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(saved.getId(), updated.getId());
        assertEquals(new BigDecimal("75.50"), updated.getCost());
        assertEquals("USD", updated.getCurrency());
        assertEquals("PREMIUM", updated.getTypeName());
        assertEquals("Premium ticket", updated.getDescription());
        assertEquals(20, updated.getMaxPerDay());
    }

    @Test
    void testDeleteTicketType() {
        TicketType saved = ticketTypeService.save(sample);
        Long id = saved.getId();
        ticketTypeService.delete(id);
        assertThrows(ResourceNotFoundException.class, () -> ticketTypeService.findById(id));
    }
}
