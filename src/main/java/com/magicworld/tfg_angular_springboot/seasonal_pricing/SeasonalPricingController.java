package com.magicworld.tfg_angular_springboot.seasonal_pricing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seasonal-pricing")
@RequiredArgsConstructor
public class SeasonalPricingController {

    private final SeasonalPricingService service;

    @Operation(summary = "Get all seasonal pricing rules", tags = {"SeasonalPricing"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of seasonal pricing rules", content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<SeasonalPricing>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Get seasonal pricing by id", tags = {"SeasonalPricing"})
    @GetMapping("/{id}")
    public ResponseEntity<SeasonalPricing> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Get multiplier for a date", tags = {"SeasonalPricing"})
    @GetMapping("/multiplier")
    public ResponseEntity<Map<String, BigDecimal>> getMultiplier(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(Map.of("multiplier", service.getMultiplier(date)));
    }

    @Operation(summary = "Create seasonal pricing rule", tags = {"SeasonalPricing"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seasonal pricing created"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PostMapping
    public ResponseEntity<SeasonalPricing> create(@RequestBody @Valid SeasonalPricingRequest request) {
        SeasonalPricing pricing = SeasonalPricing.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .multiplier(request.getMultiplier())
                .applyOnWeekdays(request.getApplyOnWeekdays())
                .applyOnWeekends(request.getApplyOnWeekends())
                .build();
        SeasonalPricing saved = service.save(pricing);
        return ResponseEntity.created(URI.create("/api/v1/seasonal-pricing/" + saved.getId())).body(saved);
    }

    @Operation(summary = "Update seasonal pricing rule", tags = {"SeasonalPricing"})
    @PutMapping("/{id}")
    public ResponseEntity<SeasonalPricing> update(@PathVariable Long id, @RequestBody @Valid SeasonalPricingRequest request) {
        SeasonalPricing pricing = SeasonalPricing.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .multiplier(request.getMultiplier())
                .applyOnWeekdays(request.getApplyOnWeekdays())
                .applyOnWeekends(request.getApplyOnWeekends())
                .build();
        return ResponseEntity.ok(service.update(id, pricing));
    }

    @Operation(summary = "Delete seasonal pricing rule", tags = {"SeasonalPricing"})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

