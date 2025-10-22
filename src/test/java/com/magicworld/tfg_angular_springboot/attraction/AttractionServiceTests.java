package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AttractionServiceTests {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private AttractionRepository attractionRepository;

    private Attraction sample;

    @BeforeEach
    public void setUp() {
        attractionRepository.deleteAll();
        sample = Attraction.builder()
                .name("Test Ride")
                .intensity(Intensity.MEDIUM)
                .minimumHeight(100)
                .minimumAge(8)
                .minimumWeight(30)
                .description("A fun ride")
                .photoUrl("http://example.com/photo.jpg")
                .isActive(true)
                .build();
    }

    @AfterEach
    public void tearDown() {
        attractionRepository.deleteAll();
    }

    @Test
    public void testSaveAttraction() {
        Attraction saved = attractionService.saveAttraction(sample);
        assertNotNull(saved.getId());
        assertEquals("Test Ride", saved.getName());
    }

    @Test
    public void testGetAllAttractions() {
        attractionService.saveAttraction(sample);
        List<Attraction> all = attractionService.getAllAttractions();
        assertFalse(all.isEmpty());
        assertEquals(1, all.size());
    }

    @Test
    public void testGetAttractionById_exists() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction found = attractionService.getAttractionById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    public void testGetAttractionById_notFound() {
        assertThrows(ResourceNotFoundException.class, () -> attractionService.getAttractionById(999L));
    }

    @Test
    public void testUpdateAttraction() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = Attraction.builder()
                .name("Updated Ride")
                .intensity(Intensity.HIGH)
                .minimumHeight(120)
                .minimumAge(12)
                .minimumWeight(40)
                .description("An updated ride")
                .photoUrl("http://example.com/updated.jpg")
                .isActive(false)
                .build();

        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals("Updated Ride", updated.getName());
        assertEquals(Intensity.HIGH, updated.getIntensity());
        assertEquals(120, updated.getMinimumHeight());
        assertEquals(12, updated.getMinimumAge());
        assertEquals(40, updated.getMinimumWeight());
        assertEquals("An updated ride", updated.getDescription());
        assertEquals("http://example.com/updated.jpg", updated.getPhotoUrl());
        assertFalse(updated.getIsActive());
    }

    @Test
    public void testDeleteAttraction() {
        Attraction saved = attractionService.saveAttraction(sample);
        Long id = saved.getId();
        attractionService.deleteAttraction(id);
        assertThrows(ResourceNotFoundException.class, () -> attractionService.getAttractionById(id));
    }
}
