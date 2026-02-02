package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;

    @Transactional
    public Attraction saveAttraction(Attraction attraction) {
        return attractionRepository.save(attraction);
    }

    @Transactional(readOnly = true)
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Attraction> getAllAttractions(Integer minHeight, Integer minWeight, Integer minAge) {
        if (minHeight != null && minHeight < 0) throw new BadRequestException("minHeight");
        if (minWeight != null && minWeight < 0) throw new BadRequestException("minWeight");
        if (minAge != null && minAge < 0) throw new BadRequestException("minAge");
        return attractionRepository.findByOptionalFilters(minHeight, minWeight, minAge);
    }

    @Transactional(readOnly = true)
    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.attraction.notfound"));
    }

    @Transactional
    public void deleteAttraction(Long id) {
        Attraction attraction = getAttractionById(id);
        attractionRepository.delete(attraction);
    }

    @Transactional
    public Attraction updateAttraction(Long id, Attraction updatedAttraction) {
        Attraction existingAttraction = getAttractionById(id);

        existingAttraction.setName(updatedAttraction.getName());
        existingAttraction.setIntensity(updatedAttraction.getIntensity());
        existingAttraction.setCategory(updatedAttraction.getCategory());
        existingAttraction.setMinimumHeight(updatedAttraction.getMinimumHeight());
        existingAttraction.setMinimumAge(updatedAttraction.getMinimumAge());
        existingAttraction.setMinimumWeight(updatedAttraction.getMinimumWeight());
        existingAttraction.setDescription(updatedAttraction.getDescription());
        if (updatedAttraction.getPhotoUrl() != null) {
            existingAttraction.setPhotoUrl(updatedAttraction.getPhotoUrl());
        }
        existingAttraction.setIsActive(updatedAttraction.getIsActive());
        existingAttraction.setMapPositionX(updatedAttraction.getMapPositionX());
        existingAttraction.setMapPositionY(updatedAttraction.getMapPositionY());
        return attractionRepository.save(existingAttraction);
    }
}
