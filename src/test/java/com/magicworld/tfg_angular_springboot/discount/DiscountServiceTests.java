package com.magicworld.tfg_angular_springboot.discount;

import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeRepository;
import com.magicworld.tfg_angular_springboot.exceptions.AtLeastOneTicketTypeMustBeProvidedException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DiscountServiceTests {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private DiscountTicketTypeRepository discountTicketTypeRepository;

    @Autowired
    private EntityManager entityManager;

    private TicketType adult;
    private TicketType child;
    private TicketType vip;

    @BeforeEach
    void setUp() {
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        adult = ticketTypeRepository.save(TicketType.builder()
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .typeName("ADULT")
                .description("Adult ticket")
                .maxPerDay(10)
                .photoUrl("https://example.com/adult.jpg")
                .build());

        child = ticketTypeRepository.save(TicketType.builder()
                .cost(new BigDecimal("25.00"))
                .currency("EUR")
                .typeName("CHILD")
                .description("Child ticket")
                .maxPerDay(10)
                .photoUrl("https://example.com/child.jpg")
                .build());

        vip = ticketTypeRepository.save(TicketType.builder()
                .cost(new BigDecimal("120.00"))
                .currency("EUR")
                .typeName("VIP")
                .description("VIP ticket")
                .maxPerDay(5)
                .photoUrl("https://example.com/vip.jpg")
                .build());
        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Discount newDiscount(String code, int percentage, LocalDate expiry) {
        return Discount.builder()
                .discountCode(code)
                .discountPercentage(percentage)
                .expiryDate(expiry)
                .build();
    }

    @Test
    void testFindAllInitiallyEmpty() {
        List<Discount> all = discountService.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testSaveDiscountWithValidTicketTypesCreatesAssociations() {
        Discount toSave = newDiscount("WELCOME10", 10, LocalDate.now().plusDays(30));
        Discount saved = discountService.save(toSave, List.of("ADULT", "CHILD"));

        assertNotNull(saved.getId());
        assertEquals("WELCOME10", saved.getDiscountCode());

        List<TicketType> associated = discountTicketTypeRepository.findByDiscountId(saved.getId());
        assertEquals(2, associated.size());
        Set<String> names = associated.stream().map(TicketType::getTypeName).collect(Collectors.toSet());
        assertTrue(names.containsAll(Set.of("ADULT", "CHILD")));
    }

    @Test
    void testSaveDiscountWithEmptyTicketTypesThrows() {
        Discount toSave = newDiscount("EMPTYLIST", 15, LocalDate.now().plusDays(10));
        assertThrows(AtLeastOneTicketTypeMustBeProvidedException.class,
                () -> discountService.save(toSave, List.of()));
    }

    @Test
    void testSaveDiscountWithUnknownTicketTypeThrows() {
        Discount toSave = newDiscount("UNKNOWN", 20, LocalDate.now().plusDays(40));
        assertThrows(ResourceNotFoundException.class,
                () -> discountService.save(toSave, List.of("NON_EXISTENT")));
    }

    @Test
    void testFindByIdExists() {
        Discount saved = discountService.save(newDiscount("FINDME", 12, LocalDate.now().plusDays(15)), List.of("ADULT"));
        Discount found = discountService.findById(saved.getId());
        assertEquals(saved.getId(), found.getId());
        assertEquals("FINDME", found.getDiscountCode());
    }

    @Test
    void testFindByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> discountService.findById(123456L));
    }

    @Test
    void testUpdateDiscountFieldsAndReplaceAssociations() {
        Discount saved = discountService.save(newDiscount("UPD10", 10, LocalDate.now().plusDays(30)), List.of("ADULT", "CHILD"));
        Long id = saved.getId();

        Discount updatedData = newDiscount("UPD20", 20, LocalDate.now().plusDays(60));
        updatedData.setId(id);
        Discount updated = discountService.update(updatedData, List.of("VIP"));

        assertEquals(id, updated.getId());
        assertEquals("UPD20", updated.getDiscountCode());
        assertEquals(20, updated.getDiscountPercentage());

        List<TicketType> associated = discountTicketTypeRepository.findByDiscountId(id);
        assertEquals(1, associated.size());
        assertEquals("VIP", associated.get(0).getTypeName());
    }

    @Test
    void testUpdateWithEmptyTicketTypesThrows() {
        Discount saved = discountService.save(newDiscount("UPD_EMPTY", 5, LocalDate.now().plusDays(5)), List.of("ADULT"));
        Discount updatedData = newDiscount("UPD_EMPTY", 7, LocalDate.now().plusDays(8));
        updatedData.setId(saved.getId());
        assertThrows(AtLeastOneTicketTypeMustBeProvidedException.class,
                () -> discountService.update(updatedData, List.of()));
    }

    @Test
    void testUpdateWithUnknownTicketTypeThrows() {
        Discount saved = discountService.save(newDiscount("UPD_UNK", 5, LocalDate.now().plusDays(5)), List.of("ADULT"));
        Discount updatedData = newDiscount("UPD_UNK", 9, LocalDate.now().plusDays(9));
        updatedData.setId(saved.getId());
        assertThrows(ResourceNotFoundException.class,
                () -> discountService.update(updatedData, List.of("NOT_FOUND_TYPE")));
    }

    @Test
    void testDeleteByIdRemovesDiscountAndAssociations() {
        Discount saved = discountService.save(newDiscount("DEL", 18, LocalDate.now().plusDays(20)), List.of("ADULT", "VIP"));
        Long id = saved.getId();

        assertEquals(2, discountTicketTypeRepository.findByDiscountId(id).size());

        discountService.deleteById(id);
        // Asegurar flush/clear para evitar estados transitorios al consultar
        entityManager.flush();
        entityManager.clear();

        assertFalse(discountRepository.findById(id).isPresent());
        assertTrue(discountTicketTypeRepository.findByDiscountId(id).isEmpty());
    }

    @Test
    void testDeleteByIdNotFoundThrows() {
        assertThrows(ResourceNotFoundException.class, () -> discountService.deleteById(99999L));
    }
}
