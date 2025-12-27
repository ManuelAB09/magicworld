package com.magicworld.tfg_angular_springboot.discount;

import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeRepository;
import com.magicworld.tfg_angular_springboot.exceptions.AtLeastOneTicketTypeMustBeProvidedException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeRepository;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Descuentos")
@Feature("Servicio de Descuentos")
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
    @Story("Listar Descuentos")
    @Description("Verifica que inicialmente la lista está vacía")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Lista inicial de descuentos está vacía")
    void testFindAllInitiallyEmpty() {
        List<Discount> all = discountService.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    @Story("Guardar Descuento")
    @Description("Verifica que guardar descuento con tipos válidos crea el descuento")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Guardar descuento con tipos válidos crea descuento")
    void testSaveDiscountWithValidTicketTypesCreatesDiscount() {
        Discount toSave = newDiscount("WELCOME10", 10, LocalDate.now().plusDays(30));
        Discount saved = discountService.save(toSave, List.of("ADULT", "CHILD"));
        assertNotNull(saved.getId());
        assertEquals("WELCOME10", saved.getDiscountCode());
    }

    @Test
    @Story("Guardar Descuento")
    @Description("Verifica que guardar descuento crea las asociaciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Guardar descuento crea asociaciones")
    void testSaveDiscountWithValidTicketTypesCreatesAssociations() {
        Discount toSave = newDiscount("WELCOME11", 10, LocalDate.now().plusDays(30));
        Discount saved = discountService.save(toSave, List.of("ADULT", "CHILD"));
        List<TicketType> associated = discountTicketTypeRepository.findByDiscountId(saved.getId());
        assertEquals(2, associated.size());
    }

    @Test
    @Story("Validación de Descuentos")
    @Description("Verifica que guardar con tipos vacíos lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Guardar con tipos vacíos lanza excepción")
    void testSaveDiscountWithEmptyTicketTypesThrows() {
        Discount toSave = newDiscount("EMPTYLIST", 15, LocalDate.now().plusDays(10));
        assertThrows(AtLeastOneTicketTypeMustBeProvidedException.class,
                () -> discountService.save(toSave, List.of()));
    }

    @Test
    @Story("Validación de Descuentos")
    @Description("Verifica que guardar con tipo desconocido lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Guardar con tipo desconocido lanza excepción")
    void testSaveDiscountWithUnknownTicketTypeThrows() {
        Discount toSave = newDiscount("UNKNOWN", 20, LocalDate.now().plusDays(40));
        assertThrows(ResourceNotFoundException.class,
                () -> discountService.save(toSave, List.of("NON_EXISTENT")));
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento existente lo encuentra")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar descuento existente lo encuentra")
    void testFindByIdExists() {
        Discount saved = discountService.save(newDiscount("FINDME", 12, LocalDate.now().plusDays(15)), List.of("ADULT"));
        Discount found = discountService.findById(saved.getId());
        assertEquals(saved.getId(), found.getId());
        assertEquals("FINDME", found.getDiscountCode());
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento inexistente lanza excepción")
    void testFindByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> discountService.findById(123456L));
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar descuento cambia el código")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar descuento cambia el código")
    void testUpdateDiscountFieldsUpdatesCode() {
        Discount saved = discountService.save(newDiscount("UPD10", 10, LocalDate.now().plusDays(30)), List.of("ADULT", "CHILD"));
        Long id = saved.getId();
        Discount updatedData = newDiscount("UPD20", 20, LocalDate.now().plusDays(60));
        updatedData.setId(id);
        Discount updated = discountService.update(updatedData, List.of("VIP"));
        assertEquals("UPD20", updated.getDiscountCode());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar descuento cambia el porcentaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento cambia el porcentaje")
    void testUpdateDiscountFieldsUpdatesPercentage() {
        Discount saved = discountService.save(newDiscount("UPD11", 10, LocalDate.now().plusDays(30)), List.of("ADULT", "CHILD"));
        Long id = saved.getId();
        Discount updatedData = newDiscount("UPD21", 20, LocalDate.now().plusDays(60));
        updatedData.setId(id);
        Discount updated = discountService.update(updatedData, List.of("VIP"));
        assertEquals(20, updated.getDiscountPercentage());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar descuento reemplaza las asociaciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento reemplaza asociaciones")
    void testUpdateDiscountReplacesAssociations() {
        Discount saved = discountService.save(newDiscount("UPD12", 10, LocalDate.now().plusDays(30)), List.of("ADULT", "CHILD"));
        Long id = saved.getId();
        Discount updatedData = newDiscount("UPD22", 20, LocalDate.now().plusDays(60));
        updatedData.setId(id);
        discountService.update(updatedData, List.of("VIP"));
        List<TicketType> associated = discountTicketTypeRepository.findByDiscountId(id);
        assertEquals(1, associated.size());
        assertEquals("VIP", associated.get(0).getTypeName());
    }

    @Test
    @Story("Validación de Descuentos")
    @Description("Verifica que actualizar con tipos vacíos lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar con tipos vacíos lanza excepción")
    void testUpdateWithEmptyTicketTypesThrows() {
        Discount saved = discountService.save(newDiscount("UPD_EMPTY", 5, LocalDate.now().plusDays(5)), List.of("ADULT"));
        Discount updatedData = newDiscount("UPD_EMPTY", 7, LocalDate.now().plusDays(8));
        updatedData.setId(saved.getId());
        assertThrows(AtLeastOneTicketTypeMustBeProvidedException.class,
                () -> discountService.update(updatedData, List.of()));
    }

    @Test
    @Story("Validación de Descuentos")
    @Description("Verifica que actualizar con tipo desconocido lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar con tipo desconocido lanza excepción")
    void testUpdateWithUnknownTicketTypeThrows() {
        Discount saved = discountService.save(newDiscount("UPD_UNK", 5, LocalDate.now().plusDays(5)), List.of("ADULT"));
        Discount updatedData = newDiscount("UPD_UNK", 9, LocalDate.now().plusDays(9));
        updatedData.setId(saved.getId());
        assertThrows(ResourceNotFoundException.class,
                () -> discountService.update(updatedData, List.of("NOT_FOUND_TYPE")));
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que eliminar descuento lo remueve")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar descuento lo remueve")
    void testDeleteByIdRemovesDiscount() {
        Discount saved = discountService.save(newDiscount("DEL", 18, LocalDate.now().plusDays(20)), List.of("ADULT", "VIP"));
        Long id = saved.getId();
        discountService.deleteById(id);
        entityManager.flush();
        entityManager.clear();
        assertFalse(discountRepository.findById(id).isPresent());
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que eliminar descuento remueve las asociaciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar descuento remueve asociaciones")
    void testDeleteByIdRemovesAssociations() {
        Discount saved = discountService.save(newDiscount("DEL2", 18, LocalDate.now().plusDays(20)), List.of("ADULT", "VIP"));
        Long id = saved.getId();
        discountService.deleteById(id);
        entityManager.flush();
        entityManager.clear();
        assertTrue(discountTicketTypeRepository.findByDiscountId(id).isEmpty());
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que eliminar descuento inexistente lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar descuento inexistente lanza excepción")
    void testDeleteByIdNotFoundThrows() {
        assertThrows(ResourceNotFoundException.class, () -> discountService.deleteById(99999L));
    }
}
