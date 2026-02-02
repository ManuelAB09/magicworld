package com.magicworld.tfg_angular_springboot.chatbot;

import com.google.genai.types.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Component responsible for building Gemini tools (function declarations)
 * for the chatbot.
 */
@Component
public class ChatbotToolsBuilder {

    public List<Tool> buildTools() {
        List<FunctionDeclaration> functions = new ArrayList<>();

        // Descuentos
        functions.addAll(buildDiscountFunctions());

        // Tipos de entrada
        functions.addAll(buildTicketTypeFunctions());

        // Atracciones
        functions.addAll(buildAttractionFunctions());

        return List.of(Tool.builder()
                .functionDeclarations(functions)
                .build());
    }

    private List<FunctionDeclaration> buildDiscountFunctions() {
        List<FunctionDeclaration> functions = new ArrayList<>();

        functions.add(FunctionDeclaration.builder()
                .name("listDiscounts")
                .description("Lists all available discounts / Lista todos los descuentos disponibles")
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("getDiscountById")
                .description("Gets detailed information about a specific discount by ID / Obtiene información detallada de un descuento por su ID")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "id", Schema.builder().type("INTEGER").description("ID of the discount / ID del descuento").build()
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("createDiscount")
                .description("Creates a new discount / Crea un nuevo descuento")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "discountCode", Schema.builder().type("STRING").description("Discount code (max 20 chars) / Código del descuento").build(),
                                "discountPercentage", Schema.builder().type("INTEGER").description("Discount percentage (1-100) / Porcentaje de descuento").build(),
                                "expiryDate", Schema.builder().type("STRING").description("Expiry date in YYYY-MM-DD format / Fecha de expiración").build(),
                                "ticketTypeNames", Schema.builder().type("ARRAY").items(Schema.builder().type("STRING").build()).description("List of ticket type names to apply discount / Lista de nombres de tipos de entrada").build()
                        ))
                        .required(List.of("discountCode", "discountPercentage", "expiryDate", "ticketTypeNames"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("updateDiscount")
                .description("Updates an existing discount. Only provide the fields you want to change / Actualiza un descuento existente. Solo proporciona los campos que deseas cambiar")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "id", Schema.builder().type("INTEGER").description("ID of the discount to update (required) / ID del descuento (requerido)").build(),
                                "discountCode", Schema.builder().type("STRING").description("New discount code (optional) / Nuevo código (opcional)").build(),
                                "discountPercentage", Schema.builder().type("INTEGER").description("New percentage (optional) / Nuevo porcentaje (opcional)").build(),
                                "expiryDate", Schema.builder().type("STRING").description("New expiry date (optional) / Nueva fecha (opcional)").build(),
                                "ticketTypeNames", Schema.builder().type("ARRAY").items(Schema.builder().type("STRING").build()).description("Ticket type names (optional) / Nombres de tipos de entrada (opcional)").build()
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("requestDeleteDiscount")
                .description("Requests to delete a discount (needs confirmation) / Solicita eliminar un descuento")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "id", Schema.builder().type("INTEGER").description("ID of the discount / ID del descuento").build()
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        return functions;
    }

    private List<FunctionDeclaration> buildTicketTypeFunctions() {
        List<FunctionDeclaration> functions = new ArrayList<>();

        functions.add(FunctionDeclaration.builder()
                .name("listTicketTypes")
                .description("Lists all ticket types / Lista todos los tipos de entrada")
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("getTicketTypeById")
                .description("Gets detailed information about a specific ticket type by ID / Obtiene información detallada de un tipo de entrada por su ID")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "id", Schema.builder().type("INTEGER").description("ID of the ticket type / ID del tipo de entrada").build()
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("findTicketTypeByName")
                .description("Finds a ticket type by name (case insensitive) / Busca un tipo de entrada por nombre")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "name", Schema.builder().type("STRING").description("Name to search / Nombre a buscar").build()
                        ))
                        .required(List.of("name"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("createTicketType")
                .description("Creates a new ticket type / Crea un nuevo tipo de entrada")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "typeName", Schema.builder().type("STRING").description("Unique name / Nombre único").build(),
                                "cost", Schema.builder().type("NUMBER").description("Price / Precio").build(),
                                "description", Schema.builder().type("STRING").description("Description / Descripción").build(),
                                "maxPerDay", Schema.builder().type("INTEGER").description("Max per day / Máximo por día").build(),
                                "photoUrl", Schema.builder().type("STRING").description("Photo URL (optional) / URL foto").build()
                        ))
                        .required(List.of("typeName", "cost", "description", "maxPerDay"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("updateTicketType")
                .description("Updates an existing ticket type. Only provide the fields you want to change / Actualiza un tipo de entrada. Solo proporciona los campos que deseas cambiar")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "id", Schema.builder().type("INTEGER").description("ID of the ticket type (required) / ID del tipo (requerido)").build(),
                                "typeName", Schema.builder().type("STRING").description("Name (optional) / Nombre (opcional)").build(),
                                "cost", Schema.builder().type("NUMBER").description("Price (optional) / Precio (opcional)").build(),
                                "description", Schema.builder().type("STRING").description("Description (optional) / Descripción (opcional)").build(),
                                "maxPerDay", Schema.builder().type("INTEGER").description("Max per day (optional) / Máximo por día (opcional)").build(),
                                "photoUrl", Schema.builder().type("STRING").description("Photo URL (optional) / URL foto (opcional)").build()
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("requestDeleteTicketType")
                .description("Requests to delete a ticket type (needs confirmation) / Solicita eliminar un tipo de entrada")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "id", Schema.builder().type("INTEGER").description("ID of the ticket type / ID del tipo").build()
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        return functions;
    }

    private List<FunctionDeclaration> buildAttractionFunctions() {
        List<FunctionDeclaration> functions = new ArrayList<>();

        functions.add(FunctionDeclaration.builder()
                .name("listAttractions")
                .description("Lists all attractions / Lista todas las atracciones")
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("getAttractionById")
                .description("Gets detailed information about a specific attraction by ID / Obtiene información detallada de una atracción por su ID")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "id", Schema.builder().type("INTEGER").description("ID of the attraction / ID de la atracción").build()
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("createAttraction")
                .description("Creates a new attraction. Map coordinates are optional (defaults to center 50,50). Use the web form with 3D preview for precise positioning. / Crea una nueva atracción. Las coordenadas del mapa son opcionales (por defecto centro 50,50). Usa el formulario web con preview 3D para posicionamiento preciso.")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.ofEntries(
                                Map.entry("name", Schema.builder().type("STRING").description("Name / Nombre").build()),
                                Map.entry("intensity", Schema.builder().type("STRING").description("Intensity: LOW, MEDIUM or HIGH / Intensidad").build()),
                                Map.entry("category", Schema.builder().type("STRING").description("Category: ROLLER_COASTER, FERRIS_WHEEL, CAROUSEL, WATER_RIDE, HAUNTED_HOUSE, DROP_TOWER, BUMPER_CARS, TRAIN_RIDE, SWING_RIDE, OTHER / Categoría").build()),
                                Map.entry("minimumHeight", Schema.builder().type("INTEGER").description("Minimum height in cm / Altura mínima").build()),
                                Map.entry("minimumAge", Schema.builder().type("INTEGER").description("Minimum age / Edad mínima").build()),
                                Map.entry("minimumWeight", Schema.builder().type("INTEGER").description("Minimum weight in kg / Peso mínimo").build()),
                                Map.entry("description", Schema.builder().type("STRING").description("Description / Descripción").build()),
                                Map.entry("photoUrl", Schema.builder().type("STRING").description("Photo URL (optional) / URL foto").build()),
                                Map.entry("isActive", Schema.builder().type("BOOLEAN").description("Is active / Está activa").build()),
                                Map.entry("mapPositionX", Schema.builder().type("NUMBER").description("Map X position 0-100 (optional, default 50) / Posición X en mapa 0-100 (opcional, por defecto 50)").build()),
                                Map.entry("mapPositionY", Schema.builder().type("NUMBER").description("Map Y position 0-100 (optional, default 50) / Posición Y en mapa 0-100 (opcional, por defecto 50)").build())
                        ))
                        .required(List.of("name", "intensity", "category", "minimumHeight", "minimumAge", "minimumWeight", "description", "isActive"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("updateAttraction")
                .description("Updates an existing attraction. Only provide the fields you want to change. For precise map positioning, use the web form with 3D preview. / Actualiza una atracción. Solo proporciona los campos que deseas cambiar. Para posicionamiento preciso en el mapa, usa el formulario web con preview 3D.")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.ofEntries(
                                Map.entry("id", Schema.builder().type("INTEGER").description("ID of the attraction (required) / ID de la atracción (requerido)").build()),
                                Map.entry("name", Schema.builder().type("STRING").description("Name (optional) / Nombre (opcional)").build()),
                                Map.entry("intensity", Schema.builder().type("STRING").description("Intensity: LOW, MEDIUM or HIGH (optional) / Intensidad (opcional)").build()),
                                Map.entry("category", Schema.builder().type("STRING").description("Category: ROLLER_COASTER, FERRIS_WHEEL, CAROUSEL, WATER_RIDE, HAUNTED_HOUSE, DROP_TOWER, BUMPER_CARS, TRAIN_RIDE, SWING_RIDE, OTHER (optional) / Categoría (opcional)").build()),
                                Map.entry("minimumHeight", Schema.builder().type("INTEGER").description("Minimum height in cm (optional) / Altura mínima (opcional)").build()),
                                Map.entry("minimumAge", Schema.builder().type("INTEGER").description("Minimum age (optional) / Edad mínima (opcional)").build()),
                                Map.entry("minimumWeight", Schema.builder().type("INTEGER").description("Minimum weight in kg (optional) / Peso mínimo (opcional)").build()),
                                Map.entry("description", Schema.builder().type("STRING").description("Description (optional) / Descripción (opcional)").build()),
                                Map.entry("photoUrl", Schema.builder().type("STRING").description("Photo URL (optional) / URL foto (opcional)").build()),
                                Map.entry("isActive", Schema.builder().type("BOOLEAN").description("Is active (optional) / Está activa (opcional)").build()),
                                Map.entry("mapPositionX", Schema.builder().type("NUMBER").description("Map X position 0-100 (optional) / Posición X en mapa 0-100 (opcional)").build()),
                                Map.entry("mapPositionY", Schema.builder().type("NUMBER").description("Map Y position 0-100 (optional) / Posición Y en mapa 0-100 (opcional)").build())
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        functions.add(FunctionDeclaration.builder()
                .name("requestDeleteAttraction")
                .description("Requests to delete an attraction (needs confirmation) / Solicita eliminar una atracción")
                .parameters(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                                "id", Schema.builder().type("INTEGER").description("ID of the attraction / ID de la atracción").build()
                        ))
                        .required(List.of("id"))
                        .build())
                .build());

        return functions;
    }
}

