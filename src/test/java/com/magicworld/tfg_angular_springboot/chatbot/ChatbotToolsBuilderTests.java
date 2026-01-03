package com.magicworld.tfg_angular_springboot.chatbot;

import com.google.genai.types.Tool;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Chatbot IA")
@Feature("Constructor de Herramientas")
public class ChatbotToolsBuilderTests {

    private ChatbotToolsBuilder toolsBuilder;

    @BeforeEach
    void setUp() {
        toolsBuilder = new ChatbotToolsBuilder();
    }

    @Test
    @Story("Construcción de Herramientas")
    @Description("Verifica que buildTools retorna lista no vacía")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("buildTools retorna lista no vacía")
    void testBuildToolsReturnsNonEmptyList() {
        List<Tool> tools = toolsBuilder.buildTools();
        assertFalse(tools.isEmpty());
    }

    @Test
    @Story("Construcción de Herramientas")
    @Description("Verifica que buildTools retorna una sola herramienta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("buildTools retorna una herramienta")
    void testBuildToolsReturnsSingleTool() {
        List<Tool> tools = toolsBuilder.buildTools();
        assertEquals(1, tools.size());
    }

    @Test
    @Story("Construcción de Herramientas")
    @Description("Verifica que la herramienta tiene declaraciones de funciones")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Herramienta tiene declaraciones de funciones")
    void testBuildToolsToolHasFunctionDeclarations() {
        List<Tool> tools = toolsBuilder.buildTools();
        assertNotNull(tools.getFirst().functionDeclarations());
    }

    @Test
    @Story("Construcción de Herramientas")
    @Description("Verifica que las declaraciones de funciones están presentes")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Declaraciones de funciones presentes")
    void testBuildToolsHasFunctionDeclarations() {
        List<Tool> tools = toolsBuilder.buildTools();
        assertTrue(tools.getFirst().functionDeclarations().isPresent());
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que contiene función listDiscounts")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función listDiscounts")
    void testBuildToolsContainsListDiscountsFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasListDiscounts = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("listDiscounts"));
        assertTrue(hasListDiscounts);
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que contiene función getDiscountById")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función getDiscountById")
    void testBuildToolsContainsGetDiscountByIdFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasGetDiscountById = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("getDiscountById"));
        assertTrue(hasGetDiscountById);
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que contiene función createDiscount")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función createDiscount")
    void testBuildToolsContainsCreateDiscountFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasCreateDiscount = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("createDiscount"));
        assertTrue(hasCreateDiscount);
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que contiene función updateDiscount")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función updateDiscount")
    void testBuildToolsContainsUpdateDiscountFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasUpdateDiscount = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("updateDiscount"));
        assertTrue(hasUpdateDiscount);
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que contiene función requestDeleteDiscount")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función requestDeleteDiscount")
    void testBuildToolsContainsRequestDeleteDiscountFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasRequestDeleteDiscount = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("requestDeleteDiscount"));
        assertTrue(hasRequestDeleteDiscount);
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que contiene función listTicketTypes")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función listTicketTypes")
    void testBuildToolsContainsListTicketTypesFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasListTicketTypes = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("listTicketTypes"));
        assertTrue(hasListTicketTypes);
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que contiene función getTicketTypeById")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función getTicketTypeById")
    void testBuildToolsContainsGetTicketTypeByIdFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasGetTicketTypeById = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("getTicketTypeById"));
        assertTrue(hasGetTicketTypeById);
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que contiene función findTicketTypeByName")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función findTicketTypeByName")
    void testBuildToolsContainsFindTicketTypeByNameFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasFindTicketTypeByName = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("findTicketTypeByName"));
        assertTrue(hasFindTicketTypeByName);
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que contiene función createTicketType")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función createTicketType")
    void testBuildToolsContainsCreateTicketTypeFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasCreateTicketType = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("createTicketType"));
        assertTrue(hasCreateTicketType);
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que contiene función updateTicketType")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función updateTicketType")
    void testBuildToolsContainsUpdateTicketTypeFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasUpdateTicketType = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("updateTicketType"));
        assertTrue(hasUpdateTicketType);
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que contiene función requestDeleteTicketType")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función requestDeleteTicketType")
    void testBuildToolsContainsRequestDeleteTicketTypeFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasRequestDeleteTicketType = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("requestDeleteTicketType"));
        assertTrue(hasRequestDeleteTicketType);
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que contiene función listAttractions")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función listAttractions")
    void testBuildToolsContainsListAttractionsFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasListAttractions = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("listAttractions"));
        assertTrue(hasListAttractions);
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que contiene función getAttractionById")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función getAttractionById")
    void testBuildToolsContainsGetAttractionByIdFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasGetAttractionById = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("getAttractionById"));
        assertTrue(hasGetAttractionById);
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que contiene función createAttraction")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función createAttraction")
    void testBuildToolsContainsCreateAttractionFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasCreateAttraction = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("createAttraction"));
        assertTrue(hasCreateAttraction);
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que contiene función updateAttraction")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función updateAttraction")
    void testBuildToolsContainsUpdateAttractionFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasUpdateAttraction = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("updateAttraction"));
        assertTrue(hasUpdateAttraction);
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que contiene función requestDeleteAttraction")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Contiene función requestDeleteAttraction")
    void testBuildToolsContainsRequestDeleteAttractionFunction() {
        List<Tool> tools = toolsBuilder.buildTools();
        boolean hasRequestDeleteAttraction = tools.getFirst().functionDeclarations().get().stream()
                .anyMatch(f -> f.name().isPresent() && f.name().get().equals("requestDeleteAttraction"));
        assertTrue(hasRequestDeleteAttraction);
    }
}
