package com.magicworld.tfg_angular_springboot.ticket_type;

import com.magicworld.tfg_angular_springboot.storage.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {
    private final TicketTypeService ticketTypeService;
    private final ImageStorageService imageStorageService;

    @Operation(summary = "Get all ticket types", description = "Retrieve a list of all ticket types", tags = {"TicketTypes"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of ticket types", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<TicketType>> getAllTicketTypes() {
        List<TicketType> list = ticketTypeService.findAll();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get ticket type by id", description = "Retrieve a ticket type by its id", tags = {"TicketTypes"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket type found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Ticket type not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketType> getTicketTypeById(@PathVariable Long id) {
        TicketType ticketType = ticketTypeService.findById(id);
        return ResponseEntity.ok(ticketType);
    }


    @Operation(summary = "Create ticket type", description = "Create a new ticket type (JSON)", tags = {"TicketTypes"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket type created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid ticket type data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TicketType> createTicketType(@RequestBody @Valid TicketType ticketType) {
        TicketType saved = ticketTypeService.save(ticketType);
        return ResponseEntity.created(URI.create("/api/v1/ticket-types/" + saved.getId())).body(saved);
    }


    @Operation(summary = "Create ticket type (multipart)", description = "Create a new ticket type with image upload", tags = {"TicketTypes"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket type created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid ticket type data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketType> createTicketTypeMultipart(@RequestPart("data") @Valid TicketTypeRequest request,
                                                                @RequestPart("photo") MultipartFile photo) {
        String url = imageStorageService.store(photo, "ticket-types");
        TicketType toSave = TicketType.builder()
                .cost(request.getCost())
                .currency(request.getCurrency())
                .typeName(request.getTypeName())
                .description(request.getDescription())
                .maxPerDay(request.getMaxPerDay())
                .photoUrl(url)
                .build();
        TicketType saved = ticketTypeService.save(toSave);
        return ResponseEntity.created(URI.create("/api/v1/ticket-types/" + saved.getId())).body(saved);
    }


    @Operation(summary = "Update ticket type", description = "Update an existing ticket type (JSON)", tags = {"TicketTypes"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket type updated", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Ticket type not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid ticket type data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TicketType> updateTicketType(@PathVariable Long id, @RequestBody @Valid TicketType updatedTicketType) {
        TicketType saved = ticketTypeService.update(id, updatedTicketType);
        return ResponseEntity.ok(saved);
    }

    // MULTIPART update con opción de nueva imagen
    @Operation(summary = "Update ticket type (multipart)", description = "Update an existing ticket type with optional image upload", tags = {"TicketTypes"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket type updated", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Ticket type not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid ticket type data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketType> updateTicketTypeMultipart(@PathVariable Long id,
                                                                @RequestPart("data") @Valid TicketTypeRequest request,
                                                                @RequestPart(value = "photo", required = false) MultipartFile photo) {
        String url = null;
        if (photo != null && !photo.isEmpty()) {
            url = imageStorageService.store(photo, "ticket-types");
        }
        TicketType update = TicketType.builder()
                .cost(request.getCost())
                .currency(request.getCurrency())
                .typeName(request.getTypeName())
                .description(request.getDescription())
                .maxPerDay(request.getMaxPerDay())
                .photoUrl(url)
                .build();
        TicketType saved = ticketTypeService.update(id, update);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Delete ticket type", description = "Delete a ticket type by id", tags = {"TicketTypes"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket type deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket type not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "The ticket type has a discount associated", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicketType(@PathVariable Long id) {
        ticketTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
