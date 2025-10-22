package com.magicworld.tfg_angular_springboot.discount;

import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;
    private final DiscountTicketTypeService discountTicketTypeService;

    // DTO para create/update
    @Getter
    @Setter
    public static class DiscountRequest {
        @NotNull
        @Valid
        private Discount discount;
        @NotNull
        @NotEmpty
        private List<String> applicableTicketTypesNames;
    }

    @Operation(summary = "Create discount", description = "Create a new discount and associate ticket types", tags = {"Discounts"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Discount created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid discount data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Discount> create(@RequestBody @Valid DiscountRequest request) {
        Discount saved = discountService.save(request.getDiscount(), request.getApplicableTicketTypesNames());
        return ResponseEntity.created(URI.create("/api/v1/discounts/" + saved.getId())).body(saved);
    }

    @Operation(summary = "Get all discounts", description = "Retrieve all discounts", tags = {"Discounts"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of discounts", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Discount>> findAll() {
        return ResponseEntity.ok(discountService.findAll());
    }

    @Operation(summary = "Get discount by id", description = "Retrieve a discount by its id", tags = {"Discounts"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Discount found", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Discount not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Discount> findById(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.findById(id));
    }

    @Operation(summary = "Update discount", description = "Update a discount and its ticket type associations", tags = {"Discounts"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Discount updated", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Discount not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid discount data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Discount> update(@PathVariable Long id, @RequestBody @Valid DiscountRequest request) {
        Discount updated = discountService.update(request.getDiscount(), request.getApplicableTicketTypesNames());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete discount", description = "Delete a discount by id", tags = {"Discounts"})
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Discount deleted", content = @Content),
        @ApiResponse(responseCode = "404", description = "Discount not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        discountService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get ticket types by discount", description = "Retrieve ticket types associated to a discount", tags = {"Discounts"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of ticket types", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Discount not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping("/{id}/ticket-types")
    public ResponseEntity<List<TicketType>> getTicketTypesByDiscount(@PathVariable Long id) {
        // Si el descuento no existe, el service findById lanzará excepción
        discountService.findById(id);
        List<TicketType> list = discountTicketTypeService.findTicketsTypesByDiscountId(id);
        return ResponseEntity.ok(list);
    }
}
