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

    private static final String CURRENCY_EUR = "EUR";
    private static final String TYPE_NAME_ADULT = "ADULT";
    private static final String TYPE_NAME_CHILD = "CHILD";
    private static final String TYPE_NAME_VIP = "VIP";
    private static final String ADULT_TICKET_DESC = "Adult ticket";
    private static final String CHILD_TICKET_DESC = "Child ticket";
    private static final String VIP_TICKET_DESC = "VIP ticket";
    private static final String PHOTO_URL_ADULT = "https://example.com/adult.jpg";
    private static final String PHOTO_URL_CHILD = "https://example.com/child.jpg";
    private static final String PHOTO_URL_VIP = "https://example.com/vip.jpg";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");
    private static final BigDecimal COST_25 = new BigDecimal("25.00");
    private static final BigDecimal COST_120 = new BigDecimal("120.00");

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

    @BeforeEach
    void setUp() {
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        TicketType adult = ticketTypeRepository.save(TicketType.builder()
                .cost(COST_50)
                .typeName(TYPE_NAME_ADULT)
                .description(ADULT_TICKET_DESC)
                .maxPerDay(10)
                .photoUrl(PHOTO_URL_ADULT)
                .build());

        TicketType child = ticketTypeRepository.save(TicketType.builder()
                .cost(COST_25)
                .typeName(TYPE_NAME_CHILD)
                .description(CHILD_TICKET_DESC)
                .maxPerDay(10)
                .photoUrl(PHOTO_URL_CHILD)
                .build());

        TicketType vip = ticketTypeRepository.save(TicketType.builder()
                .cost(COST_120)
                .typeName(TYPE_NAME_VIP)
                .description(VIP_TICKET_DESC)
                .maxPerDay(5)
                .photoUrl(PHOTO_URL_VIP)
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
        Discount saved = discountService.save(toSave, List.of(TYPE_NAME_ADULT, TYPE_NAME_CHILD));
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
        Discount saved = discountService.save(toSave, List.of(TYPE_NAME_ADULT, TYPE_NAME_CHILD));
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
        Discount saved = discountService.save(newDiscount("FINDME", 12, LocalDate.now().plusDays(15)), List.of(TYPE_NAME_ADULT));
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
        Discount saved = discountService.save(newDiscount("UPD10", 10, LocalDate.now().plusDays(30)), List.of(TYPE_NAME_ADULT, TYPE_NAME_CHILD));
        Long id = saved.getId();
        Discount updatedData = newDiscount("UPD20", 20, LocalDate.now().plusDays(60));
        updatedData.setId(id);
        Discount updated = discountService.update(updatedData, List.of(TYPE_NAME_VIP));
        assertEquals("UPD20", updated.getDiscountCode());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar descuento cambia el porcentaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento cambia el porcentaje")
    void testUpdateDiscountFieldsUpdatesPercentage() {
        Discount saved = discountService.save(newDiscount("UPD11", 10, LocalDate.now().plusDays(30)), List.of(TYPE_NAME_ADULT, TYPE_NAME_CHILD));
        Long id = saved.getId();
        Discount updatedData = newDiscount("UPD21", 20, LocalDate.now().plusDays(60));
        updatedData.setId(id);
        Discount updated = discountService.update(updatedData, List.of(TYPE_NAME_VIP));
        assertEquals(20, updated.getDiscountPercentage());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar descuento reemplaza las asociaciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento reemplaza asociaciones")
    void testUpdateDiscountReplacesAssociations() {
        Discount saved = discountService.save(newDiscount("UPD12", 10, LocalDate.now().plusDays(30)), List.of(TYPE_NAME_ADULT, TYPE_NAME_CHILD));
        Long id = saved.getId();
        Discount updatedData = newDiscount("UPD22", 20, LocalDate.now().plusDays(60));
        updatedData.setId(id);
        discountService.update(updatedData, List.of(TYPE_NAME_VIP));
        List<TicketType> associated = discountTicketTypeRepository.findByDiscountId(id);
        assertEquals(1, associated.size());
        assertEquals(TYPE_NAME_VIP, associated.getFirst().getTypeName());
    }

    @Test
    @Story("Validación de Descuentos")
    @Description("Verifica que actualizar con tipos vacíos lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar con tipos vacíos lanza excepción")
    void testUpdateWithEmptyTicketTypesThrows() {
        Discount saved = discountService.save(newDiscount("UPD_EMPTY", 5, LocalDate.now().plusDays(5)), List.of(TYPE_NAME_ADULT));
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
        Discount saved = discountService.save(newDiscount("UPD_UNK", 5, LocalDate.now().plusDays(5)), List.of(TYPE_NAME_ADULT));
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
        Discount saved = discountService.save(newDiscount("DEL", 18, LocalDate.now().plusDays(20)), List.of(TYPE_NAME_ADULT, TYPE_NAME_VIP));
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
        Discount saved = discountService.save(newDiscount("DEL2", 18, LocalDate.now().plusDays(20)), List.of(TYPE_NAME_ADULT, TYPE_NAME_VIP));
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
