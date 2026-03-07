package com.magicworld.tfg_angular_springboot.park_closure;

import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("Cierre del Parque")
@Feature("Servicio de Días de Cierre")
public class ParkClosureDayServiceTests {

    @Mock
    private ParkClosureDayRepository repository;

    private ParkClosureDayService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ParkClosureDayService(repository);
    }

    @Test
    @DisplayName("isClosedDay retorna true si existe cierre")
    @Story("Consulta de Cierres")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que isClosedDay retorna true para fecha con cierre")
    void isClosedDayReturnsTrueWhenExists() {
        when(repository.existsByClosureDate(LocalDate.of(2026, 10, 15))).thenReturn(true);

        assertTrue(service.isClosedDay(LocalDate.of(2026, 10, 15)));
    }

    @Test
    @DisplayName("isClosedDay retorna false si no existe cierre")
    @Story("Consulta de Cierres")
    @Severity(SeverityLevel.CRITICAL)
    void isClosedDayReturnsFalseWhenNotExists() {
        when(repository.existsByClosureDate(LocalDate.of(2026, 3, 10))).thenReturn(false);

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

        when(repository.existsByClosureDate(futureDate)).thenReturn(false);
        when(repository.save(any(ParkClosureDay.class))).thenReturn(closureDay);

        ParkClosureDay saved = service.save(closureDay);

        assertNotNull(saved);
        verify(repository).save(closureDay);
    }

    @Test
    @DisplayName("save lanza excepción si fecha ya existe")
    @Story("Gestión de Cierres")
    @Severity(SeverityLevel.NORMAL)
    void saveThrowsExceptionWhenDateAlreadyExists() {
        LocalDate futureDate = LocalDate.now().plusMonths(3);
        ParkClosureDay closureDay = ParkClosureDay.builder()
                .closureDate(futureDate)
                .reason("Duplicate")
                .build();

        when(repository.existsByClosureDate(futureDate)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.save(closureDay));
    }

    @Test
    @DisplayName("delete lanza excepción si fecha menor a 2 meses")
    @Story("Gestión de Cierres")
    @Severity(SeverityLevel.CRITICAL)
    void deleteThrowsExceptionWhenTooSoon() {
        ParkClosureDay closureDay = ParkClosureDay.builder()
                .closureDate(LocalDate.now().plusDays(30))
                .reason("Test")
                .build();
        closureDay.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(closureDay));

        assertThrows(InvalidOperationException.class, () -> service.delete(1L));
    }

    @Test
    @DisplayName("delete elimina cierre con suficiente antelación")
    @Story("Gestión de Cierres")
    @Severity(SeverityLevel.NORMAL)
    void deleteRemovesClosureWhenFarEnough() {
        ParkClosureDay closureDay = ParkClosureDay.builder()
                .closureDate(LocalDate.now().plusMonths(3))
                .reason("Maintenance")
                .build();
        closureDay.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(closureDay));

        service.delete(1L);

        verify(repository).delete(closureDay);
    }

    @Test
    @DisplayName("findById lanza excepción si no existe")
    @Story("Consulta de Cierres")
    @Severity(SeverityLevel.NORMAL)
    void findByIdThrowsExceptionWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    @DisplayName("findByRange retorna cierres en rango")
    @Story("Consulta de Cierres")
    @Severity(SeverityLevel.NORMAL)
    void findByRangeReturnsClosuresInRange() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        ParkClosureDay day1 = ParkClosureDay.builder().closureDate(LocalDate.of(2026, 10, 5)).reason("Test").build();
        ParkClosureDay day2 = ParkClosureDay.builder().closureDate(LocalDate.of(2026, 10, 20)).reason("Test").build();

        when(repository.findByClosureDateBetween(from, to)).thenReturn(List.of(day1, day2));

        List<ParkClosureDay> result = service.findByRange(from, to);

        assertEquals(2, result.size());
    }
}

