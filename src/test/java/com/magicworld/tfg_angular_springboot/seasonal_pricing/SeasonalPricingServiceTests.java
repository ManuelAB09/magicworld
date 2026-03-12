package com.magicworld.tfg_angular_springboot.seasonal_pricing;

import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import io.qameta.allure.*;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Precios Estacionales")
@Feature("Servicio de Precios Estacionales")
public class SeasonalPricingServiceTests {

    @Autowired
    private SeasonalPricingRepository repository;

    @Autowired
    private SeasonalPricingService service;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("getMultiplier retorna 1.0 sin reglas activas")
    @Story("Cálculo de Multiplicador")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que sin reglas activas el multiplicador es 1.0")
    void getMultiplierReturnsOneWhenNoActiveRules() {
        BigDecimal multiplier = service.getMultiplier(LocalDate.of(2026, 3, 10)); // Tuesday

        assertEquals(0, BigDecimal.ONE.compareTo(multiplier));
    }

    @Test
    @DisplayName("getMultiplier aplica multiplicador de fin de semana")
    @Story("Cálculo de Multiplicador")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se aplica el multiplicador en fin de semana")
    void getMultiplierAppliesWeekendMultiplier() {
        repository.save(SeasonalPricing.builder()
                .name("Weekend surcharge")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .multiplier(new BigDecimal("1.25"))
                .applyOnWeekdays(false)
                .applyOnWeekends(true)
                .build());

        // 2026-03-07 is a Saturday
        BigDecimal multiplier = service.getMultiplier(LocalDate.of(2026, 3, 7));

        assertEquals(0, new BigDecimal("1.25").compareTo(multiplier));
    }

    @Test
    @DisplayName("getMultiplier no aplica regla de fin de semana en día laborable")
    @Story("Cálculo de Multiplicador")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la regla de fin de semana no se aplica en laborable")
    void getMultiplierDoesNotApplyWeekendRuleOnWeekday() {
        repository.save(SeasonalPricing.builder()
                .name("Weekend surcharge")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .multiplier(new BigDecimal("1.25"))
                .applyOnWeekdays(false)
                .applyOnWeekends(true)
                .build());

        // 2026-03-10 is a Tuesday
        BigDecimal multiplier = service.getMultiplier(LocalDate.of(2026, 3, 10));

        assertEquals(0, BigDecimal.ONE.compareTo(multiplier));
    }

    @Test
    @DisplayName("getMultiplier acumula multiplicadores")
    @Story("Cálculo de Multiplicador")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que los multiplicadores se acumulan (se multiplican)")
    void getMultiplierAccumulatesMultipliers() {
        repository.save(SeasonalPricing.builder()
                .name("Weekend surcharge")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .multiplier(new BigDecimal("1.25"))
                .applyOnWeekdays(false)
                .applyOnWeekends(true)
                .build());

        repository.save(SeasonalPricing.builder()
                .name("Summer surcharge")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .multiplier(new BigDecimal("1.30"))
                .applyOnWeekdays(true)
                .applyOnWeekends(true)
                .build());

        // 2026-06-06 is a Saturday in summer
        BigDecimal multiplier = service.getMultiplier(LocalDate.of(2026, 6, 6));

        // 1.25 * 1.30 = 1.625
        BigDecimal expected = new BigDecimal("1.25").multiply(new BigDecimal("1.30"));
        assertEquals(0, expected.compareTo(multiplier));
    }

    @Test
    @DisplayName("save lanza excepción si fecha fin antes de fecha inicio")
    @Story("CRUD de Reglas")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que no se puede crear regla con fecha fin antes de inicio")
    void saveThrowsExceptionWhenEndBeforeStart() {
        SeasonalPricing pricing = SeasonalPricing.builder()
                .name("Invalid")
                .startDate(LocalDate.of(2026, 12, 31))
                .endDate(LocalDate.of(2026, 1, 1))
                .multiplier(new BigDecimal("1.25"))
                .applyOnWeekdays(true)
                .applyOnWeekends(true)
                .build();

        assertThrows(BadRequestException.class, () -> service.save(pricing));
    }

    @Test
    @DisplayName("findById lanza excepción si no existe")
    @Story("CRUD de Reglas")
    @Severity(SeverityLevel.NORMAL)
    void findByIdThrowsExceptionWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    @DisplayName("delete elimina regla existente")
    @Story("CRUD de Reglas")
    @Severity(SeverityLevel.NORMAL)
    void deleteRemovesExistingRule() {
        SeasonalPricing saved = repository.save(SeasonalPricing.builder()
                .name("Test")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .multiplier(new BigDecimal("1.25"))
                .applyOnWeekdays(true)
                .applyOnWeekends(true)
                .build());

        service.delete(saved.getId());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(saved.getId()));
    }
}
