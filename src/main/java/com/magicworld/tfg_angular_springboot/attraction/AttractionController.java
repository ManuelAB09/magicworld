package com.magicworld.tfg_angular_springboot.attraction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

import java.net.URI;
import java.util.List;

import com.magicworld.tfg_angular_springboot.storage.ImageStorageService;

@RestController
@RequestMapping("/api/v1/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;
    private final ImageStorageService imageStorageService;

    @Operation(summary = "Create attraction", description = "Create a new attraction", tags = {"Attractions"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attraction created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid attraction data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Attraction> createAttraction(@RequestBody @Valid Attraction attraction) {
        Attraction saved = attractionService.saveAttraction(attraction);
        return ResponseEntity.created(URI.create("/api/v1/attractions/" + saved.getId())).body(saved);
    }

    @Operation(summary = "Create attraction (multipart)", description = "Create a new attraction with image upload", tags = {"Attractions"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attraction created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid attraction data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Attraction> createAttractionMultipart(@RequestPart("data") @Valid AttractionRequest request,
                                                                @RequestPart("photo") MultipartFile photo) {
        String url = imageStorageService.store(photo, "attractions");
        Attraction toSave = Attraction.builder()
                .name(request.getName())
                .intensity(request.getIntensity())
                .minimumHeight(request.getMinimumHeight())
                .minimumAge(request.getMinimumAge())
                .minimumWeight(request.getMinimumWeight())
                .description(request.getDescription())
                .photoUrl(url)
                .isActive(request.getIsActive())
                .build();
        Attraction saved = attractionService.saveAttraction(toSave);
        return ResponseEntity.created(URI.create("/api/v1/attractions/" + saved.getId())).body(saved);
    }

    @Operation(summary = "Get all attractions", description = "Retrieve a list of all attractions", tags = {"Attractions"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of attractions", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid filter", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Attraction>> getAllAttractions(
            @RequestParam(required = false) Integer minHeight,
            @RequestParam(required = false) Integer minWeight,
            @RequestParam(required = false) Integer minAge) {

        List<Attraction> list = attractionService.getAllAttractions(minHeight, minWeight, minAge);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get attraction by id", description = "Retrieve an attraction by its id", tags = {"Attractions"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attraction found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Attraction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Attraction> getAttractionById(@PathVariable Long id) {
        Attraction attraction = attractionService.getAttractionById(id);
        return ResponseEntity.ok(attraction);
    }

    @Operation(summary = "Update attraction", description = "Update an existing attraction", tags = {"Attractions"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attraction updated", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Attraction not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid attraction data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Attraction> updateAttraction(@PathVariable Long id, @RequestBody @Valid Attraction updatedAttraction) {
        Attraction saved = attractionService.updateAttraction(id, updatedAttraction);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Update attraction (multipart)", description = "Update an existing attraction with optional image upload", tags = {"Attractions"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attraction updated", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Attraction not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid attraction data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Attraction> updateAttractionMultipart(@PathVariable Long id,
                                                                @RequestPart("data") @Valid AttractionRequest request,
                                                                @RequestPart(value = "photo", required = false) MultipartFile photo) {
        String url = null;
        if (photo != null && !photo.isEmpty()) {
            url = imageStorageService.store(photo, "attractions");
        }
        Attraction update = Attraction.builder()
                .name(request.getName())
                .intensity(request.getIntensity())
                .minimumHeight(request.getMinimumHeight())
                .minimumAge(request.getMinimumAge())
                .minimumWeight(request.getMinimumWeight())
                .description(request.getDescription())
                .photoUrl(url)
                .isActive(request.getIsActive())
                .build();
        Attraction saved = attractionService.updateAttraction(id, update);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Delete attraction", description = "Delete an attraction by id", tags = {"Attractions"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Attraction deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Attraction not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttraction(@PathVariable Long id) {
        attractionService.deleteAttraction(id);
        return ResponseEntity.noContent().build();
    }

}
