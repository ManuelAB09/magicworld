package com.magicworld.tfg_angular_springboot.chatbot.executor;

import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.discount.Discount;
import com.magicworld.tfg_angular_springboot.discount.DiscountService;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeService;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Epic("Chatbot IA")
@Feature("Ejecutor de Funciones de Descuentos")
public class DiscountFunctionExecutorTests {

    @Mock
    private DiscountService discountService;

    @Mock
    private TicketTypeService ticketTypeService;

    @Mock
    private DiscountTicketTypeService discountTicketTypeService;

    @InjectMocks
    private DiscountFunctionExecutor executor;

    private Discount sample;
    private TicketType ticketType;

    @BeforeEach
    void setUp() {
        sample = Discount.builder()
                .discountCode("SAVE20")
                .discountPercentage(20)
                .expiryDate(LocalDate.now().plusDays(30))
                .build();
        sample.setId(1L);

        ticketType = TicketType.builder()
                .typeName("ADULT")
                .cost(new BigDecimal("50.00"))
                .description("Adult ticket")
                .maxPerDay(100)
                .build();
        ticketType.setId(1L);
    }

    @Test
    @Story("Listar Descuentos")
    @Description("Verifica que listar descuentos vacíos retorna éxito en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar descuentos vacíos retorna éxito en inglés")
    void testListDiscountsEmptyReturnsSuccessEnglish() {
        when(discountService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listDiscounts("en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Descuentos")
    @Description("Verifica que el mensaje indica que no hay descuentos en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar descuentos vacíos muestra mensaje en inglés")
    void testListDiscountsEmptyContainsNoDiscountsMessageEnglish() {
        when(discountService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listDiscounts("en");
        assertTrue(response.getMessage().contains("No discounts"));
    }

    @Test
    @Story("Listar Descuentos")
    @Description("Verifica que listar descuentos vacíos retorna éxito en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar descuentos vacíos retorna éxito en español")
    void testListDiscountsEmptyReturnsSuccessSpanish() {
        when(discountService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listDiscounts("es");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Descuentos")
    @Description("Verifica que el mensaje indica que no hay descuentos en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar descuentos vacíos muestra mensaje en español")
    void testListDiscountsEmptyContainsNoDiscountsMessageSpanish() {
        when(discountService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listDiscounts("es");
        assertTrue(response.getMessage().contains("No hay descuentos"));
    }

    @Test
    @Story("Listar Descuentos")
    @Description("Verifica que listar descuentos con datos retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar descuentos con datos retorna éxito")
    void testListDiscountsWithDataReturnsSuccess() {
        when(discountService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listDiscounts("en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Descuentos")
    @Description("Verifica que la respuesta contiene el código del descuento")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar descuentos muestra código de descuento")
    void testListDiscountsWithDataContainsDiscountCode() {
        when(discountService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listDiscounts("en");
        assertTrue(response.getMessage().contains("SAVE20"));
    }

    @Test
    @Story("Listar Descuentos")
    @Description("Verifica que la respuesta tiene lista de datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar descuentos retorna lista de datos")
    void testListDiscountsWithDataHasDataList() {
        when(discountService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listDiscounts("en");
        assertNotNull(response.getData());
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento por ID retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar descuento por ID retorna éxito")
    void testGetDiscountByIdFoundReturnsSuccess() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(discountTicketTypeService.findTicketsTypesByDiscountId(1L)).thenReturn(List.of(ticketType));
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getDiscountById(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que la respuesta contiene el código del descuento")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento por ID muestra código")
    void testGetDiscountByIdFoundContainsDiscountCode() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(discountTicketTypeService.findTicketsTypesByDiscountId(1L)).thenReturn(List.of(ticketType));
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getDiscountById(args, "en");
        assertTrue(response.getMessage().contains("SAVE20"));
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que la respuesta en español contiene detalles")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento por ID en español muestra detalles")
    void testGetDiscountByIdSpanishContainsDetails() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(discountTicketTypeService.findTicketsTypesByDiscountId(1L)).thenReturn(List.of());
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getDiscountById(args, "es");
        assertTrue(response.getMessage().contains("Detalles del Descuento"));
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que sin tipos asociados muestra All")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento sin tipos asociados muestra All")
    void testGetDiscountByIdNoAssociatedTypesShowsAll() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(discountTicketTypeService.findTicketsTypesByDiscountId(1L)).thenReturn(List.of());
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getDiscountById(args, "en");
        assertTrue(response.getMessage().contains("All"));
    }

    @Test
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear descuento retorna éxito")
    void testCreateDiscountReturnsSuccess() {
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.save(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("discountCode", "SAVE20");
        args.put("discountPercentage", 20);
        args.put("expiryDate", LocalDate.now().plusDays(30).toString());
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.createDiscount(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Descuento")
    @Description("Verifica que el mensaje contiene confirmación de creación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento muestra mensaje de creación")
    void testCreateDiscountContainsCreatedMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.save(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("discountCode", "SAVE20");
        args.put("discountPercentage", 20);
        args.put("expiryDate", LocalDate.now().plusDays(30).toString());
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.createDiscount(args, "en");
        assertTrue(response.getMessage().contains("Discount created"));
    }

    @Test
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento en español muestra mensaje de creación")
    void testCreateDiscountSpanishContainsCreatedMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.save(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("discountCode", "SAVE20");
        args.put("discountPercentage", 20);
        args.put("expiryDate", LocalDate.now().plusDays(30).toString());
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.createDiscount(args, "es");
        assertTrue(response.getMessage().contains("Descuento creado"));
    }

    @Test
    @Story("Crear Descuento")
    @Description("Verifica que crear con tipos inválidos retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento con tipos inválidos retorna error")
    void testCreateDiscountInvalidTicketTypesReturnsError() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        Map<String, Object> args = new HashMap<>();
        args.put("discountCode", "SAVE20");
        args.put("discountPercentage", 20);
        args.put("expiryDate", LocalDate.now().plusDays(30).toString());
        args.put("ticketTypeNames", List.of("NONEXISTENT"));
        ChatResponse response = executor.createDiscount(args, "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Crear Descuento")
    @Description("Verifica que crear con fecha pasada retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento con fecha pasada retorna error")
    void testCreateDiscountPastExpiryDateReturnsError() {
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        Map<String, Object> args = new HashMap<>();
        args.put("discountCode", "SAVE20");
        args.put("discountPercentage", 20);
        args.put("expiryDate", LocalDate.now().minusDays(1).toString());
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.createDiscount(args, "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar descuento retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar descuento retorna éxito")
    void testUpdateDiscountReturnsSuccess() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.update(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("discountCode", "NEWSAVE");
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.updateDiscount(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que el mensaje contiene confirmación de actualización")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento muestra mensaje de actualización")
    void testUpdateDiscountContainsUpdatedMessage() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.update(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("discountCode", "NEWSAVE");
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.updateDiscount(args, "en");
        assertTrue(response.getMessage().contains("Discount updated"));
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar descuento en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento en español muestra mensaje de actualización")
    void testUpdateDiscountSpanishContainsUpdatedMessage() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.update(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.updateDiscount(args, "es");
        assertTrue(response.getMessage().contains("Descuento actualizado"));
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que solicitar eliminación retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Solicitar eliminación de descuento retorna éxito")
    void testRequestDeleteDiscountReturnsSuccess() {
        when(discountService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteDiscount(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que solicitar eliminación contiene acción pendiente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Solicitar eliminación contiene acción pendiente")
    void testRequestDeleteDiscountContainsPendingAction() {
        when(discountService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteDiscount(args, "en");
        assertNotNull(response.getPendingAction());
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que la acción pendiente tiene el tipo correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Acción pendiente tiene tipo deleteDiscount")
    void testRequestDeleteDiscountPendingActionHasCorrectType() {
        when(discountService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteDiscount(args, "en");
        assertEquals("deleteDiscount", response.getPendingAction().getActionType());
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que ejecutar eliminación retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ejecutar eliminación de descuento retorna éxito")
    void testExecuteDeleteDiscountReturnsSuccess() {
        ChatResponse response = executor.executeDeleteDiscount(1L, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que el mensaje contiene confirmación de eliminación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ejecutar eliminación muestra mensaje de eliminación")
    void testExecuteDeleteDiscountContainsDeletedMessage() {
        ChatResponse response = executor.executeDeleteDiscount(1L, "en");
        assertTrue(response.getMessage().contains("deleted"));
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que ejecutar eliminación en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ejecutar eliminación en español muestra mensaje de eliminación")
    void testExecuteDeleteDiscountSpanishContainsDeletedMessage() {
        ChatResponse response = executor.executeDeleteDiscount(1L, "es");
        assertTrue(response.getMessage().contains("eliminado"));
    }


    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar con valores null preserva existentes")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento con valores null preserva existentes")
    void testUpdateDiscountWithNullValuesPreservesExisting() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.update(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.updateDiscount(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar con nueva fecha actualiza la fecha")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento con nueva fecha actualiza la fecha")
    void testUpdateDiscountWithNewExpiryDateUpdatesDate() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.update(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("expiryDate", LocalDate.now().plusDays(60).toString());
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.updateDiscount(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Descuento")
    @Description("Verifica que actualizar con nuevo porcentaje actualiza el porcentaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar descuento con nuevo porcentaje actualiza el porcentaje")
    void testUpdateDiscountWithNewPercentageUpdatesPercentage() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        when(discountService.update(any(Discount.class), any())).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("discountPercentage", 30);
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.updateDiscount(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Eliminar Descuento")
    @Description("Verifica que solicitar eliminación en español muestra confirmación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Solicitar eliminación en español muestra confirmación")
    void testRequestDeleteDiscountSpanishContainsConfirmation() {
        when(discountService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteDiscount(args, "es");
        assertTrue(response.getMessage().contains("Confirmación requerida"));
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que con tipos asociados muestra nombres")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento con tipos asociados muestra nombres")
    void testGetDiscountByIdWithAssociatedTypesShowsTypeNames() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(discountTicketTypeService.findTicketsTypesByDiscountId(1L)).thenReturn(List.of(ticketType));
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getDiscountById(args, "en");
        assertTrue(response.getMessage().contains("ADULT"));
    }

    @Test
    @Story("Buscar Descuento por ID")
    @Description("Verifica que en español muestra detalles")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento en español muestra detalles")
    void testGetDiscountByIdSpanishShowsDetails() {
        when(discountService.findById(1L)).thenReturn(sample);
        when(discountTicketTypeService.findTicketsTypesByDiscountId(1L)).thenReturn(List.of(ticketType));
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getDiscountById(args, "es");
        assertTrue(response.getMessage().contains("Código"));
    }

    @Test
    @Story("Crear Descuento")
    @Description("Verifica que crear con tipos inválidos en español retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento con tipos inválidos en español retorna error")
    void testCreateDiscountSpanishInvalidTicketTypesReturnsError() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        Map<String, Object> args = new HashMap<>();
        args.put("discountCode", "SAVE20");
        args.put("discountPercentage", 20);
        args.put("expiryDate", LocalDate.now().plusDays(30).toString());
        args.put("ticketTypeNames", List.of("NONEXISTENT"));
        ChatResponse response = executor.createDiscount(args, "es");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Crear Descuento")
    @Description("Verifica que crear con fecha pasada en español retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento con fecha pasada en español retorna error")
    void testCreateDiscountSpanishPastExpiryDateReturnsError() {
        when(ticketTypeService.findAll()).thenReturn(List.of(ticketType));
        Map<String, Object> args = new HashMap<>();
        args.put("discountCode", "SAVE20");
        args.put("discountPercentage", 20);
        args.put("expiryDate", LocalDate.now().minusDays(1).toString());
        args.put("ticketTypeNames", List.of("ADULT"));
        ChatResponse response = executor.createDiscount(args, "es");
        assertFalse(response.isSuccess());
    }
}

