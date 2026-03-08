package com.magicworld.tfg_angular_springboot.park_closure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/park-closures")
@RequiredArgsConstructor
public class ParkClosureDayController {

    private final ParkClosureDayService service;

    @Operation(summary = "Get all closure days", tags = {"ParkClosures"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of closure days", content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<ParkClosureDay>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null && to != null) {
            return ResponseEntity.ok(service.findByRange(from, to));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Check if a date is closed", tags = {"ParkClosures"})
    @GetMapping("/check")
    public ResponseEntity<Boolean> isClosedDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.isClosedDay(date));
    }

    @Operation(summary = "Create a closure day", tags = {"ParkClosures"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Closure day created"),
            @ApiResponse(responseCode = "400", description = "Invalid data or too soon")
    })
    @PostMapping
    public ResponseEntity<ParkClosureDay> create(@RequestBody @Valid ParkClosureDayRequest request) {
        ParkClosureDay closureDay = ParkClosureDay.builder()
                .closureDate(request.getClosureDate())
                .reason(request.getReason())
                .build();
        ParkClosureDay saved = service.save(closureDay);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Delete a closure day", tags = {"ParkClosures"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Closure day deleted"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "400", description = "Too soon to delete")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

