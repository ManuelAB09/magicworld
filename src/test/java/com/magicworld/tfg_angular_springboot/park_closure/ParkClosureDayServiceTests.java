package com.magicworld.tfg_angular_springboot.park_closure;

import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Cierre del Parque")
@Feature("Servicio de Días de Cierre")
public class ParkClosureDayServiceTests {

    @Autowired
    private ParkClosureDayRepository repository;

    @Autowired
    private ParkClosureDayService service;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("isClosedDay retorna true si existe cierre")
    @Story("Consulta de Cierres")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que isClosedDay retorna true para fecha con cierre")
    void isClosedDayReturnsTrueWhenExists() {
        LocalDate closureDate = LocalDate.of(2026, 10, 15);
        repository.save(ParkClosureDay.builder()
                .closureDate(closureDate)
                .reason("Maintenance")
                .build());

        assertTrue(service.isClosedDay(closureDate));
    }

    @Test
    @DisplayName("isClosedDay retorna false si no existe cierre")
    @Story("Consulta de Cierres")
    @Severity(SeverityLevel.CRITICAL)
    void isClosedDayReturnsFalseWhenNotExists() {
        assertFalse(service.isClosedDay(LocalDate.of(2026, 3, 10)));
    }

    @Test
    @DisplayName("save lanza excepción si fecha menor a 2 meses")
    @Story("Gestión de Cierres")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que no se puede crear cierre con menos de 2 meses de antelación")
    void saveThrowsExceptionWhenTooSoon() {
        ParkClosureDay closureDay = ParkClosureDay.builder()
                .closureDate(LocalDate.now().plusDays(30))
                .reason("Test closure")
                .build();

        assertThrows(InvalidOperationException.class, () -> service.save(closureDay));
    }

    @Test
    @DisplayName("save guarda cierre con suficiente antelación")
    @Story("Gestión de Cierres")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que se puede crear cierre con más de 2 meses de antelación")
    void saveSavesClosureWhenFarEnough() {
        LocalDate futureDate = LocalDate.now().plusMonths(3);
        ParkClosureDay closureDay = ParkClosureDay.builder()
                .closureDate(futureDate)
                .reason("Maintenance")
                .build();

        ParkClosureDay saved = service.save(closureDay);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(futureDate, saved.getClosureDate());
    }

    @Test
    @DisplayName("save lanza excepción si fecha ya existe")
    @Story("Gestión de Cierres")
    @Severity(SeverityLevel.NORMAL)
    void saveThrowsExceptionWhenDateAlreadyExists() {
        LocalDate futureDate = LocalDate.now().plusMonths(3);
        repository.save(ParkClosureDay.builder()
                .closureDate(futureDate)
                .reason("First closure")
                .build());

        ParkClosureDay duplicate = ParkClosureDay.builder()
                .closureDate(futureDate)
                .reason("Duplicate")
                .build();

        assertThrows(BadRequestException.class, () -> service.save(duplicate));
    }

    @Test
    @DisplayName("delete lanza excepción si fecha menor a 2 meses")
    @Story("Gestión de Cierres")
    @Severity(SeverityLevel.CRITICAL)
    void deleteThrowsExceptionWhenTooSoon() {
        // Save directly via repository to bypass service validation
        ParkClosureDay closureDay = repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.now().plusDays(30))
                .reason("Test")
                .build());

        assertThrows(InvalidOperationException.class, () -> service.delete(closureDay.getId()));
    }

    @Test
    @DisplayName("delete elimina cierre con suficiente antelación")
    @Story("Gestión de Cierres")
    @Severity(SeverityLevel.NORMAL)
    void deleteRemovesClosureWhenFarEnough() {
        ParkClosureDay closureDay = repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.now().plusMonths(3))
                .reason("Maintenance")
                .build());
        Long id = closureDay.getId();

        service.delete(id);

        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }

    @Test
    @DisplayName("findById lanza excepción si no existe")
    @Story("Consulta de Cierres")
    @Severity(SeverityLevel.NORMAL)
    void findByIdThrowsExceptionWhenNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    @DisplayName("findByRange retorna cierres en rango")
    @Story("Consulta de Cierres")
    @Severity(SeverityLevel.NORMAL)
    void findByRangeReturnsClosuresInRange() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.of(2026, 10, 5))
                .reason("Test 1")
                .build());
        repository.save(ParkClosureDay.builder()
                .closureDate(LocalDate.of(2026, 10, 20))
                .reason("Test 2")
                .build());

        List<ParkClosureDay> result = service.findByRange(from, to);

        assertEquals(2, result.size());
    }
}
