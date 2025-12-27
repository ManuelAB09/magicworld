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
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .typeName("STANDARD")
                .description("Standard ticket")
                .maxPerDay(10)
                .photoUrl("http://example.com/photo.jpg")
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
    void testSaveTicketType_returnsId() {
        TicketType saved = ticketTypeService.save(sample);
        assertNotNull(saved.getId());
    }

    @Test
    @Story("Guardar Tipo de Entrada")
    @Description("Verifica que save retorna el nombre del tipo correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("save retorna tipo de entrada con nombre correcto")
    void testSaveTicketType_returnsTypeName() {
        TicketType saved = ticketTypeService.save(sample);
        assertEquals("STANDARD", saved.getTypeName());
    }

    @Test
    @Story("Buscar Tipo de Entrada por ID")
    @Description("Verifica que findById retorna el ID correcto")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("findById retorna tipo de entrada con ID correcto")
    void testFindByIdExists_returnsId() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType found = ticketTypeService.findById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @Story("Buscar Tipo de Entrada por ID")
    @Description("Verifica que findById retorna el nombre correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("findById retorna tipo de entrada con nombre correcto")
    void testFindByIdExists_returnsTypeName() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType found = ticketTypeService.findById(saved.getId());
        assertEquals("STANDARD", found.getTypeName());
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
        TicketType found = ticketTypeService.findByTypeName("STANDARD");
        assertEquals("STANDARD", found.getTypeName());
    }

    @Test
    @Story("Buscar Tipo de Entrada por Nombre")
    @Description("Verifica que findByTypeName lanza excepción cuando no existe")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("findByTypeName lanza excepción cuando no existe")
    void testFindByTypeNameNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> ticketTypeService.findByTypeName("DOES_NOT_EXIST"));
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza el costo")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("update actualiza el costo correctamente")
    void testUpdateTicketType_updatesCost() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .photoUrl("http://example.com/updated.jpg")
                .build();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(new BigDecimal("75.50"), updated.getCost());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza la moneda")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update actualiza la moneda correctamente")
    void testUpdateTicketType_updatesCurrency() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .photoUrl("http://example.com/updated.jpg")
                .build();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals("USD", updated.getCurrency());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza el nombre del tipo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update actualiza el nombre del tipo correctamente")
    void testUpdateTicketType_updatesTypeName() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .photoUrl("http://example.com/updated.jpg")
                .build();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals("PREMIUM", updated.getTypeName());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza la descripción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update actualiza la descripción correctamente")
    void testUpdateTicketType_updatesDescription() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .photoUrl("http://example.com/updated.jpg")
                .build();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals("Premium ticket", updated.getDescription());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update actualiza el máximo por día")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update actualiza el máximo por día correctamente")
    void testUpdateTicketType_updatesMaxPerDay() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .photoUrl("http://example.com/updated.jpg")
                .build();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals(20, updated.getMaxPerDay());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que update mantiene la URL de foto existente cuando es null")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("update mantiene URL de foto existente cuando es null")
    void testUpdateTicketType_withNullPhotoUrl_keepsExisting() {
        TicketType saved = ticketTypeService.save(sample);
        TicketType update = TicketType.builder()
                .cost(new BigDecimal("75.50"))
                .currency("USD")
                .typeName("PREMIUM")
                .description("Premium ticket")
                .maxPerDay(20)
                .photoUrl(null)
                .build();
        TicketType updated = ticketTypeService.update(saved.getId(), update);
        assertEquals("http://example.com/photo.jpg", updated.getPhotoUrl());
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que delete lanza excepción cuando tiene descuentos asociados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("delete lanza excepción con descuentos asociados")
    void testDeleteTicketType_withAssociations_throws() {
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
