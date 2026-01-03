package com.magicworld.tfg_angular_springboot.ticket_type;

import com.magicworld.tfg_angular_springboot.discount.Discount;
import com.magicworld.tfg_angular_springboot.discount.DiscountRepository;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketType;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeRepository;
import com.magicworld.tfg_angular_springboot.exceptions.NoDiscountsCanBeAssignedToTicketTypeException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Tipos de Entrada")
@Feature("Servicio de Tipos de Entrada")
public class TicketTypeServiceTests {

    private static final String TYPE_NAME_STANDARD = "STANDARD";
    private static final String TYPE_NAME_PREMIUM = "PREMIUM";
    private static final String STANDARD_TICKET_DESC = "Standard ticket";
    private static final String PREMIUM_TICKET_DESC = "Premium ticket";
    private static final String CURRENCY_EUR = "EUR";
    private static final String CURRENCY_USD = "USD";
    private static final String PHOTO_URL = "http://example.com/photo.jpg";
    private static final String PHOTO_URL_UPDATED = "http://example.com/updated.jpg";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");
    private static final BigDecimal COST_75_50 = new BigDecimal("75.50");

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private DiscountTicketTypeRepository discountTicketTypeRepository;

    private TicketType sample;

    @BeforeEach
    void setUp() {
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();
        sample = TicketType.builder()
                .cost(COST_50)
                .currency(CURRENCY_EUR)
                .typeName(TYPE_NAME_STANDARD)
                .description(STANDARD_TICKET_DESC)
                .maxPerDay(10)
                .photoUrl(PHOTO_URL)
                .build();
    }

    @AfterEach
    void tearDown() {
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que la lista está vacía inicialmente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("findAll retorna lista vacía inicialmente")
    void testFindAllInitiallyEmpty() {
        List<TicketType> all = ticketTypeService.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    @Story("Guardar Tipo de Entrada")
    @Description("Verifica que save retorna un ID generado")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("save retorna tipo de entrada con ID")
    void testSaveTicketTypeReturnsId() {
        TicketType saved = ticketTypeService.save(sample);
        assertNotNull(saved.getId());
    }

    @Test
    @Story("Guardar Tipo de Entrada")
    @Description("Verifica que save retorna el nombre del tipo correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("save retorna tipo de entrada con nombre correcto")
    void testSaveTicketTypeReturnsTypeName() {
        TicketType saved = ticketTypeService.save(sample);
        assertEquals(TYPE_NAME_STANDARD, saved.getTypeName());
    }

    @Test
    @Story("Buscar Tipo de Entrada por ID")
    @Description("Verifica que findById retorna el ID correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("findById retorna tipo de entrada con ID correcto")
    void testFindByIdExistsReturnsId() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType found = ticketTypeService.findById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @Story("Buscar Tipo de Entrada por ID")
    @Description("Verifica que findById retorna el nombre correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("findById retorna tipo de entrada con nombre correcto")
    void testFindByIdExistsReturnsTypeName() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType found = ticketTypeService.findById(saved.getId());
        assertEquals(TYPE_NAME_STANDARD, found.getTypeName());
    }

    @Test
    @Story("Buscar Tipo de Entrada por ID")
    @Description("Verifica que findById lanza excepción cuando no existe")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("findById lanza excepción cuando no existe")
    void testFindByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> ticketTypeService.findById(99999L));
    }

    @Test
    @Story("Buscar Tipo de Entrada por Nombre")
    @Description("Verifica que findByTypeName retorna el tipo correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("findByTypeName retorna tipo de entrada correcto")
    void testFindByTypeNameExists() {
        ticketTypeService.save(sample);
        TicketType found = ticketTypeService.findByTypeName(TYPE_NAME_STANDARD);
        assertEquals(TYPE_NAME_STANDARD, found.getTypeName());
    }

    @Test
    @Story("Buscar Tipo de Entrada por Nombre")
    @Description("Verifica que findByTypeName lanza excepción cuando no existe")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("findByTypeName lanza excepción cuando no existe")
    void testFindByTypeNameNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> ticketTypeService.findByTypeName("DOES_NOT_EXIST"));
    }

    private TicketType buildUpdateTicketType() {
        return TicketType.builder()
                .cost(COST_75_50)
                .currency(CURRENCY_USD)
                .typeName(TYPE_NAME_PREMIUM)
                .description(PREMIUM_TICKET_DESC)
                .maxPerDay(20)
                .photoUrl(PHOTO_URL_UPDATED)
                .build();
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza el costo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("update actualiza el costo correctamente")
    void testUpdateTicketTypeUpdatesCost() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = buildUpdateTicketType();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(COST_75_50, updated.getCost());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza la moneda")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update actualiza la moneda correctamente")
    void testUpdateTicketTypeUpdatesCurrency() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = buildUpdateTicketType();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(CURRENCY_USD, updated.getCurrency());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza el nombre del tipo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update actualiza el nombre del tipo correctamente")
    void testUpdateTicketTypeUpdatesTypeName() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = buildUpdateTicketType();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(TYPE_NAME_PREMIUM, updated.getTypeName());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza la descripción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update actualiza la descripción correctamente")
    void testUpdateTicketTypeUpdatesDescription() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = buildUpdateTicketType();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(PREMIUM_TICKET_DESC, updated.getDescription());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza el máximo por día")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update actualiza el máximo por día correctamente")
    void testUpdateTicketTypeUpdatesMaxPerDay() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = buildUpdateTicketType();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(20, updated.getMaxPerDay());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update mantiene la URL de foto existente cuando es null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update mantiene URL de foto existente cuando es null")
    void testUpdateTicketTypeWithNullPhotoUrlKeepsExisting() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = TicketType.builder()
                .cost(COST_75_50)
                .currency(CURRENCY_USD)
                .typeName(TYPE_NAME_PREMIUM)
                .description(PREMIUM_TICKET_DESC)
                .maxPerDay(20)
                .photoUrl(null)
                .build();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(PHOTO_URL, updated.getPhotoUrl());
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que delete lanza excepción cuando tiene descuentos asociados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("delete lanza excepción con descuentos asociados")
    void testDeleteTicketTypeWithAssociationsThrows() {
        TicketType saved = ticketTypeService.save(sample);
        Discount discount = discountRepository.save(Discount.builder()
                .discountCode("TEST10")
                .discountPercentage(10)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());
        discountTicketTypeRepository.save(DiscountTicketType.builder()
                .discount(discount)
                .ticketType(saved)
                .build());
        assertThrows(NoDiscountsCanBeAssignedToTicketTypeException.class, () -> ticketTypeService.delete(saved.getId()));
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que delete elimina el tipo de entrada correctamente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("delete elimina el tipo de entrada correctamente")
    void testDeleteTicketType() {
        TicketType saved = ticketTypeService.save(sample);
        Long id = saved.getId();
        ticketTypeService.delete(id);
        assertThrows(ResourceNotFoundException.class, () -> ticketTypeService.findById(id));
    }
}
